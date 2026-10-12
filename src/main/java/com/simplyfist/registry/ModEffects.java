package com.simplyfist.registry;

import com.simplyfist.Simplyfist;
import com.simplyfist.effect.BleedingEffect;
import com.simplyfist.effect.RisingStrikeEffect;
import com.simplyfist.effect.StunEffect;
import com.simplyfist.effect.VampirismEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Simplyfist.MODID);

    public static final RegistryObject<MobEffect> STUN =
            EFFECTS.register("stun", StunEffect::new);

    public static final RegistryObject<MobEffect> BLEEDING =
            EFFECTS.register("bleeding", BleedingEffect::new);

    public static final RegistryObject<MobEffect> VAMPIRISM =
            EFFECTS.register("vampirism", VampirismEffect::new);

    public static final RegistryObject<MobEffect> RISING_STRIKE =
            EFFECTS.register("rising_strike", RisingStrikeEffect::new);
}