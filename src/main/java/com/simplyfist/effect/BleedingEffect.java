package com.simplyfist.effect;

import com.simplyfist.registry.ModEffects;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class BleedingEffect extends MobEffect {

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(
                    "simplyfist",
                    "textures/mob_effect/bleed.png"
            );

    private static final ThreadLocal<Boolean> APPLYING_BONUS_DAMAGE =
            ThreadLocal.withInitial(() -> false);

    public BleedingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (APPLYING_BONUS_DAMAGE.get()) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        MobEffectInstance effect =
                attacker.getEffect(ModEffects.BLEEDING.get());

        if (effect == null) {
            return;
        }

        float originalDamage = event.getAmount();

        float bonusMultiplier =
                (effect.getAmplifier() + 1) * 0.10F;

        float bonusDamage =
                originalDamage * bonusMultiplier;

        if (bonusDamage <= 0.0F) {
            return;
        }

        int previousInvulnerabilityTime =
                target.invulnerableTime;

        try {
            APPLYING_BONUS_DAMAGE.set(true);

            target.invulnerableTime = 0;

            target.hurt(
                    target.damageSources().magic(),
                    bonusDamage
            );
        } finally {
            target.invulnerableTime =
                    previousInvulnerabilityTime;

            APPLYING_BONUS_DAMAGE.set(false);
        }
    }

    @Override
    public void initializeClient(
            Consumer<IClientMobEffectExtensions> consumer
    ) {
        consumer.accept(new IClientMobEffectExtensions() {

            @Override
            public boolean renderInventoryIcon(
                    MobEffectInstance instance,
                    EffectRenderingInventoryScreen<?> screen,
                    GuiGraphics guiGraphics,
                    int x,
                    int y,
                    int blitOffset
            ) {
                guiGraphics.blit(
                        ICON,
                        x,
                        y + 7,
                        blitOffset,
                        0,
                        0,
                        18,
                        18,
                        18,
                        18
                );

                return true;
            }

            @Override
            public boolean renderGuiIcon(
                    MobEffectInstance instance,
                    Gui gui,
                    GuiGraphics guiGraphics,
                    int x,
                    int y,
                    float z,
                    float alpha
            ) {
                guiGraphics.blit(
                        ICON,
                        x + 3,
                        y + 3,
                        18,
                        18,
                        0,
                        0,
                        18,
                        18,
                        18,
                        18
                );

                return true;
            }
        });
    }
}