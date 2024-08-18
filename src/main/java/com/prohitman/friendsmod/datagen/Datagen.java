package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.core.ModBlocks;
import com.prohitman.friendsmod.core.ModEntityTypes;
import com.prohitman.friendsmod.core.ModItems;
import com.prohitman.friendsmod.loot.ModLootContextParamSets;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = FriendsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Datagen {

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(SpawnWithLootTableSubProvider::new, ModLootContextParamSets.SPAWN_WITH)), lookupProvider));
        generator.addProvider(event.includeServer(), new ModItemTags(packOutput, lookupProvider, fileHelper));

        generator.addProvider(event.includeClient(), new ModBlockStateGen(packOutput, fileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelGen(packOutput, fileHelper));
        generator.addProvider(event.includeClient(), new ModLangGen(packOutput, "en_us", languageProvider -> {
            languageProvider.add(ModBlocks.MIMIC_PLANT.get(), "Mimic Plant");
            languageProvider.add(ModItems.MIMIC_SPAWN_EGG.get(), "Mimic Spawn Egg");
            languageProvider.add(ModEntityTypes.MIMIC.get(), "Mimic");
            languageProvider.add("item.friendsmod.mimic_plant", "Mimic Plant");
        }));
    }
}
