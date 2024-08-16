package com.prohitman.friendsmod.common.entity;

import com.prohitman.friendsmod.core.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class MimicEntity extends Mob {
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public void setPlayerUuid(UUID uuid){
        this.entityData.set(PLAYER_UUID, Optional.of(uuid));
    }

    public Optional<UUID> getPlayerUuid(){
        return this.entityData.get(PLAYER_UUID);
    }

    public MimicEntity(Level level) {
        super(ModEntityTypes.MIMIC.get(), level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYER_UUID, Optional.of(UUID.randomUUID()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(this.getPlayerUuid().isPresent()){
            this.setPlayerUuid(compound.getUUID("player_uuid"));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if(this.getPlayerUuid().isPresent()){
            compound.putUUID("player_uuid", this.getPlayerUuid().get());
        }
    }
}
