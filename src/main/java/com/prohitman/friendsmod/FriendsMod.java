package com.prohitman.friendsmod;

import com.prohitman.friendsmod.common.entity.MimicEntity;
import com.prohitman.friendsmod.core.ModBlockEntities;
import com.prohitman.friendsmod.core.ModBlocks;
import com.prohitman.friendsmod.core.ModEntityTypes;
import com.prohitman.friendsmod.core.ModItems;
import com.prohitman.friendsmod.loot.ModLootContextParamSets;
import com.prohitman.friendsmod.loot.ModLootPoolEntryTypes;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(FriendsMod.MODID)
public class FriendsMod
{
    public static final String MODID = "friendsmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public FriendsMod(IEventBus modEventBus, ModContainer modContainer)
    {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntityTypes.ENTITY_TYPES.register(modEventBus);
        ModLootPoolEntryTypes.ENTRIES.register(modEventBus);

        ModLootContextParamSets.bootstrap();

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::addDefaultAttributes);

        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.MIMIC.get(), MimicEntity.createAttributes().build());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){

        }
            //event.accept(EXAMPLE_BLOCK_ITEM);
    }
}
