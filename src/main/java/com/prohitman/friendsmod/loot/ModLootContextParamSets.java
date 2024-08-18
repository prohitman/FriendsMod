package com.prohitman.friendsmod.loot;

import com.prohitman.friendsmod.FriendsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.function.Consumer;

public class ModLootContextParamSets {
    public static final LootContextParamSet SPAWN_WITH = register("entity_spawn_with", builder ->
            builder.required(LootContextParams.ORIGIN).required(LootContextParams.THIS_ENTITY)
    );

    private static LootContextParamSet register(String registryName, Consumer<LootContextParamSet.Builder> builderConsumer) {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        builderConsumer.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(FriendsMod.MODID, registryName);
        LootContextParamSet lootcontextparamset1 = LootContextParamSets.REGISTRY.put(resourcelocation, lootcontextparamset);
        if (lootcontextparamset1 != null) {
            throw new IllegalStateException("Loot table parameter set " + resourcelocation + " is already registered");
        } else {
            return lootcontextparamset;
        }
    }

    public static void bootstrap() {}
}
