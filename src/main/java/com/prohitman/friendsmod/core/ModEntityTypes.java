package com.prohitman.friendsmod.core;

import com.prohitman.friendsmod.FriendsMod;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, FriendsMod.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<MimicEntity>> MIMIC = ENTITY_TYPES.register("mimic",
            () -> EntityType.Builder.of((EntityType<MimicEntity> entityType, Level level) -> new MimicEntity(level), MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).clientTrackingRange(10).updateInterval(2).build(""));
}
