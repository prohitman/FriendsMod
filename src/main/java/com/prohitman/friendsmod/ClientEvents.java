package com.prohitman.friendsmod;

import com.prohitman.friendsmod.client.MimicRenderer;
import com.prohitman.friendsmod.core.ModBlocks;
import com.prohitman.friendsmod.core.ModEntityTypes;
import com.prohitman.friendsmod.core.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = FriendsMod.MODID, bus =EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntityTypes.MIMIC.get(), MimicRenderer::new);
        });
    }

    @SubscribeEvent
    private static void addToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.MIMIC_SPAWN_EGG);
        } else if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS){
            event.accept(ModBlocks.MIMIC_PLANT);
        }
    }
}
