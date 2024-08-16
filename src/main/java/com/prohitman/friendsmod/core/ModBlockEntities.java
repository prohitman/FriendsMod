package com.prohitman.friendsmod.core;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.block.entity.MimicPlantBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FriendsMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MimicPlantBlockEntity>> MIMIC_PLANT_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("mimic_plant_be", () -> BlockEntityType.Builder.of(MimicPlantBlockEntity::new, ModBlocks.MIMIC_PLANT.get()).build(null));

}
