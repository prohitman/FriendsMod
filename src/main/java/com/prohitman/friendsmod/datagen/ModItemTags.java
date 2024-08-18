package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTags extends ItemTagsProvider {
    public static final TagKey<Item> MIMIC_ITEMS = create("mimic_items");
    public static final TagKey<Item> MIMIC_BLOCK_ITEMS = create("mimic_block_items");

    public ModItemTags(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, CompletableFuture.completedFuture(TagLookup.empty()), FriendsMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(MIMIC_ITEMS).addTags(MIMIC_BLOCK_ITEMS)
                .addTags(ItemTags.MEAT)
                .addTags(ItemTags.FISHES)
                .add(Items.FEATHER)
                .add(Items.LEATHER)
                .add(Items.STICK)
                .add(Items.WHEAT_SEEDS)
                .add(Items.DEAD_BUSH)
                .addTags(ItemTags.FLOWERS)
                .add(Items.WOODEN_AXE)
                .add(Items.WOODEN_PICKAXE)
                .add(Items.WOODEN_HOE)
                .add(Items.WOODEN_SHOVEL)
                .add(Items.WOODEN_SWORD);

        tag(MIMIC_BLOCK_ITEMS).addTags(ItemTags.OAK_LOGS)
                .addTags(ItemTags.DIRT)
                .add(Items.GRAVEL)
                .add(Items.SAND)
                .add(Items.COBBLESTONE)
                .add(Items.CRAFTING_TABLE);

    }

    private static TagKey<Item> create(String tag) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(FriendsMod.MODID, tag));
    }

    private static TagKey<Item> createNeoForgeTag(String tag) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", tag));
    }

    private static TagKey<Item> createCommonTag(String tag) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", tag));
    }
}
