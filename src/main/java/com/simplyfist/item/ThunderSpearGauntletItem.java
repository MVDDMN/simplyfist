package com.simplyfist.item;

import com.simplyfist.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class ThunderSpearGauntletItem extends FistWeaponItem {

    private static final String CHARGING_TAG = "ThunderSpearCharging";
    private static final String CHARGE_COMPLETE_TAG = "ThunderSpearChargeComplete";
    private static final String PUNCHING_TAG = "ThunderSpearPunching";
    private static final String CHARGE_START_TIME_TAG = "ThunderSpearChargeStartTime";
    private static final String PUNCH_TIME_TAG = "ThunderSpearPunchTime";
    private static final String SPEED_STACKS_TAG = "ThunderSpearSpeedStacks";
    private static final String SPEED_TIMER_TAG = "ThunderSpearSpeedTimer";
    private static final String SUPERMAN_PUNCH_KILL_STACKS_TAG = "SupermanPunchKillStacks";

    private static final long MAX_CHARGE_TIME_NANOS = 5_500_000_000L;

    private static final double MIN_CHARGE_STRENGTH = 1.2D;
    private static final double MAX_CHARGE_STRENGTH = 4.0D;

    private static final double DAMAGE_MULTIPLIER = 3.0D;

    private static final int PUNCH_DURATION = 20;

    private static final int SPEED_DURATION = 40;
    private static final int MAX_SPEED_STACKS = 10;

    private static final int COOLDOWN = 300;

    public ThunderSpearGauntletItem(Properties properties) {
        super(Tiers.IRON, 7, -2.8F, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);

        if (!level.isClientSide) {
            player.getPersistentData().putBoolean(
                    CHARGING_TAG,
                    true
            );

            player.getPersistentData().putBoolean(
                    CHARGE_COMPLETE_TAG,
                    false
            );

            player.getPersistentData().putLong(
                    CHARGE_START_TIME_TAG,
                    System.nanoTime()
            );
        }

        player.playSound(
                ModSounds.CHARGING.get(),
                1.0F,
                1.0F
        );

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            int timeLeft
    ) {
        if (!(entity instanceof Player player)) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        if (!player.getPersistentData().getBoolean(CHARGING_TAG)) {
            return;
        }

        long chargeStartTime =
                player.getPersistentData().getLong(
                        CHARGE_START_TIME_TAG
                );

        long elapsedTime =
                System.nanoTime() - chargeStartTime;

        double chargeProgress =
                Math.min(
                        (double) elapsedTime / MAX_CHARGE_TIME_NANOS,
                        1.0D
                );

        double strength =
                MIN_CHARGE_STRENGTH
                        + ((MAX_CHARGE_STRENGTH - MIN_CHARGE_STRENGTH)
                        * chargeProgress);

        player.getPersistentData().putBoolean(
                CHARGING_TAG,
                false
        );

        player.getPersistentData().putBoolean(
                CHARGE_COMPLETE_TAG,
                false
        );

        player.getPersistentData().remove(
                CHARGE_START_TIME_TAG
        );

        Vec3 look = player.getLookAngle().normalize();

        player.setDeltaMovement(
                look.x * strength,
                look.y * strength,
                look.z * strength
        );

        player.hasImpulse = true;

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.DASH.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        player.getPersistentData().putBoolean(
                PUNCHING_TAG,
                true
        );

        player.getPersistentData().putInt(
                PUNCH_TIME_TAG,
                PUNCH_DURATION
        );

        player.getCooldowns().addCooldown(
                this,
                COOLDOWN
        );

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                    new ClientboundSetEntityMotionPacket(player)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        updateCharge(player);
        updateSpeedStacks(player);

        if (!player.getPersistentData().getBoolean(PUNCHING_TAG)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ThunderSpearGauntletItem)) {
            stopPunch(player);
            return;
        }

        int punchTime = player.getPersistentData().getInt(
                PUNCH_TIME_TAG
        );

        if (punchTime <= 0) {
            stopPunch(player);
            return;
        }

        player.getPersistentData().putInt(
                PUNCH_TIME_TAG,
                punchTime - 1
        );

        ServerLevel serverLevel = (ServerLevel) player.level();

        Vec3 movement = player.getDeltaMovement();

        if (movement.lengthSqr() > 0.01D) {
            Vec3 trailPosition = player.position()
                    .subtract(movement.normalize().scale(0.7D));

            serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    trailPosition.x,
                    trailPosition.y + 0.5D,
                    trailPosition.z,
                    2,
                    0.15D,
                    0.15D,
                    0.15D,
                    0.02D
            );
        }

        AABB hitBox = player.getBoundingBox().inflate(0.8D);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                hitBox,
                target ->
                        target != player
                                && target.isAlive()
                                && !target.isSpectator()
        );

        if (targets.isEmpty()) {
            return;
        }

        LivingEntity target = targets.get(0);

        int killStacks = stack.getOrCreateTag().getInt(
                SUPERMAN_PUNCH_KILL_STACKS_TAG
        );

        float baseDamage = (float) player.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE
        );

        float damage =
                (baseDamage + killStacks) * (float) DAMAGE_MULTIPLIER;

        stopPunch(player);

        if (target.hurt(
                player.damageSources().playerAttack(player),
                damage
        )) {
            ServerLevel targetLevel = (ServerLevel) target.level();

            targetLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    target.getX(),
                    target.getY() + target.getBbHeight() * 0.5D,
                    target.getZ(),
                    8,
                    0.4D,
                    0.4D,
                    0.4D,
                    0.05D
            );

            target.level().playSound(
                    null,
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    ModSounds.SUPERMAN_PUNCH.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            if (!target.isAlive()) {
                killStacks++;

                stack.getOrCreateTag().putInt(
                        SUPERMAN_PUNCH_KILL_STACKS_TAG,
                        killStacks
                );
            }
        }
    }

    private static void updateCharge(Player player) {
        if (!player.getPersistentData().getBoolean(CHARGING_TAG)) {
            return;
        }

        long chargeStartTime =
                player.getPersistentData().getLong(
                        CHARGE_START_TIME_TAG
                );

        long elapsedTime =
                System.nanoTime() - chargeStartTime;

        ServerLevel serverLevel = (ServerLevel) player.level();

        double progress =
                Math.min(
                        (double) elapsedTime / MAX_CHARGE_TIME_NANOS,
                        1.0D
                );

        spawnChargeParticles(player, serverLevel, progress);

        if (elapsedTime >= MAX_CHARGE_TIME_NANOS
                && !player.getPersistentData().getBoolean(CHARGE_COMPLETE_TAG)) {

            player.getPersistentData().putBoolean(
                    CHARGE_COMPLETE_TAG,
                    true
            );

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.5F
            );

            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    20,
                    0.5D,
                    0.7D,
                    0.5D,
                    0.15D
            );

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    12,
                    0.35D,
                    0.6D,
                    0.35D,
                    0.03D
            );
        }
    }

    private static void spawnChargeParticles(
            Player player,
            ServerLevel serverLevel,
            double progress
    ) {
        double radius = 0.35D + (progress * 0.45D);

        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                2,
                radius,
                0.5D,
                radius,
                0.08D
        );

        if (player.tickCount % 4 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    1,
                    radius,
                    0.45D,
                    radius,
                    0.02D
            );
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof ThunderSpearGauntletItem)) {
            return;
        }

        if (player.getPersistentData().getBoolean(PUNCHING_TAG)) {
            return;
        }

        int stacks = player.getPersistentData().getInt(
                SPEED_STACKS_TAG
        );

        stacks = Math.min(
                stacks + 1,
                MAX_SPEED_STACKS
        );

        player.getPersistentData().putInt(
                SPEED_STACKS_TAG,
                stacks
        );

        player.getPersistentData().putInt(
                SPEED_TIMER_TAG,
                SPEED_DURATION
        );

        int speedLevel;

        if (stacks <= 3) {
            speedLevel = 0;
        } else if (stacks <= 6) {
            speedLevel = 1;
        } else {
            speedLevel = 2;
        }

        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                SPEED_DURATION,
                speedLevel
        ));
    }

    private static void updateSpeedStacks(Player player) {
        int timer = player.getPersistentData().getInt(
                SPEED_TIMER_TAG
        );

        if (timer <= 0) {
            return;
        }

        timer--;

        if (timer <= 0) {
            player.getPersistentData().remove(
                    SPEED_STACKS_TAG
            );

            player.getPersistentData().remove(
                    SPEED_TIMER_TAG
            );

            return;
        }

        player.getPersistentData().putInt(
                SPEED_TIMER_TAG,
                timer
        );
    }

    private static void stopPunch(Player player) {
        player.getPersistentData().putBoolean(
                PUNCHING_TAG,
                false
        );

        player.getPersistentData().remove(
                PUNCH_TIME_TAG
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        int killStacks = stack.getOrCreateTag().getInt(
                SUPERMAN_PUNCH_KILL_STACKS_TAG
        );

        tooltip.add(Component.literal("§6Passive: Drive-by"));
        tooltip.add(Component.literal(
                "§7Hits grant §bSpeed§7 per stack."
        ));
        tooltip.add(Component.literal(
                "§7Maximum: §e10 stacks §7(§bSpeed I - III§7)"
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§cOn Right Click: Superman Punch"));
        tooltip.add(Component.literal("§7[>] Cooldown: 15 seconds"));
        tooltip.add(Component.literal(
                "Hold to charge and release to dash forward."
        ));
        tooltip.add(Component.literal(
                "Deals §c3x §fdamage on hit."
        ));
        tooltip.add(Component.literal(
                "§7Superman Punch Kill Stacks: §e" + killStacks
        ));
        tooltip.add(Component.literal(
                "§7Each Superman Punch kill permanently adds §e+1 §7base damage."
        ));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}