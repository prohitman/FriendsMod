package com.prohitman.friendsmod.loot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Optional;

public class LootUtil {
    public static Optional<ItemStack> generateSingleItem(ServerLevel serverLevel, Entity entity, String poolName) {
        LootTable lootTable = getSpawnWithLootTable(serverLevel, entity);
        LootContext lootContext = createSpawnWithContext(serverLevel, entity, lootTable);
        return runOptionalPool(lootTable, lootContext, poolName).map(itemStacks -> itemStacks.get(0));
    }

    public static Optional<ItemStack> generateSingleItem(LootTable lootTable, LootContext lootContext, String poolName) {
        return runOptionalPool(lootTable, lootContext, poolName).map(itemStacks -> itemStacks.get(0));
    }

    public static Optional<List<ItemStack>> runOptionalPool(LootTable lootTable, LootContext lootContext, String poolName) {
        LootPool pool = lootTable.getPool(poolName);
        if (pool == null) {
            return Optional.empty();
        }
        List<ItemStack> stacks = new ObjectArrayList<>();
        pool.addRandomItems(stacks::add, lootContext);
        return stacks.isEmpty() ? Optional.empty() : Optional.of(stacks);
    }

    public static LootTable getSpawnWithLootTable(ServerLevel serverLevel, Entity entity) {
        var tableId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).withPrefix("spawn_with/");
        return serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, tableId));
    }

    public static LootContext createSpawnWithContext(ServerLevel serverLevel, Entity entity, LootTable lootTable) {
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .create(lootTable.getParamSet());
        return new LootContext.Builder(params).create(Optional.of(lootTable.getLootTableId()));
    }
}
