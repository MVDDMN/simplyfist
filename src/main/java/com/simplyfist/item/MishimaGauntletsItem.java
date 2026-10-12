package com.simplyfist.item;

import com.simplyfist.registry.ModEffects;
import com.simplyfist.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class MishimaGauntletsItem extends FistWeaponItem {

    private static final String DAMAGE_STACKS_TAG = "MishimaDamageStacks";
    private static final String DAMAGE_STACK_EXPIRY_TAG = "MishimaDamageStackExpiry";

    private static final int MAX_DAMAGE_STACKS = 5;
    private static final double DAMAGE_PER_STACK = 0.10D;
    private static final int STACK_DURATION = 100;

    private static final int RISING_STRIKE_DURATION = 100;

    private static final double DASH_STRENGTH = 1.8D;
    private static final int DASH_COOLDOWN = 200;

    public MishimaGauntletsItem(Properties properties) {
        super(Tiers.IRON, 4, -2.6F, properties);
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
            Vec3 look = player.getLookAngle();
            Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

            if (horizontal.lengthSqr() > 0.0001D) {
                horizontal = horizontal.normalize().scale(DASH_STRENGTH);

                Vec3 velocity = player.getDeltaMovement();

                player.setDeltaMovement(
                        horizontal.x,
                        velocity.y,
                        horizontal.z
                );

                player.hasImpulse = true;

                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModSounds.DASH.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        0.85F
                );

                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetEntityMotionPacket(player)
                    );
                }

                player.addEffect(new MobEffectInstance(
                        ModEffects.RISING_STRIKE.get(),
                        RISING_STRIKE_DURATION,
                        0,
                        false,
                        true,
                        true
                ));

                player.getCooldowns().addCooldown(
                        this,
                        DASH_COOLDOWN
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
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

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof MishimaGauntletsItem)) {
            return;
        }

        long currentTime = player.level().getGameTime();
        int stacks = getDamageStacks(player, currentTime);

        double damageMultiplier =
                1.0D + (stacks * DAMAGE_PER_STACK);

        event.setAmount(
                event.getAmount() * (float) damageMultiplier
        );

        int newStacks = Math.min(
                stacks + 1,
                MAX_DAMAGE_STACKS
        );

        setDamageStacks(
                player,
                newStacks,
                currentTime + STACK_DURATION
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("§6Passive: Mishima Momentum"));
        tooltip.add(Component.literal(
                "§7Hits grant §c+10% damage§7 per stack."
        ));
        tooltip.add(Component.literal(
                "§7Maximum: §e5 stacks §7(§c+50% damage§7)"
        ));
        tooltip.add(Component.literal(
                "§7Stacks expire after §e5 seconds §7without hitting."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§cOn Right Click: Wavedash"));
        tooltip.add(Component.literal("§7[>] Cooldown: 10 seconds"));
        tooltip.add(Component.literal(
                "Dash forward and gain Rising Strike for 5 seconds."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§eRising Strike"));
        tooltip.add(Component.literal(
                "The next melee hit deals 2x damage."
        ));
        tooltip.add(Component.literal(
                "Launches the target approximately 3 blocks upward."
        ));

        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static int getDamageStacks(Player player, long currentTime) {
        if (!player.getPersistentData().contains(DAMAGE_STACKS_TAG)) {
            return 0;
        }

        long expiry = player.getPersistentData().getLong(
                DAMAGE_STACK_EXPIRY_TAG
        );

        if (currentTime >= expiry) {
            clearDamageStacks(player);
            return 0;
        }

        return Math.min(
                player.getPersistentData().getInt(DAMAGE_STACKS_TAG),
                MAX_DAMAGE_STACKS
        );
    }

    private static void setDamageStacks(
            Player player,
            int stacks,
            long expiry
    ) {
        player.getPersistentData().putInt(
                DAMAGE_STACKS_TAG,
                stacks
        );

        player.getPersistentData().putLong(
                DAMAGE_STACK_EXPIRY_TAG,
                expiry
        );
    }

    private static void clearDamageStacks(Player player) {
        player.getPersistentData().remove(DAMAGE_STACKS_TAG);
        player.getPersistentData().remove(DAMAGE_STACK_EXPIRY_TAG);
    }
}