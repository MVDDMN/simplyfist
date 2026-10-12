package com.simplyfist.effect;

import com.simplyfist.registry.ModEffects;
import com.simplyfist.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class RisingStrikeEffect extends MobEffect {

    private static final double DAMAGE_MULTIPLIER = 2.0D;
    private static final double LAUNCH_STRENGTH = 0.75D;

    private static final Map<UUID, Integer> PENDING_LAUNCHES =
            new ConcurrentHashMap<>();

    public RisingStrikeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
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

        if (!player.hasEffect(ModEffects.RISING_STRIKE.get())) {
            return;
        }

        LivingEntity target = event.getEntity();

        event.setAmount(
                event.getAmount() * (float) DAMAGE_MULTIPLIER
        );

        PENDING_LAUNCHES.put(
                target.getUUID(),
                1
        );

        target.level().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                ModSounds.RISING_STRIKE.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        if (target.level() instanceof ServerLevel serverLevel) {
            double centerX = target.getX();
            double centerY = target.getY() + target.getBbHeight() * 0.5D;
            double centerZ = target.getZ();

            for (int i = 0; i < 24; i++) {
                double x = target.getRandom().nextDouble() - 0.5D;
                double y = target.getRandom().nextDouble() - 0.15D;
                double z = target.getRandom().nextDouble() - 0.5D;

                Vec3 direction = new Vec3(x, y, z);

                if (direction.lengthSqr() < 0.01D) {
                    direction = new Vec3(0.0D, 1.0D, 0.0D);
                }

                direction = direction.normalize();

                double speed =
                        0.15D + target.getRandom().nextDouble() * 0.25D;

                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        centerX,
                        centerY,
                        centerZ,
                        1,
                        direction.x * speed,
                        direction.y * speed,
                        direction.z * speed,
                        1.0D
                );
            }
        }

        player.removeEffect(ModEffects.RISING_STRIKE.get());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        PENDING_LAUNCHES.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();

            for (ServerLevel level : event.getServer().getAllLevels()) {
                LivingEntity target = findEntity(level, uuid);

                if (target != null && target.isAlive()) {
                    Vec3 velocity = target.getDeltaMovement();

                    target.setDeltaMovement(
                            velocity.x,
                            LAUNCH_STRENGTH,
                            velocity.z
                    );

                    target.hasImpulse = true;

                    return true;
                }
            }

            return true;
        });
    }

    private static LivingEntity findEntity(
            ServerLevel level,
            UUID uuid
    ) {
        for (var player : level.players()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }

        for (var entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity livingEntity
                    && livingEntity.getUUID().equals(uuid)) {
                return livingEntity;
            }
        }

        return null;
    }
}