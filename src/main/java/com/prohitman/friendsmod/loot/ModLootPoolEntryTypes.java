package com.prohitman.friendsmod.loot;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.loot.entries.AnyItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModLootPoolEntryTypes {
    public static final DeferredRegister<LootPoolEntryType> ENTRIES = DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, FriendsMod.MODID);

    public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> ANY_ITEM = ENTRIES.register("any_item", () -> new LootPoolEntryType(AnyItemEntry.CODEC));
}
