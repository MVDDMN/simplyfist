package com.simplyfist.item;

import com.simplyfist.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class SteraksGageItem extends FistWeaponItem {

    private static final int HITS_PER_PROC = 4;
    private static final float CURRENT_HEALTH_DAMAGE_PERCENT = 0.05F;

    private static final float ABSORPTION_PERCENT = 0.40F;
    private static final int LIFELINE_COOLDOWN = 7200;
    private static final int LIFELINE_ABSORPTION_DURATION = 1200;

    private static final String DEATHBRINGER_HIT_COUNT_TAG =
            "SteraksGageDeathbringerHitCount";

    private static final String LIFELINE_TIMER_TAG =
            "SteraksGageLifelineTimer";

    private static final String LIFELINE_ABSORPTION_TAG =
            "SteraksGageLifelineAbsorption";

    private static final ThreadLocal<Boolean> APPLYING_BONUS_DAMAGE =
            ThreadLocal.withInitial(() -> false);

    public SteraksGageItem(Properties properties) {
        super(Tiers.IRON, 6, -2.8F, properties);
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

        if (!level.isClientSide) {
            float absorptionAmount =
                    player.getMaxHealth() * ABSORPTION_PERCENT;

            player.setAbsorptionAmount(
                    player.getAbsorptionAmount() + absorptionAmount
            );

            player.getPersistentData().putInt(
                    LIFELINE_TIMER_TAG,
                    LIFELINE_ABSORPTION_DURATION
            );

            player.getPersistentData().putFloat(
                    LIFELINE_ABSORPTION_TAG,
                    absorptionAmount
            );

            player.getCooldowns().addCooldown(
                    this,
                    LIFELINE_COOLDOWN
            );

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.TOTEM_USE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(),
                        player.getY() + 1.0D,
                        player.getZ(),
                        40,
                        0.5D,
                        0.8D,
                        0.5D,
                        0.5D
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide
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

        int timer =
                player.getPersistentData().getInt(
                        LIFELINE_TIMER_TAG
                );

        if (timer <= 0) {
            return;
        }

        timer--;

        if (timer > 0) {
            player.getPersistentData().putInt(
                    LIFELINE_TIMER_TAG,
                    timer
            );
            return;
        }

        float lifelineAbsorption =
                player.getPersistentData().getFloat(
                        LIFELINE_ABSORPTION_TAG
                );

        float currentAbsorption =
                player.getAbsorptionAmount();

        player.setAbsorptionAmount(
                Math.max(
                        0.0F,
                        currentAbsorption - lifelineAbsorption
                )
        );

        player.getPersistentData().remove(
                LIFELINE_TIMER_TAG
        );

        player.getPersistentData().remove(
                LIFELINE_ABSORPTION_TAG
        );
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (APPLYING_BONUS_DAMAGE.get()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        boolean hasSteraks =
                player.getMainHandItem().is(ModItems.STERAKS_GAGE.get())
                        || player.getOffhandItem().is(ModItems.STERAKS_GAGE.get());

        if (!hasSteraks) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (!target.isAlive()) {
            return;
        }

        int hitCount =
                player.getPersistentData().getInt(
                        DEATHBRINGER_HIT_COUNT_TAG
                );

        hitCount++;

        if (hitCount < HITS_PER_PROC) {
            player.getPersistentData().putInt(
                    DEATHBRINGER_HIT_COUNT_TAG,
                    hitCount
            );

            return;
        }

        player.getPersistentData().putInt(
                DEATHBRINGER_HIT_COUNT_TAG,
                0
        );

        float currentHealth = target.getHealth();

        float bonusDamage =
                currentHealth * CURRENT_HEALTH_DAMAGE_PERCENT;

        if (bonusDamage <= 0.0F) {
            return;
        }

        try {
            APPLYING_BONUS_DAMAGE.set(true);

            target.hurt(
                    player.damageSources().playerAttack(player),
                    bonusDamage
            );
        } finally {
            APPLYING_BONUS_DAMAGE.set(false);
        }

        Level level = target.level();

        level.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT,
                SoundSource.PLAYERS,
                1.2F,
                0.8F
        );

        if (level instanceof ServerLevel serverLevel) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5D;
            double z = target.getZ();

            serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    x,
                    y,
                    z,
                    20,
                    target.getBbWidth() * 0.7D,
                    target.getBbHeight() * 0.7D,
                    target.getBbWidth() * 0.7D,
                    0.08D
            );

            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x,
                    y,
                    z,
                    15,
                    target.getBbWidth() * 0.5D,
                    target.getBbHeight() * 0.6D,
                    target.getBbWidth() * 0.5D,
                    0.04D
            );

            serverLevel.sendParticles(
                    ParticleTypes.ASH,
                    x,
                    y,
                    z,
                    25,
                    target.getBbWidth() * 0.8D,
                    target.getBbHeight() * 0.8D,
                    target.getBbWidth() * 0.8D,
                    0.05D
            );

            serverLevel.sendParticles(
                    ParticleTypes.DAMAGE_INDICATOR,
                    x,
                    y,
                    z,
                    10,
                    target.getBbWidth() * 0.6D,
                    target.getBbHeight() * 0.6D,
                    target.getBbWidth() * 0.6D,
                    0.12D
            );
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("§6Passive: Deathbringer Stance"));
        tooltip.add(Component.literal(
                "§7Every §e4th hit §7deals §c5% §fof the target's current HP as physical damage."
        ));
        tooltip.add(Component.literal(
                "§7Works while held in §eeither hand§7."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§cOn Right Click: Lifeline"));
        tooltip.add(Component.literal("§7[>] Cooldown: 360 seconds"));
        tooltip.add(Component.literal(
                "Instantly grants absorption equal to §e40% §fof your maximum HP §7for §e60 seconds§7."
        ));

        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );
    }
}