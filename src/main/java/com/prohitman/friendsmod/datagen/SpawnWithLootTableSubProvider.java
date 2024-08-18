package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.loot.ModLootContextParamSets;
import com.prohitman.friendsmod.loot.entries.AnyItemEntry;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

import java.util.function.BiConsumer;

public class SpawnWithLootTableSubProvider implements LootTableSubProvider {
    public static final ResourceLocation RANDOM_MIMIC_ITEMS = spawnWithId("common/random_mimic_items");
    public static final ResourceLocation RANDOM_MIMIC_BLOCK_ITEMS = spawnWithId("common/random_mimic_block_items");
    protected final HolderLookup.Provider registries;

    public SpawnWithLootTableSubProvider(HolderLookup.Provider registries){
        this.registries = registries;
    }
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, RANDOM_MIMIC_ITEMS),
                LootTable.lootTable().setParamSet(ModLootContextParamSets.SPAWN_WITH).withPool(LootPool.lootPool().name("base")
                        .add(AnyItemEntry.builder()
                                .filter(ItemPredicate.Builder.item().of(ModItemTags.MIMIC_ITEMS).build())
                                .setWeight(2)
                        )
                        .add(AnyItemEntry.builder()
                                .filter(ItemPredicate.Builder.item().of(ModItemTags.WOODEN_TOOLS).build())
                                .setWeight(3))
                )
        );

        biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, RANDOM_MIMIC_BLOCK_ITEMS),
                LootTable.lootTable().setParamSet(ModLootContextParamSets.SPAWN_WITH).withPool(LootPool.lootPool().name("base")
                        .add(AnyItemEntry.builder()
                                .filter(ItemPredicate.Builder.item().of(ModItemTags.MIMIC_BLOCK_ITEMS).build())
                                .setWeight(1)
                        )
                )
        );

        biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, spawnWithId("mimic")), mimic());
        biConsumer.accept(ResourceKey.create(Registries.LOOT_TABLE, spawnWithId("mimic_placeable")), mimicPlaceables());


    }
    protected static ResourceLocation spawnWithId(String id) {
        return ResourceLocation.fromNamespaceAndPath(FriendsMod.MODID,"spawn_with/" + id);
    }

    protected LootTable.Builder mimic() {
        return LootTable.lootTable().setParamSet(ModLootContextParamSets.SPAWN_WITH).withPool(LootPool.lootPool().name("mainhand")
                .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, RANDOM_MIMIC_ITEMS)))
                .add(EmptyLootItem.emptyItem())
        );
    }

    protected LootTable.Builder mimicPlaceables() {
        return LootTable.lootTable().setParamSet(ModLootContextParamSets.SPAWN_WITH).withPool(LootPool.lootPool().name("mimic_placeable")
                .add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, RANDOM_MIMIC_BLOCK_ITEMS)))
                .add(EmptyLootItem.emptyItem())
        );
    }

}
