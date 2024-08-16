package com.prohitman.friendsmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MimicRenderer<T extends MimicEntity> extends HumanoidMobRenderer<T, PlayerModel<T>> {
/*    private static final ResourceLocation BUILDER_TEXTURE = AmbientPlayers.id("textures/entity/ambient_player/builder.png");
    // These are set up this way in case any of the skins need to be removed individually due to model incompatibility
    private static final List<ResourceLocation> WIDE_SKINS = Util.make(new ObjectArrayList<>(), list -> {
        for (PlayerSkin skinType : DefaultPlayerSkin.DEFAULT_SKINS) {
            if (skinType.model() == PlayerSkin.Model.WIDE) {
                list.add(skinType.texture());
            }
        }
        list.add(AmbientPlayers.id("textures/entity/ambient_player/agent.png"));
    });
    private static final List<ResourceLocation> SLIM_SKINS = Util.make(new ObjectArrayList<>(), list -> {
        for (PlayerSkin skinType : DefaultPlayerSkin.DEFAULT_SKINS) {
            if (skinType.model() == PlayerSkin.Model.SLIM) {
                list.add(skinType.texture());
            }
        }
        list.add(AmbientPlayers.id("textures/entity/ambient_player/agent.png"));
    });*/
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
        if ((pEntity.getUUID().getLeastSignificantBits() & 1) == 0) {
            slim.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        } else {
            wide.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }
/*        List<? extends String> displayNames = Config.SERVER.names.get();
        if (Config.CLIENT.displayNames.get() && !displayNames.isEmpty()) {
            renderNameTag(pEntity, Component.literal(displayNames.get(Math.floorMod(pEntity.getUUID().hashCode(), displayNames.size()))), pPoseStack, pBuffer, pPackedLight, pPartialTicks);
        }*/
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MimicEntity pEntity) {
        //return DefaultPlayerSkin.getDefaultTexture();
        return DefaultPlayerSkin.get(pEntity.getPlayerUuid().get()).texture();
    }

    public static class Deferred extends HumanoidMobRenderer<MimicEntity, PlayerModel<MimicEntity>> {
        protected final PlayerSkin.Model modelType;

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
        }

        @Override
        public void render(MimicEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
            setupPoses(pEntity, getModel());
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
        }

        public static void setupPoses(MimicEntity entity, PlayerModel<MimicEntity> model) {
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