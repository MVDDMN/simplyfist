package com.simplyfist.item;

import com.simplyfist.effect.VampirismEffect;
import com.simplyfist.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BloodyKnucklesItem extends FistWeaponItem {

    private static final int HITS_PER_SERRATED_HIT = 4;

    private static final int SERRATED_HIT_DURATION = 120;
    private static final int MAX_SERRATED_HIT_DURATION = 600;
    private static final int SERRATED_HIT_AMPLIFIER = 2;

    private static final int VAMPIRISM_LEVEL = 1;
    private static final int VAMPIRISM_DURATION = 200;

    private static final int BLOOD_SACRIFICE_COOLDOWN = 600;
    private static final float BLOOD_SACRIFICE_PERCENT = 0.25F;

    private static final String SERRATED_HIT_COUNT_TAG =
            "BloodyKnucklesSerratedHitCount";

    public BloodyKnucklesItem(Properties properties) {
        super(Tiers.IRON, 6, -2.6F, properties);
    }

    @Override
    public boolean hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {
        if (!attacker.level().isClientSide
                && attacker instanceof Player player) {

            applySerratedHit(player);

            VampirismEffect.healOnHit(player);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    private void applySerratedHit(Player player) {
        int hitCount =
                player.getPersistentData().getInt(
                        SERRATED_HIT_COUNT_TAG
                );

        hitCount++;

        if (hitCount < HITS_PER_SERRATED_HIT) {
            player.getPersistentData().putInt(
                    SERRATED_HIT_COUNT_TAG,
                    hitCount
            );

            return;
        }

        player.getPersistentData().putInt(
                SERRATED_HIT_COUNT_TAG,
                0
        );

        MobEffectInstance existing =
                player.getEffect(ModEffects.BLEEDING.get());

        int duration;

        if (existing == null) {
            duration = SERRATED_HIT_DURATION;
        } else {
            duration =
                    Math.min(
                            existing.getDuration()
                                    + SERRATED_HIT_DURATION,
                            MAX_SERRATED_HIT_DURATION
                    );
        }

        player.addEffect(
                new MobEffectInstance(
                        ModEffects.BLEEDING.get(),
                        duration,
                        SERRATED_HIT_AMPLIFIER
                )
        );
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

        if (player.getHealth()
                <= player.getMaxHealth() * BLOOD_SACRIFICE_PERCENT) {

            player.displayClientMessage(
                    Component.literal(
                            "§cYou're way too low in blood for that..."
                    ),
                    true
            );

            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            float sacrificeAmount =
                    player.getMaxHealth()
                            * BLOOD_SACRIFICE_PERCENT;

            player.setHealth(
                    Math.max(
                            0.0F,
                            player.getHealth() - sacrificeAmount
                    )
            );

            player.addEffect(
                    new MobEffectInstance(
                            ModEffects.VAMPIRISM.get(),
                            VAMPIRISM_DURATION,
                            VAMPIRISM_LEVEL - 1
                    )
            );

            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ZOMBIE_VILLAGER_CURE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );

            player.getCooldowns().addCooldown(
                    this,
                    BLOOD_SACRIFICE_COOLDOWN
            );
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.literal("§cPassive: Bloodshed")
        );

        tooltip.add(
                Component.literal(
                        "§7Every §e4th hit§7 grants §cSerrated Hit III§7."
                )
        );

        tooltip.add(
                Component.literal(
                        "§7Serrated Hit III: §e+30% damage§7 dealt as magic damage."
                )
        );

        tooltip.add(
                Component.literal(
                        "§7Each 4-hit cycle adds §e6 seconds§7."
                )
        );

        tooltip.add(
                Component.literal(
                        "§7Maximum duration: §e30 seconds§7."
                )
        );

        tooltip.add(Component.literal(""));

        tooltip.add(
                Component.literal(
                        "§4On Right Click: Blood Sacrifice"
                )
        );

        tooltip.add(
                Component.literal(
                        "§7[>] Cooldown: 30 seconds"
                )
        );

        tooltip.add(
                Component.literal(
                        "Lose 25% of your maximum HP."
                )
        );

        tooltip.add(
                Component.literal(
                        "Gain Vampirism I for 10 seconds."
                )
        );

        tooltip.add(Component.literal(""));

        tooltip.add(
                Component.literal("§dVampirism I")
        );

        tooltip.add(
                Component.literal("Hits heal 4 HP.")
        );

        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );
    }
}