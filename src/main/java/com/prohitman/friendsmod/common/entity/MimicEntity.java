package com.prohitman.friendsmod.common.entity;

import com.prohitman.friendsmod.common.entity.goals.PlaceBlockGoal;
import com.prohitman.friendsmod.core.ModEntityTypes;
import com.prohitman.friendsmod.loot.LootUtil;
import com.prohitman.friendsmod.vc.AudioUtils;
import com.prohitman.friendsmod.vc.EntityPlayerManager;
import com.prohitman.friendsmod.vc.FriendsVoicePlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MimicEntity extends PathfinderMob {
    private static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> HAS_PLAYER = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> HAS_NAME = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> RED_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLUE_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GREEN_DIFF = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.INT);

    public static final EntityDataAccessor<Float> RARM_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LARM_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> RLEG_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> LLEG_SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);

    public static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(MimicEntity.class, EntityDataSerializers.FLOAT);

    public void setPlayerUuid(UUID uuid){
        this.entityData.set(PLAYER_UUID, Optional.of(uuid));
    }
    public Optional<UUID> getPlayerUuid(){
        return this.entityData.get(PLAYER_UUID);
    }

    public void setHasPlayer(boolean hasPlayer){
        this.entityData.set(HAS_PLAYER, hasPlayer);
    }
    public boolean getHasPlayer(){
        return this.entityData.get(HAS_PLAYER);
    }

    public void setMimicName(String name){
        this.entityData.set(NAME, name);
    }
    public String getMimicName(){
        return this.entityData.get(NAME);
    }

    public void setHasName(boolean hasName){
        this.entityData.set(HAS_NAME, hasName);
    }
    public boolean getHasName(){
        return this.entityData.get(HAS_NAME);
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

    public void setModelScale(float scale){
        this.entityData.set(SCALE, scale);
    }
    public float getModelScale(){
        return this.entityData.get(SCALE);
    }

    public static final Predicate<LivingEntity> MIMIC_TARGETS = (livingEntity -> {
       return livingEntity.getType().getCategory().isFriendly() && !(livingEntity instanceof NeutralMob) && !(livingEntity instanceof WaterAnimal);
    });

    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;

    public float oBob;
    public float bob;

    public List<short[]> currentSound = new ArrayList<>();
    public UUID currentChannel;
    public UUID fromPlayer;
    public VoicechatServerApi vcApi;
    public boolean canResetSound = true;

    public void setCurrentSound(List<short[]> newSound){
        currentSound = newSound;
    }

    public List<short[]> getCurrentSound(){
        return this.currentSound;
    }

    public MimicEntity(Level level) {
        super(ModEntityTypes.MIMIC.get(), level);
        this.xpReward = 5;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3f, false));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1));
        this.goalSelector.addGoal(8, new PlaceBlockGoal(this));

        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, true, MIMIC_TARGETS));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.generateColors();
        this.generateLimbScales();
        this.setModelScale(Mth.nextFloat(level.getRandom(), 0.85f, 1.05f));
        this.setMimicName(garbleName(this.getName().getString(), this.getRandom()));

        LootTable lootTable = LootUtil.getSpawnWithLootTable(level.getLevel(), this);
        LootContext lootContext = LootUtil.createSpawnWithContext(level.getLevel(), this, lootTable);
        LootUtil.generateSingleItem(lootTable, lootContext, EquipmentSlot.MAINHAND.getName()).ifPresent(itemStack -> setItemSlot(EquipmentSlot.MAINHAND, itemStack));

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    public static String garbleName(String name, RandomSource random) {
        StringBuilder garbledName = new StringBuilder(name);

        for (int i = 0; i < garbledName.length(); i++) {
            if (random.nextInt(5) == 0) {
                char newChar = (char) ('a' + random.nextInt(26));
                garbledName.setCharAt(i, newChar);
            }
        }

        if(garbledName.toString().equals(name)){
            int i = random.nextInt(name.length());
            char newChar = (char) ('a' + random.nextInt(26));
            garbledName.setCharAt(i, newChar);
        }

        return garbledName.toString();
    }

    @Override
    public void tick() {
        super.tick();
        this.moveCloak();
    }

    private void moveCloak() {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double d0 = this.getX() - this.xCloak;
        double d1 = this.getY() - this.yCloak;
        double d2 = this.getZ() - this.zCloak;
        double d3 = 10.0;
        if (d0 > 10.0) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 > 10.0) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 > 10.0) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        if (d0 < -10.0) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (d2 < -10.0) {
            this.zCloak = this.getZ();
            this.zCloakO = this.zCloak;
        }

        if (d1 < -10.0) {
            this.yCloak = this.getY();
            this.yCloakO = this.yCloak;
        }

        this.xCloak += d0 * 0.25;
        this.zCloak += d2 * 0.25;
        this.yCloak += d1 * 0.25;
    }

    @Override
    public void aiStep() {
        updateSwingTime();
        this.oBob = this.bob;

        super.aiStep();
        float f;
        if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
            f = Math.min(0.1F, (float)this.getDeltaMovement().horizontalDistance());
        } else {
            f = 0.0F;
        }
        this.bob = this.bob + (f - this.bob) * 0.4F;

        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(25.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        if(!players.isEmpty()){
            for(Player player : players){
                Vec3 vec3 = DefaultRandomPos.getPosAway(this, 16, 7, player.position());
                if (vec3 != null && player.distanceToSqr(vec3.x, vec3.y, vec3.z) >= player.distanceToSqr(this)) {
                    this.setAggressive(false);
                    this.setTarget(null);
                    //if(this.tickCount % 5 == 0){
                        Path path = this.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
                        if(path != null){
                            this.getNavigation().moveTo(path, 1.55);
                        }
                    //}
                }
            }
        }

        if(this.tickCount % 80 == 0 && FriendsVoicePlugin.voiceApi != null && level() instanceof ServerLevel serverLevel /*&& !level().isClientSide*/){
            if(currentChannel != null && EntityPlayerManager.instance().isPlaying(currentChannel)){
                EntityPlayerManager.instance().stop(currentChannel);
            }

            System.out.println("Playing sooound for: " + this.getUUID());
            UUID channelId = EntityPlayerManager.instance().playEntitySound(
                    FriendsVoicePlugin.voiceApi,
                    serverLevel,
                    this,
                    32f,
                    FriendsVoicePlugin.MIMICING
            );

            if(channelId != null){
                this.currentChannel = channelId;
            }
        }
    }

    public void generateColors(){
        this.setRedDiff(this.random.nextInt(5, 50));
        this.setGreenDiff(this.random.nextInt(5, 50));
        this.setBlueDiff(this.random.nextInt(5, 50));

        if(this.getRedDiff() >= this.getGreenDiff() && this.getRedDiff() >= this.getBlueDiff()){
            this.setGreenDiff(this.getRedDiff());
            this.setBlueDiff(this.getRedDiff());
            this.setRedDiff(0);
        } else if(this.getBlueDiff() >= this.getGreenDiff() && this.getBlueDiff() >= this.getRedDiff()){
            this.setGreenDiff(this.getBlueDiff());
            this.setRedDiff(this.getBlueDiff());
            this.setBlueDiff(0);
        } else if(this.getGreenDiff() >= this.getRedDiff() && this.getGreenDiff() >= this.getBlueDiff()){
            this.setRedDiff(this.getGreenDiff());
            this.setBlueDiff(this.getGreenDiff());
            this.setGreenDiff(0);
        }
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
        builder.define(SCALE, 1f);
        builder.define(HAS_PLAYER, false);
        builder.define(NAME, "");
        builder.define(HAS_NAME, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setModelScale(compound.getFloat("scale"));

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

        this.setHasPlayer(compound.getBoolean("has_player"));
        this.setMimicName(compound.getString("mimic_name"));
        this.setHasName(compound.getBoolean("has_mimic_name"));

        int[] currentSound = compound.getIntArray("current_mimic_sound");
        short[] currentShortSound = convertIntArrayToShortArray(currentSound);

        this.setCurrentSound(List.of(currentShortSound));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("scale", this.getModelScale());

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

        compound.putBoolean("has_player", this.getHasPlayer());
        compound.putString("mimic_name", this.getMimicName());
        compound.putBoolean("has_mimic_name", this.getHasName());

        this.setCurrentSound(List.of(AudioUtils.concatenateShortArrays(this.getCurrentSound())));
        int[] currentSound = convertShortArrayToIntArray(this.getCurrentSound().getFirst());

        compound.putIntArray("current_mimic_sound", currentSound);
    }

    public static int[] convertShortArrayToIntArray(short[] shortArray) {
        int[] intArray = new int[shortArray.length];
        for (int i = 0; i < shortArray.length; i++) {
            intArray[i] = shortArray[i];
        }
        return intArray;
    }

    public static short[] convertIntArrayToShortArray(int[] intArray) {
        short[] shortArray = new short[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            if (intArray[i] < Short.MIN_VALUE || intArray[i] > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Value " + intArray[i] + " at index " + i + " is out of short range.");
            }
            shortArray[i] = (short) intArray[i];
        }
        return shortArray;
    }

    @Override
    public float getScale() {
        return this.getModelScale();
    }

    @Override
    public boolean isLeashed() {
        return false;
    }

    @Override
    public float maxUpStep() {
        return this.getModelScale() > 1 ? 1 : super.maxUpStep();
    }

    @Override
    public void checkDespawn() {
        super.checkDespawn();
    }
}
