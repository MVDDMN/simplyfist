package com.simplyfist.registry;

import com.simplyfist.Simplyfist;
import com.simplyfist.item.BloodyKnucklesItem;
import com.simplyfist.item.BoxingGlovesItem;
import com.simplyfist.item.MishimaGauntletsItem;
import com.simplyfist.item.ThunderSpearGauntletItem;
import com.simplyfist.item.FireSpearGauntletItem;
import com.simplyfist.item.SteraksGageItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Simplyfist.MODID);

    public static final RegistryObject<Item> BOXING_GLOVES =
            ITEMS.register(
                    "boxing_gloves",
                    () -> new BoxingGlovesItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );

    public static final RegistryObject<Item> BLOODY_KNUCKLES =
            ITEMS.register(
                    "bloody_knuckles",
                    () -> new BloodyKnucklesItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );

    public static final RegistryObject<Item> MISHIMA_GAUNTLETS =
            ITEMS.register(
                    "mishima_gauntlet",
                    () -> new MishimaGauntletsItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );

    public static final RegistryObject<Item> THUNDER_SPEAR_GAUNTLET =
            ITEMS.register(
                    "thunder_spear_gauntlet",
                    () -> new ThunderSpearGauntletItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );

    public static final RegistryObject<Item> FIRE_SPEAR_GAUNTLET =
            ITEMS.register(
                    "fire_spear_gauntlet",
                    () -> new FireSpearGauntletItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );

    public static final RegistryObject<Item> STERAKS_GAGE =
            ITEMS.register(
                    "steraks_gage",
                    () -> new SteraksGageItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .durability(1562)
                    )
            );
}