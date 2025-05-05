package cn.ussshenzhou.gravitywar.entity;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * @author USS_Shenzhou
 */
public class CoreEntityRenderer extends EntityRenderer<CoreEntity> {
    private static final ResourceLocation END_CRYSTAL_LOCATION = ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/entity/end_crystal.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
    private static final float SIN_45 = (float) Math.sin(Math.PI / 4);
    private static final String GLASS = "glass";
    private static final String BASE = "base";
    private final ModelPart cube;
    private final ModelPart glass;
    private final ModelPart base;

    protected CoreEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0;
        ModelPart modelpart = context.bakeLayer(ModelLayers.END_CRYSTAL);
        this.glass = modelpart.getChild("glass");
        this.cube = modelpart.getChild("cube");
        this.base = modelpart.getChild("base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void render(CoreEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float f1 = ((float) entity.time + partialTicks) * 3.0F;
        VertexConsumer vertexconsumer = buffer.getBuffer(RENDER_TYPE);
        poseStack.pushPose();
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0F, 0.5F, 0.0F);
        int i = OverlayTexture.NO_OVERLAY;

        var dir = DirectionHelper.getPyramidRegion(entity.position());
        int color;
        if (dir == Direction.DOWN) {
            color = 0x80a0a0a0;
        } else {
            color = ColorHelper.getARGB(dir, 0x80);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        this.glass.render(poseStack, vertexconsumer, packedLight, i, color);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.glass.render(poseStack, vertexconsumer, packedLight, i, color);
        poseStack.scale(0.875F, 0.875F, 0.875F);
        poseStack.mulPose(new Quaternionf().setAngleAxis((float) (Math.PI / 3), SIN_45, 0.0F, SIN_45));
        poseStack.mulPose(Axis.YP.rotationDegrees(f1));
        this.cube.render(poseStack, vertexconsumer, packedLight, i, color);
        poseStack.popPose();
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(CoreEntity entity) {
        return END_CRYSTAL_LOCATION;
    }
}
