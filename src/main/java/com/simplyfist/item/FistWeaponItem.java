package com.simplyfist.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class FistWeaponItem extends SwordItem {

    public FistWeaponItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }
}