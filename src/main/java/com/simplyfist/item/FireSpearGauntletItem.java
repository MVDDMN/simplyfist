package com.simplyfist.item;

import com.simplyfist.registry.ModEffects;
import com.simplyfist.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        modid = "simplyfist",
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class FireSpearGauntletItem extends SwordItem {

    private static final long MAX_CHARGE_TIME_NANOS = 5_500_000_000L;
    private static final double MIN_CHARGE_STRENGTH = 1.2D;
    private static final double MAX_CHARGE_STRENGTH = 4.0D;

    private static final int PUNCH_DURATION = 20;
    private static final int COOLDOWN = 600;

    private static final int STUN_DURATION = 60;

    private static final String CHARGING_TAG = "FireSpearCharging";
    private static final String CHARGE_COMPLETE_TAG = "FireSpearChargeComplete";
    private static final String CHARGE_START_TIME_TAG = "FireSpearChargeStartTime";
    private static final String PUNCHING_TAG = "FireSpearPunching";
    private static final String PUNCH_TIMER_TAG = "FireSpearPunchTimer";
    private static final String HIT_COUNT_TAG = "FireSpearHitCount";

    public FireSpearGauntletItem(Properties properties) {
        super(Tiers.IRON, 6, -2.6F, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
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

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.CHARGING.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(
            ItemStack stack,
            Level level,
            LivingEntity livingEntity,
            int timeCharged
    ) {
        if (!(livingEntity instanceof Player player)) {
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
                MIN_CHARGE_STRENGTH +
                        ((MAX_CHARGE_STRENGTH - MIN_CHARGE_STRENGTH)
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

        player.setDeltaMovement(
                player.getLookAngle().scale(strength)
        );

        player.hurtMarked = true;

        player.getPersistentData().putBoolean(
                PUNCHING_TAG,
                true
        );

        player.getPersistentData().putInt(
                PUNCH_TIMER_TAG,
                PUNCH_DURATION
        );

        player.getCooldowns().addCooldown(
                this,
                COOLDOWN
        );

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

        if (!(stack.getItem() instanceof FireSpearGauntletItem)) {
            return;
        }

        if (player.getPersistentData().getBoolean(PUNCHING_TAG)) {
            return;
        }

        int hitCount = player.getPersistentData().getInt(
                HIT_COUNT_TAG
        );

        hitCount++;

        if (hitCount >= 4) {
            event.setAmount(
                    event.getAmount() * 1.30F
            );

            LivingEntity target = event.getEntity();

            target.setDeltaMovement(
                    target.getDeltaMovement().x,
                    0.65D,
                    target.getDeltaMovement().z
            );

            target.hurtMarked = true;

            hitCount = 0;
        }

        player.getPersistentData().putInt(
                HIT_COUNT_TAG,
                hitCount
        );
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
        updatePunch(player);
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

        spawnChargeParticles(
                player,
                serverLevel,
                progress
        );

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
                    ParticleTypes.FLAME,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    35,
                    0.6D,
                    0.8D,
                    0.6D,
                    0.12D
            );

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    20,
                    0.45D,
                    0.7D,
                    0.45D,
                    0.06D
            );

            serverLevel.sendParticles(
                    ParticleTypes.LAVA,
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    8,
                    0.35D,
                    0.5D,
                    0.35D,
                    0.0D
            );
        }
    }

    private static void spawnChargeParticles(
            Player player,
            ServerLevel serverLevel,
            double progress
    ) {
        double radius = 0.3D + (progress * 0.5D);

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                3,
                radius,
                0.45D,
                radius,
                0.05D
        );

        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                1,
                radius * 0.8D,
                0.35D,
                radius * 0.8D,
                0.025D
        );

        if (player.tickCount % 4 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.LAVA,
                    player.getX(),
                    player.getY() + 0.8D,
                    player.getZ(),
                    1,
                    radius * 0.7D,
                    0.25D,
                    radius * 0.7D,
                    0.0D
            );
        }
    }

    private static void updatePunch(Player player) {
        if (!player.getPersistentData().getBoolean(PUNCHING_TAG)) {
            return;
        }

        int timer = player.getPersistentData().getInt(
                PUNCH_TIMER_TAG
        );

        if (timer <= 0) {
            player.getPersistentData().putBoolean(
                    PUNCHING_TAG,
                    false
            );
            return;
        }

        spawnPunchParticles(player);

        AABB hitBox = player.getBoundingBox().inflate(0.8D);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                hitBox,
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity target : targets) {
            performSpear(player, target);
            break;
        }

        timer--;

        player.getPersistentData().putInt(
                PUNCH_TIMER_TAG,
                timer
        );

        if (timer <= 0) {
            player.getPersistentData().putBoolean(
                    PUNCHING_TAG,
                    false
            );
        }
    }

    private static void performSpear(
            Player player,
            LivingEntity target
    ) {
        player.getPersistentData().putBoolean(
                PUNCHING_TAG,
                false
        );

        float currentHealth = target.getHealth();
        float damage = currentHealth * 0.25F;

        boolean hurt = target.hurt(
                player.damageSources().playerAttack(player),
                damage
        );

        if (!hurt) {
            return;
        }

        target.addEffect(
                new MobEffectInstance(
                        ModEffects.STUN.get(),
                        STUN_DURATION,
                        0
                )
        );

        target.setDeltaMovement(
                target.getDeltaMovement().x,
                0.35D,
                target.getDeltaMovement().z
        );

        target.hurtMarked = true;

        spawnHitFireParticles(target);

        player.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                ModSounds.SUPERMAN_PUNCH.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private static void spawnHitFireParticles(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                30,
                0.4D,
                target.getBbHeight() * 0.4D,
                0.4D,
                0.08D
        );

        serverLevel.sendParticles(
                ParticleTypes.LAVA,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                6,
                0.3D,
                target.getBbHeight() * 0.3D,
                0.3D,
                0.0D
        );
    }

    private static void spawnPunchParticles(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                player.getX(),
                player.getY() + 0.5D,
                player.getZ(),
                6,
                0.25D,
                0.25D,
                0.25D,
                0.02D
        );

        serverLevel.sendParticles(
                ParticleTypes.LAVA,
                player.getX(),
                player.getY() + 0.5D,
                player.getZ(),
                1,
                0.15D,
                0.15D,
                0.15D,
                0.0D
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("§6Passive: Samoan Drop"));
        tooltip.add(Component.literal(
                "§7Every §e4th hit §7deals §c30% increased damage§7."
        ));
        tooltip.add(Component.literal(
                "§7The target is launched §e2 blocks§7 upward."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§bOn Right Click: Spear"));
        tooltip.add(Component.literal("§7[>] Cooldown: 30 seconds"));
        tooltip.add(Component.literal(
                "§7Hold to charge the Spear."
        ));
        tooltip.add(Component.literal(
                "§fDeals §c25% §fof the target's current HP."
        ));
        tooltip.add(Component.literal(
                "§fApplies §cStun §ffor §e3 seconds§f."
        ));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}