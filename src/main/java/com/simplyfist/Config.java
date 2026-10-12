package com.simplyfist;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SPEC = builder.build();
    }
}