package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.core.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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

/*        CompletableFuture<TagsProvider.TagLookup<Block>> tagLookup = generator.addProvider(event.includeServer(), new ModBlockTagGen(packOutput, lookupProvider, fileHelper)).contentsGetter();
        generator.addProvider(event.includeServer(), new ModItemTagGen(packOutput, lookupProvider, tagLookup, fileHelper));
        List<LootTableProvider.SubProviderEntry> entries = List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootGen::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModPrizeLootGen::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(ModInjectedLootGen::new, LootContextParamSets.CHEST)
        );*/
        //generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Set.of(), entries, lookupProvider));
        //generator.addProvider(event.includeServer(), new ModGlobalLootModifierGen(packOutput, lookupProvider));

        //generator.addProvider(event.includeClient(), new ModBlockStateGen(packOutput, fileHelper));
        //generator.addProvider(event.includeClient(), new ModItemModelGen(packOutput, fileHelper));
        generator.addProvider(event.includeClient(), new ModLangGen(packOutput, "en_us", languageProvider -> {
            languageProvider.add(ModBlocks.MIMIC_PLANT.get(), "Mimic Plant");
            languageProvider.add("item.friendsmod.mimic_plant", "Mimic Plant");
        }));
    }
}
