package com.prohitman.friendsmod;

import com.mojang.logging.LogUtils;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import com.prohitman.friendsmod.core.ModBlockEntities;
import com.prohitman.friendsmod.core.ModBlocks;
import com.prohitman.friendsmod.core.ModEntityTypes;
import com.prohitman.friendsmod.core.ModItems;
import com.prohitman.friendsmod.loot.ModLootContextParamSets;
import com.prohitman.friendsmod.loot.ModLootPoolEntryTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

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

        modEventBus.addListener(this::addDefaultAttributes);

        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.MIMIC.get(), MimicEntity.createAttributes().build());
    }
}
