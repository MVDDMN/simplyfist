package com.simplyfist.item;

import com.simplyfist.registry.ModEffects;
import com.simplyfist.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = "simplyfist")
public class BoxingGlovesItem extends FistWeaponItem {

    private static final String HASTE_HITS_TAG = "BoxingGlovesHasteHits";
    private static final String HASTE_TIMER_TAG = "BoxingGlovesHasteTimer";

    private static final int MAX_HITS = 20;
    private static final int HITS_PER_LEVEL = 4;
    private static final int MAX_HASTE_LEVEL = 5;

    private static final int HASTE_DURATION = 40;

    private static final double DASH_STRENGTH = 1.2D;
    private static final int DASH_COOLDOWN = 40;

    private static final int STUN_DURATION = 40;
    private static final float STUN_CHANCE = 0.10F;

    public BoxingGlovesItem(Properties properties) {
        super(Tiers.IRON, 5, -2.2F, properties);
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

        ItemStack stack = player.getMainHandItem();

        if (!(stack.getItem() instanceof BoxingGlovesItem)) {
            resetHaste(player);
            return;
        }

        int timer = player.getPersistentData().getInt(HASTE_TIMER_TAG);

        if (timer <= 0) {
            return;
        }

        timer--;

        if (timer <= 0) {
            resetHaste(player);
            return;
        }

        player.getPersistentData().putInt(
                HASTE_TIMER_TAG,
                timer
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

        if (!(stack.getItem() instanceof BoxingGlovesItem)) {
            return;
        }

        int hits = player.getPersistentData().getInt(HASTE_HITS_TAG);

        hits++;

        if (hits > MAX_HITS) {
            hits = MAX_HITS;
        }

        player.getPersistentData().putInt(
                HASTE_HITS_TAG,
                hits
        );

        player.getPersistentData().putInt(
                HASTE_TIMER_TAG,
                HASTE_DURATION
        );

        int hasteLevel = ((hits - 1) / HITS_PER_LEVEL) + 1;

        hasteLevel = Math.min(
                hasteLevel,
                MAX_HASTE_LEVEL
        );

        player.addEffect(new MobEffectInstance(
                MobEffects.DIG_SPEED,
                HASTE_DURATION,
                hasteLevel - 1,
                false,
                false,
                true
        ));

        if (player.getRandom().nextFloat() < STUN_CHANCE) {
            event.getEntity().addEffect(
                    new MobEffectInstance(
                            ModEffects.STUN.get(),
                            STUN_DURATION,
                            0
                    )
            );

            event.getEntity().level().playSound(
                    null,
                    event.getEntity().getX(),
                    event.getEntity().getY(),
                    event.getEntity().getZ(),
                    ModSounds.PUNCH_STUN.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
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

            Vec3 horizontal = new Vec3(
                    look.x,
                    0.0D,
                    look.z
            );

            if (horizontal.lengthSqr() > 0.0001D) {
                horizontal = horizontal
                        .normalize()
                        .scale(DASH_STRENGTH);

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
                        1.0F
                );

                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(
                            new ClientboundSetEntityMotionPacket(player)
                    );
                }

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

    private static void resetHaste(Player player) {
        player.getPersistentData().remove(HASTE_HITS_TAG);
        player.getPersistentData().remove(HASTE_TIMER_TAG);

        player.removeEffect(MobEffects.DIG_SPEED);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("§6Passive: Fighting Spirit"));
        tooltip.add(Component.literal(
                "§7Successful hits build §eHaste§7."
        ));
        tooltip.add(Component.literal(
                "§7Every §e4 hits §7increases the Haste level."
        ));
        tooltip.add(Component.literal(
                "§7Maximum: §eHaste V §7at §e20 hits§7."
        ));
        tooltip.add(Component.literal(
                "§7Combo resets after §e2 seconds §7without hitting."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§cOn Hit: Discombobulate"));
        tooltip.add(Component.literal(
                "§710% chance to §cStun§7 the target for §e2 seconds§7."
        ));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§bOn Right Click: Dash"));
        tooltip.add(Component.literal("§7[>] Cooldown: 2.0 seconds"));
        tooltip.add(Component.literal("Lunge forward."));

        super.appendHoverText(stack, level, tooltip, flag);
    }
}