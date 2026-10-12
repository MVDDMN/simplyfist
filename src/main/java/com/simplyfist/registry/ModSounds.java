package com.simplyfist.registry;

import com.simplyfist.Simplyfist;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

        public static final DeferredRegister<SoundEvent> SOUNDS =
                DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Simplyfist.MODID);

        public static final RegistryObject<SoundEvent> PUNCH_HIT =
                SOUNDS.register("punch-hit", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "punch-hit")
                        ));

        public static final RegistryObject<SoundEvent> PUNCH_UP =
                SOUNDS.register("punch-up", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "punch-up")
                        ));

        public static final RegistryObject<SoundEvent> PUNCH_STUN =
                SOUNDS.register("punch-stun", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "punch-stun")
                        ));

        public static final RegistryObject<SoundEvent> DASH =
                SOUNDS.register("dash", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "dash")
                        ));

        public static final RegistryObject<SoundEvent> RISING_STRIKE =
                SOUNDS.register("rising-strike", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "rising-strike")
                        ));

        public static final RegistryObject<SoundEvent> SUPERMAN_PUNCH =
                SOUNDS.register("superman-punch", () ->
                        SoundEvent.createVariableRangeEvent(
                                ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "superman-punch")
                        ));
                        
        public static final RegistryObject<SoundEvent> CHARGING = SOUNDS.register("charging", () ->
                SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(Simplyfist.MODID, "charging")
                ));
}