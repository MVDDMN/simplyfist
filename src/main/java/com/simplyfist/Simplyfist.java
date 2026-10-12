package com.simplyfist;

import com.mojang.logging.LogUtils;
import com.simplyfist.registry.ModEffects;
import com.simplyfist.registry.ModItems;
import com.simplyfist.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(Simplyfist.MODID)
public class Simplyfist {

    public static final String MODID = "simplyfist";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> SIMPLYFIST_TAB =
            CREATIVE_MODE_TABS.register("simplyfist_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.simplyfist"))
                    .icon(() -> new ItemStack(ModItems.BOXING_GLOVES.get()))
                    .displayItems((parameters, output) -> {
                        ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                    })
                    .build());

    public Simplyfist() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("========================================");
        LOGGER.info("SimplyFist is initializing...");
        LOGGER.info("========================================");

        ModItems.ITEMS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("SimplyFist items registered.");
        LOGGER.info("SimplyFist effects registered.");
        LOGGER.info("SimplyFist sounds registered.");
        LOGGER.info("SimplyFist creative tab registered.");
        LOGGER.info("SimplyFist initialization complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("SimplyFist server systems are ready.");
    }
}