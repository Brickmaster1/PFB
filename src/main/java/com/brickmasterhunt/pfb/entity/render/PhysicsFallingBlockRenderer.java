package com.brickmasterhunt.pfb.entity.render;

import com.brickmasterhunt.pfb.entity.PhysicsFallingBlockEntity;
import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class PhysicsFallingBlockRenderer extends EntityRenderer<PhysicsFallingBlockEntity> {

    public PhysicsFallingBlockRenderer(EntityRendererProvider.Context dispatcher) {
        super(dispatcher);
        this.shadowRadius = 0.5F;
    }

    public void render(PhysicsFallingBlockEntity fallingBlockEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        BlockState blockState = fallingBlockEntity.getBlockState();

        if(blockState.getRenderShape() == RenderShape.MODEL) {
            Level level = fallingBlockEntity.getLevel();
            com.mojang.math.Quaternion rot = Convert.toMinecraft(fallingBlockEntity.getPhysicsRotation(new Quaternion(), g));
            if (blockState != level.getBlockState(fallingBlockEntity.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
                poseStack.pushPose();
                BlockPos blockPos = new BlockPos(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
                poseStack.mulPose(rot);
                poseStack.translate(-0.5D, -0.5D, -0.5D);
                BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
                for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.chunkBufferLayers()) {
                    if (ItemBlockRenderTypes.canRenderInLayer(blockState, type)) {
                        net.minecraftforge.client.ForgeHooksClient.setRenderType(type);
                        blockRenderDispatcher.getModelRenderer().tesselateBlock(level, blockRenderDispatcher.getBlockModel(blockState), blockState, blockPos, poseStack, multiBufferSource.getBuffer(type), false, new Random(), blockState.getSeed(fallingBlockEntity.getStartPos()), OverlayTexture.NO_OVERLAY);
                    }
                }
                net.minecraftforge.client.ForgeHooksClient.setRenderType(null);
                poseStack.popPose();
                super.render(fallingBlockEntity, f, g, poseStack, multiBufferSource, i);
            }
        }
    }

    public ResourceLocation getTextureLocation(PhysicsFallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}