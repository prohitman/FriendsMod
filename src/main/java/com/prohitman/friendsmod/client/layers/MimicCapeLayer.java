package com.prohitman.friendsmod.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.prohitman.friendsmod.client.MimicRenderer;
import com.prohitman.friendsmod.common.entity.MimicEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@OnlyIn(Dist.CLIENT)
public class MimicCapeLayer extends RenderLayer<MimicEntity, PlayerModel<MimicEntity>> {
    public MimicCapeLayer(RenderLayerParent<MimicEntity, PlayerModel<MimicEntity>> renderer) {
        super(renderer);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, MimicEntity livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!livingEntity.isInvisible() /*&& livingEntity.isModelPartShown(PlayerModelPart.CAPE)*/) {
            PlayerSkin playerSkin;
            var skin= MimicRenderer.Deferred.cache.computeIfAbsent(
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

            if (playerSkin.capeTexture() != null) {
                ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (!itemstack.is(Items.ELYTRA)) {
                    poseStack.pushPose();
                    poseStack.translate(0.0F, 0.0F, 0.125F);
                    double d0 = Mth.lerp((double)partialTicks, livingEntity.xCloakO, livingEntity.xCloak) - Mth.lerp((double)partialTicks, livingEntity.xo, livingEntity.getX());
                    double d1 = Mth.lerp((double)partialTicks, livingEntity.yCloakO, livingEntity.yCloak) - Mth.lerp((double)partialTicks, livingEntity.yo, livingEntity.getY());
                    double d2 = Mth.lerp((double)partialTicks, livingEntity.zCloakO, livingEntity.zCloak) - Mth.lerp((double)partialTicks, livingEntity.zo, livingEntity.getZ());
                    float f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                    double d3 = (double)Mth.sin(f * 0.017453292F);
                    double d4 = (double)(-Mth.cos(f * 0.017453292F));
                    float f1 = (float)d1 * 10.0F;
                    f1 = Mth.clamp(f1, -6.0F, 32.0F);
                    float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                    f2 = Mth.clamp(f2, 0.0F, 150.0F);
                    float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                    f3 = Mth.clamp(f3, -20.0F, 20.0F);
                    if (f2 < 0.0F) {
                        f2 = 0.0F;
                    }

                    float f4 = Mth.lerp(partialTicks, livingEntity.oBob, livingEntity.bob);
                    f1 += Mth.sin(Mth.lerp(partialTicks, livingEntity.walkDistO, livingEntity.walkDist) * 6.0F) * 32.0F * f4;
                    if (livingEntity.isCrouching()) {
                        f1 += 25.0F;
                    }

                    poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
                    VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(playerSkin.capeTexture()));
                    ((PlayerModel)this.getParentModel()).renderCloak(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    poseStack.popPose();
                }
            }
        }

    }
}
