package com.prohitman.friendsmod.common.block.entity;

import com.mojang.logging.LogUtils;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import com.prohitman.friendsmod.core.ModBlockEntities;
import com.prohitman.friendsmod.loot.LootUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class MimicPlantBlockEntity extends BlockEntity {
    public boolean hasPlayer = false;
    @Nullable
    private ResolvableProfile owner;
    private static final Logger LOGGER = LogUtils.getLogger();

    public MimicPlantBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MIMIC_PLANT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tickServer(){
        assert level != null;

        BlockState state = this.getBlockState();

        if(state.getValue(BlockStateProperties.AGE_4) == 4 && !this.isRemoved()
                && this.level.random.nextInt(10) == 0 && this.owner != null
                && !this.level.isClientSide){
            MimicEntity mimic = new MimicEntity(level);

            mimic.setPlayerUuid(this.getOwner().id().get());
            mimic.setPos(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
            mimic.generateColors();
            mimic.generateLimbScales();
            mimic.setModelScale(Mth.nextFloat(level.getRandom(), 0.85f, 1.05f));
            mimic.setHasPlayer(true);
            mimic.setMimicName(MimicEntity.garbleName(this.getOwner().name().get(), mimic.getRandom()));

            LootTable lootTable = LootUtil.getSpawnWithLootTable((ServerLevel) level, mimic);
            LootContext lootContext = LootUtil.createSpawnWithContext((ServerLevel)level, mimic, lootTable);
            LootUtil.generateSingleItem(lootTable, lootContext, EquipmentSlot.MAINHAND.getName()).ifPresent(itemStack -> mimic.setItemSlot(EquipmentSlot.MAINHAND, itemStack));

            level.addFreshEntity(mimic);
            level.destroyBlock(this.getBlockPos(), false);
        }
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
        }
        tag.putBoolean("has_player", this.hasPlayer);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("profile")) {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile")).resultOrPartial((p_332637_) -> {
                LOGGER.error("Failed to load profile from player: {}", p_332637_);
            }).ifPresent(this::setOwner);
        }

        this.hasPlayer = tag.getBoolean("has_player");
    }

    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public void setOwner(@Nullable ResolvableProfile owner) {
        synchronized(this) {
            this.owner = owner;
        }
    }

    public @Nullable ResolvableProfile getOwner() {
        return owner;
    }


    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.setOwner(componentInput.get(DataComponents.PROFILE));
    }

    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.PROFILE, this.owner);
    }

    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("profile");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
