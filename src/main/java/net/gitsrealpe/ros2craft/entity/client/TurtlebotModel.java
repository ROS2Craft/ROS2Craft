// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

package net.gitsrealpe.ros2craft.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.gitsrealpe.ros2craft.ROS2Craft;
import net.gitsrealpe.ros2craft.entity.custom.TurtlebotEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class TurtlebotModel<T extends TurtlebotEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in
    // the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ROS2Craft.MODID, "turtlebot"), "main");
    private final ModelPart body;
    private final ModelPart wheels;

    public TurtlebotModel(ModelPart root) {
        this.body = root.getChild("Body");
        this.wheels = this.body.getChild("Wheels");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 18)
                        .addBox(-5.0F, -6.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-3.0F))
                        .texOffs(8, 1).addBox(-9.0F, -3.0F, -3.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 22.0F, -3.0F));

        PartDefinition Wheels = Body.addOrReplaceChild("Wheels",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-8.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(-1.0F))
                        .texOffs(0, 0).addBox(4.0F, -3.0F, -3.0F, 4.0F, 6.0F, 6.0F, new CubeDeformation(-1.0F)),
                PartPose.offset(-3.0F, 0.0F, 3.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(TurtlebotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        // if (this.wheelSpeed++ == 100) {
        this.wheels.xRot = ageInTicks * 0.1f;
        // this.wheelSpeed = 0;
        // }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return body;
    }
}