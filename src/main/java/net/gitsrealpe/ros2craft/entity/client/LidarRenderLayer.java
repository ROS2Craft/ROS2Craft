package net.gitsrealpe.ros2craft.entity.client;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public class LidarRenderLayer extends RenderLayer<TurtlebotEntity, TurtlebotModel<TurtlebotEntity>> {
    private final ItemRenderer itemRenderer;

    public LidarRenderLayer(TurtlebotRenderer renderer, ItemRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            TurtlebotEntity entity, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // render item from slot 0 on the head / back / wherever
        ItemStack stack = entity.inventory.getStackInSlot(0);
        if (!stack.isEmpty()) {
            poseStack.pushPose();

            // Position the item
            poseStack.translate(0.0D, 1.2D, 0.4D); // up + back
            // poseStack.rotateAround(new Quaternionf(Axis.ZP.rotationDegrees(180.0F)),
            // ageInTicks, netHeadYaw, headPitch);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F)); // face outward
            poseStack.scale(0.8F, 0.8F, 0.8F);

            this.itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    getLightLevel(entity.level(), entity.getOnPos().above()), OverlayTexture.NO_OVERLAY, poseStack,
                    buffer, entity.level(), 1);
            poseStack.popPose();
        }

    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}