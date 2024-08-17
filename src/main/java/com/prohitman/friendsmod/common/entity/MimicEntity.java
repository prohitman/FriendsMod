package com.prohitman.friendsmod.common.entity;

import com.prohitman.friendsmod.core.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MimicEntity extends Mob {
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> RED_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLUE_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GREEN_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Float> RARM_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LARM_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> RLEG_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LLEG_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);

    public void setPlayerUuid(UUID uuid){
        this.entityData.set(PLAYER_UUID, Optional.of(uuid));
    }

    public Optional<UUID> getPlayerUuid(){
        return this.entityData.get(PLAYER_UUID);
    }

    public void setRedDiff(int red){
        this.entityData.set(RED_DIFF, red);
    }
    public int getRedDiff(){
        return this.entityData.get(RED_DIFF);
    }

    public void setGreenDiff(int green){
        this.entityData.set(GREEN_DIFF, green);
    }
    public int getGreenDiff(){
        return this.entityData.get(GREEN_DIFF);
    }

    public void setBlueDiff(int blue){
        this.entityData.set(BLUE_DIFF, blue);
    }
    public int getBlueDiff(){
        return this.entityData.get(BLUE_DIFF);
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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.generateColors();
        this.generateLimbScales();
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public void generateColors(){
        this.setRedDiff(this.random.nextInt(0, 55));
        this.setGreenDiff(this.random.nextInt(0, 55));
        this.setBlueDiff(this.random.nextInt(0, 55));
    }

    public void generateLimbScales(){
        this.entityData.set(RARM_SCALE, (this.random.nextBoolean() ? -1 : 1) * Mth.nextFloat(this.random, 0, 0.25f));
        this.entityData.set(LARM_SCALE, (this.random.nextBoolean() ? -1 : 1) * Mth.nextFloat(this.random, 0, 0.25f));
        this.entityData.set(RLEG_SCALE, (this.random.nextBoolean() ? -1 : 1) * Mth.nextFloat(this.random, 0, 0.25f));
        this.entityData.set(LLEG_SCALE, (this.random.nextBoolean() ? -1 : 1) * Mth.nextFloat(this.random, 0, 0.25f));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PLAYER_UUID, Optional.of(UUID.randomUUID()));
        builder.define(RED_DIFF, 0);
        builder.define(GREEN_DIFF, 0);
        builder.define(BLUE_DIFF, 0);
        builder.define(RARM_SCALE, 0f);
        builder.define(LARM_SCALE, 0f);
        builder.define(RLEG_SCALE, 0f);
        builder.define(LLEG_SCALE, 0f);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setRedDiff(compound.getInt("red_diff"));
        this.setGreenDiff(compound.getInt("green_diff"));
        this.setBlueDiff(compound.getInt("blue_diff"));

        this.entityData.set(RARM_SCALE, compound.getFloat("rarm_scale"));
        this.entityData.set(LARM_SCALE, compound.getFloat("larm_scale"));
        this.entityData.set(RLEG_SCALE, compound.getFloat("rleg_scale"));
        this.entityData.set(LLEG_SCALE, compound.getFloat("lleg_scale"));

        if(this.getPlayerUuid().isPresent()){
            this.setPlayerUuid(compound.getUUID("player_uuid"));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("red_diff", this.getRedDiff());
        compound.putInt("green_diff", this.getGreenDiff());
        compound.putInt("blue_diff", this.getBlueDiff());

        compound.putFloat("rarm_scale", this.entityData.get(RARM_SCALE));
        compound.putFloat("larm_scale", this.entityData.get(LARM_SCALE));
        compound.putFloat("rleg_scale", this.entityData.get(RLEG_SCALE));
        compound.putFloat("lleg_scale", this.entityData.get(LLEG_SCALE));

        if(this.getPlayerUuid().isPresent()){
            compound.putUUID("player_uuid", this.getPlayerUuid().get());
        }
    }

    @Override
    public boolean isLeashed() {
        return false;
    }
}
