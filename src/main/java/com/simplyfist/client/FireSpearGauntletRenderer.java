package com.simplyfist.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simplyfist.item.FireSpearGauntletItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "simplyfist",
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class FireSpearGauntletRenderer {

    private static final int MAX_CHARGE_TIME = 40;
    private static final float MAX_PULLBACK = 0.3F;
    private static final float INITIAL_RIGHT_OFFSET = 0.4F;
    private static final float ADDITIONAL_RIGHT_OFFSET = 0.1F;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (!player.isUsingItem()) {
            return;
        }

        ItemStack stack = player.getUseItem();

        if (!(stack.getItem() instanceof FireSpearGauntletItem)) {
            return;
        }

        int remainingTicks = player.getUseItemRemainingTicks();

        float progress = 1.0F -
                ((float) remainingTicks / 72000.0F);

        progress = Math.min(
                progress * 72000.0F / MAX_CHARGE_TIME,
                1.0F
        );

        progress = progress * progress;

        float pullback =
                progress * MAX_PULLBACK;

        float rightOffset =
                INITIAL_RIGHT_OFFSET
                        + (progress * ADDITIONAL_RIGHT_OFFSET);

        PoseStack poseStack = event.getPoseStack();

        poseStack.translate(
                rightOffset,
                0.0D,
                pullback
        );
    }
}