package com.prohitman.friendsmod.datagen;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.block.MimicPlant;
import com.prohitman.friendsmod.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateGen extends BlockStateProvider {
    public ModBlockStateGen(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, FriendsMod.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.createBerryBush();
    }

    private void createBerryBush(){
        getVariantBuilder(ModBlocks.MIMIC_PLANT.get()).forAllStatesExcept(state -> {
            ModelFile model = models().cross(ModBlocks.MIMIC_PLANT.getId().getPath() + "_stage" + 0,
                    modLoc("block/" + ModBlocks.MIMIC_PLANT.getId().getPath() + "_stage" + 0)).renderType("cutout_mipped");
            int i;
            for(i=0; i<4; i++){
                if(state.getValue(MimicPlant.AGE) == i){
                    model = models().cross(ModBlocks.MIMIC_PLANT.getId().getPath() + "_stage" + i,
                            modLoc("block/" + ModBlocks.MIMIC_PLANT.getId().getPath() + "_stage" + i)).renderType("cutout_mipped");
                }
            }

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .uvLock(true)
                    .build();
        });
    }
}
