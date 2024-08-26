package com.prohitman.friendsmod.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.prohitman.friendsmod.client.layers.MimicCapeLayer;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PeriodicNotificationManager;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.PlayerHeadBlockProfileFix;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MimicRenderer<T extends MimicEntity> extends HumanoidMobRenderer<T, PlayerModel<T>> {
    private static Deferred wide;
    private static Deferred slim;

    public MimicRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        setupIfNeeded(pContext);
    }

    private static void setupIfNeeded(EntityRendererProvider.Context pContext) {
        if (wide == null || slim == null) {
            wide = new Deferred(pContext, PlayerSkin.Model.WIDE);
            slim = new Deferred(pContext, PlayerSkin.Model.SLIM);
        }
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        PlayerSkin playerSkin;
        if(!pEntity.getHasPlayer()){
            playerSkin = DefaultPlayerSkin.get(pEntity.getUUID());
        } else {
            var skin = Deferred.cache.computeIfAbsent(
                    pEntity.getPlayerUuid().get(), key -> CompletableFuture.supplyAsync(() -> Optional.ofNullable(
                            Minecraft.getInstance().getMinecraftSessionService().fetchProfile(key, false)
                    ), Util.ioPool()).thenCompose(o -> o
                            .map(p -> Minecraft.getInstance().getSkinManager().getOrLoad(p.profile()))
                            .orElse(CompletableFuture.completedFuture(null))
                    )
            ).getNow(null);
            playerSkin = Objects.requireNonNullElseGet(skin,
                    () -> DefaultPlayerSkin.get(pEntity.getPlayerUuid().get()));
        }

        if(playerSkin.model() == PlayerSkin.Model.SLIM){
            slim.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        } else {
            wide.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }

        renderNameTag(pEntity, Component.literal(pEntity.getMimicName()), pPoseStack, pBuffer, pPackedLight, pPartialTicks);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MimicEntity pEntity) {
        return DefaultPlayerSkin.get(pEntity.getPlayerUuid().get()).texture();
    }

    public static class Deferred extends HumanoidMobRenderer<MimicEntity, PlayerModel<MimicEntity>> {
        protected final PlayerSkin.Model modelType;
        public static final Map<UUID, CompletableFuture<PlayerSkin>> cache = new HashMap<>();

        public Deferred(EntityRendererProvider.Context pContext, PlayerSkin.Model modelType) {
            super(pContext, new PlayerModel<>(pContext.bakeLayer(modelType == PlayerSkin.Model.SLIM ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), modelType == PlayerSkin.Model.SLIM), 0.5f);
            this.modelType = modelType;
            addLayer(
                    new HumanoidArmorLayer<>(
                            this,
                            new HumanoidArmorModel<>(pContext.bakeLayer(modelType == PlayerSkin.Model.SLIM ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                            new HumanoidArmorModel<>(pContext.bakeLayer(modelType == PlayerSkin.Model.SLIM ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                            pContext.getModelManager()
                    )
            );
            addLayer(new ArrowLayer<>(pContext, this));
            addLayer(new MimicCapeLayer(this));
        }

        @Override
        public void render(MimicEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
            setupPoses(entity, getModel());
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            poseStack.pushPose();
            this.model.attackTime = this.getAttackAnim(entity, partialTicks);
            boolean shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
            this.model.riding = shouldSit;
            this.model.young = entity.isBaby();
            float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
            float f2 = f1 - f;
            float f7;
            if (shouldSit) {
                Entity var12 = entity.getVehicle();
                if (var12 instanceof LivingEntity) {
                    LivingEntity livingentity = (LivingEntity)var12;
                    f = Mth.rotLerp(partialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
                    f2 = f1 - f;
                    f7 = Mth.wrapDegrees(f2);
                    if (f7 < -85.0F) {
                        f7 = -85.0F;
                    }

                    if (f7 >= 85.0F) {
                        f7 = 85.0F;
                    }

                    f = f1 - f7;
                    if (f7 * f7 > 2500.0F) {
                        f += f7 * 0.2F;
                    }

                    f2 = f1 - f;
                }
            }

            float f6 = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
            if (isEntityUpsideDown(entity)) {
                f6 *= -1.0F;
                f2 *= -1.0F;
            }

            f2 = Mth.wrapDegrees(f2);
            float f9;
            if (entity.hasPose(Pose.SLEEPING)) {
                Direction direction = entity.getBedOrientation();
                if (direction != null) {
                    f9 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
                    poseStack.translate((float)(-direction.getStepX()) * f9, 0.0F, (float)(-direction.getStepZ()) * f9);
                }
            }

            f7 = entity.getScale();
            poseStack.scale(f7, f7, f7);
            f9 = this.getBob(entity, partialTicks);
            this.setupRotations(entity, poseStack, f9, f, partialTicks, f7);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(entity, poseStack, partialTicks);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            float f4 = 0.0F;
            float f5 = 0.0F;
            if (!shouldSit && entity.isAlive()) {
                f4 = entity.walkAnimation.speed(partialTicks);
                f5 = entity.walkAnimation.position(partialTicks);
                if (entity.isBaby()) {
                    f5 *= 3.0F;
                }

                if (f4 > 1.0F) {
                    f4 = 1.0F;
                }
            }

            this.model.prepareMobModel(entity, f5, f4, partialTicks);
            this.model.setupAnim(entity, f5, f4, f9, f2, f6);
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = this.isBodyVisible(entity);
            boolean flag1 = !flag && !entity.isInvisibleTo(minecraft.player);
            boolean flag2 = minecraft.shouldEntityAppearGlowing(entity);
            RenderType rendertype = this.getRenderType(entity, flag, flag1, flag2);
            if (rendertype != null) {
                VertexConsumer vertexconsumer = buffer.getBuffer(rendertype);
                int i = getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks));
                this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, i, flag1 ? 654311423 :
                        FastColor.ARGB32.color(255,255 - entity.getRedDiff(),255 - entity.getGreenDiff(), 255 - entity.getBlueDiff()));
            }

            if (!entity.isSpectator()) {
                Iterator var26 = this.layers.iterator();

                while(var26.hasNext()) {
                    RenderLayer<MimicEntity, PlayerModel<MimicEntity>> renderlayer = (RenderLayer)var26.next();
                    renderlayer.render(poseStack, buffer, packedLight, entity, f5, f4, partialTicks, f9, f2, f6);
                }
            }

            poseStack.popPose();
        }

        @Nullable
        protected RenderType getRenderType(MimicEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
            ResourceLocation resourceLocation;

            if(!livingEntity.getHasPlayer()){
                resourceLocation = DefaultPlayerSkin.get(livingEntity.getUUID()).texture();
            }
            else {
                PlayerSkin playerSkin;
                var skin = Deferred.cache.computeIfAbsent(
                        livingEntity.getPlayerUuid().get(), key -> CompletableFuture.supplyAsync(() -> Optional.ofNullable(
                                Minecraft.getInstance().getMinecraftSessionService().fetchProfile(key, false)
                        ), Util.ioPool()).thenCompose(o -> o
                                .map(p -> Minecraft.getInstance().getSkinManager().getOrLoad(p.profile()))
                                .orElse(CompletableFuture.completedFuture(null))
                        )
                ).getNow(null);
                if (skin == null || !livingEntity.getHasPlayer()) {
                    playerSkin = DefaultPlayerSkin.get(livingEntity.getPlayerUuid().get());
                } else {
                    playerSkin = skin;
                }

                resourceLocation = playerSkin.texture();
            }

            if (translucent) {
                return RenderType.itemEntityTranslucentCull(resourceLocation);
            } else if (bodyVisible) {
                return this.model.renderType(resourceLocation);
            } else {
                return glowing ? RenderType.outline(resourceLocation) : null;
            }
        }

        public static void scaleModelParts(MimicEntity entity, PlayerModel<MimicEntity> model){
            model.leftArm.xScale = 1 + entity.getEntityData().get(MimicEntity.LARM_SCALE);
            model.leftArm.yScale = 1 + entity.getEntityData().get(MimicEntity.LARM_SCALE);
            model.leftArm.zScale = 1 + entity.getEntityData().get(MimicEntity.LARM_SCALE);

            model.rightArm.xScale = 1 + entity.getEntityData().get(MimicEntity.RARM_SCALE);
            model.rightArm.yScale = 1 + entity.getEntityData().get(MimicEntity.RARM_SCALE);
            model.rightArm.zScale = 1 + entity.getEntityData().get(MimicEntity.RARM_SCALE);

            model.rightLeg.xScale = 1 + entity.getEntityData().get(MimicEntity.RLEG_SCALE);
            model.rightLeg.yScale = 1 + entity.getEntityData().get(MimicEntity.RLEG_SCALE);
            model.rightLeg.zScale = 1 + entity.getEntityData().get(MimicEntity.RLEG_SCALE);

            model.leftLeg.xScale = 1 + entity.getEntityData().get(MimicEntity.LLEG_SCALE);
            model.leftLeg.yScale = 1 + entity.getEntityData().get(MimicEntity.LLEG_SCALE);
            model.leftLeg.zScale = 1 + entity.getEntityData().get(MimicEntity.LLEG_SCALE);
        }

        public static void setupPoses(MimicEntity entity, PlayerModel<MimicEntity> model) {
            scaleModelParts(entity, model);
            HumanoidModel.ArmPose mainHand = getArmPose(entity, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose offHand = getArmPose(entity, InteractionHand.OFF_HAND);
            if (mainHand.isTwoHanded()) {
                offHand = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            switch (entity.getMainArm()) {
                case RIGHT -> {
                    model.rightArmPose = mainHand;
                    model.leftArmPose = offHand;
                }
                case LEFT -> {
                    model.rightArmPose = offHand;
                    model.leftArmPose = mainHand;
                }
            }
        }

        public static HumanoidModel.ArmPose getArmPose(MimicEntity entity, InteractionHand hand) {
            ItemStack stack = entity.getItemInHand(hand);
            if (stack.isEmpty()) {
                return HumanoidModel.ArmPose.EMPTY;
            }
            if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
                HumanoidModel.ArmPose pose = switch (stack.getUseAnimation()) {
                    case BLOCK -> HumanoidModel.ArmPose.BLOCK;
                    case BOW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
                    case SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
                    case CROSSBOW -> HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    case SPYGLASS -> HumanoidModel.ArmPose.SPYGLASS;
                    case TOOT_HORN -> HumanoidModel.ArmPose.TOOT_HORN;
                    case BRUSH -> HumanoidModel.ArmPose.BRUSH;
                    default -> HumanoidModel.ArmPose.EMPTY;
                };
                if (pose != HumanoidModel.ArmPose.EMPTY) {
                    return pose;
                }
            } else if (!entity.swinging && stack.is(Tags.Items.TOOLS_CROSSBOW) && CrossbowItem.isCharged(stack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            HumanoidModel.ArmPose forgePose = IClientItemExtensions.of(stack).getArmPose(entity, hand, stack);
            return forgePose != null ? forgePose : HumanoidModel.ArmPose.ITEM;
        }

        @Override
        public @NotNull ResourceLocation getTextureLocation(MimicEntity pEntity) {
            return DefaultPlayerSkin.get(pEntity.getPlayerUuid().get()).texture();
        }
    }
}