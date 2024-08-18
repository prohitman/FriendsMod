package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.core.ModBlocks;
import com.prohitman.friendsmod.core.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModItemModelGen extends ItemModelProvider {
    public ModItemModelGen(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, FriendsMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        spawnEgg(ModItems.MIMIC_SPAWN_EGG);

        singleTexture((ModBlocks.MIMIC_PLANT.getId().getPath()),
                mcLoc("item/generated"),
                "layer0", modLoc("block/mimic_plant_stage2"));
    }

    protected ItemModelBuilder spawnEgg(Supplier<Item> item) {
        String path = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
        return withExistingParent(path, mcLoc("item/template_spawn_egg"));
    }
}
