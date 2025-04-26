package cn.ussshenzhou.gravitywar.border;

import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static org.lwjgl.opengl.GL32.*;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class BorderRenderer {
    private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    @SubscribeEvent
    public static void renderBorder(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            return;
        }
        var alpha = shouldRenderBorder();
        if (alpha == 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(0, alpha,  alpha, 1- alpha);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        fillVertices(event, bufferbuilder);
        MeshData meshdata = bufferbuilder.build();
        if (meshdata != null) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            RenderSystem.getModelViewStack().pushMatrix();
            RenderSystem.getModelViewStack().set(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
            BufferUploader.drawWithShader(meshdata);
            RenderSystem.getModelViewStack().popMatrix();
            meshdata.close();
        }
        RenderSystem.enableCull();
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
    }

    private static final float WARN_DISTANCE = 5;

    private static float shouldRenderBorder() {
        var camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        float dis = (float) DirectionHelper.distanceToBoundary(camPos.x, camPos.y, camPos.z);
        dis = 1 - (Mth.clamp(dis, 0, WARN_DISTANCE) / WARN_DISTANCE);
        return dis;
    }

    private static final int RENDER_DISTANCE = 50;

    private static void fillVertices(RenderLevelStageEvent event, BufferBuilder bufferbuilder) {
        var poseStack = event.getPoseStack();
        var camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, -RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, -RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);

        bufferbuilder.addVertex(poseStack.last(), 0, 0, 0).setUv(0, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, RENDER_DISTANCE, RENDER_DISTANCE).setUv(RENDER_DISTANCE, 0);
        bufferbuilder.addVertex(poseStack.last(), -RENDER_DISTANCE, -RENDER_DISTANCE, RENDER_DISTANCE).setUv(0, RENDER_DISTANCE);
    }
}
