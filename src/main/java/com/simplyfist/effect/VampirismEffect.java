package com.simplyfist.effect;

import com.simplyfist.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class VampirismEffect extends MobEffect {

    private static final float BASE_HEAL = 4.0F;
    private static final float MAX_HEAL = 20.0F;
    private static final int MAX_LEVEL = 10;

    public VampirismEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    public static void healOnHit(Player player) {
        MobEffectInstance effect =
                player.getEffect(ModEffects.VAMPIRISM.get());

        if (effect == null) {
            return;
        }

        int level = Math.min(
                effect.getAmplifier() + 1,
                MAX_LEVEL
        );

        float healAmount =
                BASE_HEAL
                        + ((level - 1)
                        * (MAX_HEAL - BASE_HEAL)
                        / (MAX_LEVEL - 1));

        player.heal(healAmount);
    }
}