package com.simplyfist.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StunEffect extends MobEffect {

    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555);

        addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                "6d4f5a3e-2b7c-4f8a-91d6-8e2c4a7b5f10",
                -0.99D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "a83e6c21-5f49-4d72-b8c3-1e7a9f2d6405",
                -0.99D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(0.0D, 0.0D, 0.0D);
        entity.hasImpulse = false;

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float rotation = entity.tickCount % 2 == 0 ? 4.0F : -4.0F;

        entity.setYRot(entity.getYRot() + rotation);
        entity.yBodyRot += rotation;
        entity.setYHeadRot(entity.getYHeadRot() + rotation);

        if (entity.tickCount % 3 == 0) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + entity.getBbHeight() * (0.5D + entity.getRandom().nextDouble() * 0.5D);
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();

            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    x,
                    y,
                    z,
                    2,
                    0.1D,
                    0.2D,
                    0.1D,
                    0.02D
            );
        }

        if (entity.tickCount % 6 == 0) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
            double y = entity.getY() + entity.getBbHeight() + 0.1D;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();

            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    x,
                    y,
                    z,
                    1,
                    0.05D,
                    0.05D,
                    0.05D,
                    0.01D
            );
        }
    }
}