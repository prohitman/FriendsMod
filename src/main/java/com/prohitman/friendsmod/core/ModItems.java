package com.prohitman.friendsmod.core;

import com.prohitman.friendsmod.FriendsMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FriendsMod.MODID);

    public static final DeferredItem<Item> MIMIC_SPAWN_EGG = ITEMS.register("mimic_spawn_egg", () -> new DeferredSpawnEggItem(ModEntityTypes.MIMIC, 0xA0A0A0, 0x000000, new Item.Properties()));
}
