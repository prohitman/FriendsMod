package com.prohitman.friendsmod.common.block.entity;

import com.mojang.logging.LogUtils;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import com.prohitman.friendsmod.core.ModBlockEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.internal.objects.annotations.Getter;
import org.slf4j.Logger;

import java.util.concurrent.Executor;

public class MimicPlantBlockEntity extends BlockEntity {
    @Nullable
    private ResolvableProfile owner;
    @Nullable
    private static Executor mainThreadExecutor;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Executor CHECKED_MAIN_THREAD_EXECUTOR = (p_294078_) -> {
        Executor executor = mainThreadExecutor;
        if (executor != null) {
            executor.execute(p_294078_);
        }
    };

    public static void setup(Executor executor){
        mainThreadExecutor = Minecraft.getInstance();
    }


    public MimicPlantBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MIMIC_PLANT_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void tickServer(){
        assert level != null;

        if(this.owner != null && this.level.random.nextInt(5) == 0){
            System.out.println(this.owner.name());
        }

        BlockState state = this.getBlockState();

        if(state.getValue(BlockStateProperties.AGE_4) == 4 && !this.isRemoved()
                && this.level.random.nextInt(10) == 0 && this.owner != null
                && !this.level.isClientSide){
            MimicEntity mimic = new MimicEntity(level);
            mimic.setPlayerUuid(this.getOwner().id().get());
            mimic.setPos(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
            mimic.generateColors();
            level.addFreshEntity(mimic);
            level.destroyBlock(this.getBlockPos(), false);
        }
    }

    public static void clear() {
        mainThreadExecutor = null;
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.put("profile", ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, this.owner).getOrThrow());
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("profile")) {
            ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, tag.get("profile")).resultOrPartial((p_332637_) -> {
                LOGGER.error("Failed to load profile from player: {}", p_332637_);
            }).ifPresent(this::setOwner);
        }
    }

    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public void setOwner(@Nullable ResolvableProfile owner) {
        synchronized(this) {
            this.owner = owner;
        }

        //this.updateOwnerProfile();
    }

    public @Nullable ResolvableProfile getOwner() {
        return owner;
    }

    /*    private void updateOwnerProfile() {
        if (this.owner != null && !this.owner.isResolved()) {
            this.owner.resolve().thenAcceptAsync((p_332638_) -> {
                this.owner = p_332638_;
                this.setChanged();
            }, CHECKED_MAIN_THREAD_EXECUTOR);
        } else {
            this.setChanged();
        }

    }*/

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
