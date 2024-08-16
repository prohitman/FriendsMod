package com.prohitman.friendsmod.core;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.block.MimicPlant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FriendsMod.MODID);

    public static final DeferredBlock<?> MIMIC_PLANT = createRegistry("mimic_plant", () -> new MimicPlant(BlockBehaviour.Properties.of()
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .mapColor(MapColor.PLANT)
            .randomTicks()
            .noCollission()
            .sound(SoundType.SWEET_BERRY_BUSH)
            .pushReaction(PushReaction.DESTROY)), new Item.Properties());

    public static <T extends Block> DeferredBlock<?> createRegistry(String name, Supplier<T> block, Item.Properties properties) {
        DeferredBlock<?> object = BLOCKS.register(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(object.get(), properties));

        return object;
    }
}