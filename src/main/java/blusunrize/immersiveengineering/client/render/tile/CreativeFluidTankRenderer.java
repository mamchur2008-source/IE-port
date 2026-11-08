/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import blusunrize.immersiveengineering.common.blocks.metal.CreativeFluidTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class CreativeFluidTankRenderer extends IEBlockEntityRenderer<CreativeFluidTankBlockEntity>
{
	private static final float MIN = 3/16f;
	private static final float MAX = 13/16f;
	private static final int FLUID_ALPHA = 176;

	public void submit(
			RenderState<CreativeFluidTankBlockEntity> state, PoseStack poseStack,
			SubmitNodeCollector nodes, CameraRenderState cameraState
	)
	{
		if(state.blockEntity==null||state.blockEntity.getStoredFluid().isEmpty())
			return;
		nodes.submitCustomGeometry(
				poseStack,
				RenderTypeCompat.translucent(),
				(pose, consumer) -> {
					PoseStack renderPose = new PoseStack();
					renderPose.last().set(pose);
					render(
							state.blockEntity, state.partialTicks, renderPose, type -> consumer,
							state.lightCoords, OverlayTexture.NO_OVERLAY
					);
				}
		);
	}

	public void render(
			CreativeFluidTankBlockEntity blockEntity,
			float partialTicks,
			PoseStack matrixStack,
			MultiBufferSource bufferIn,
			int combinedLightIn,
			int combinedOverlayIn
	)
	{
		FluidStack fluid = blockEntity.getStoredFluid();
		if(fluid.isEmpty())
			return;

		TextureAtlasSprite sprite = GuiHelper.getFluidStillSprite(fluid);
		int color = GuiHelper.getFluidColor(fluid);
		int red = color>>16&255;
		int green = color>>8&255;
		int blue = color&255;
		int alpha = color>>>24;
		if(alpha <= 0)
			alpha = 255;
		alpha = Math.min(alpha, FLUID_ALPHA);
		VertexConsumer builder = bufferIn.getBuffer(RenderTypeCompat.translucent());
		PoseStack.Pose pose = matrixStack.last();
		Matrix4f mat = pose.pose();

		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, 0, 0, 1,
				MIN, MIN, MAX, MAX, MIN, MAX, MAX, MAX, MAX, MIN, MAX, MAX,
				sprite, red, green, blue, alpha);
		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, 0, 0, -1,
				MAX, MIN, MIN, MIN, MIN, MIN, MIN, MAX, MIN, MAX, MAX, MIN,
				sprite, red, green, blue, alpha);
		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, 1, 0, 0,
				MAX, MIN, MAX, MAX, MIN, MIN, MAX, MAX, MIN, MAX, MAX, MAX,
				sprite, red, green, blue, alpha);
		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, -1, 0, 0,
				MIN, MIN, MIN, MIN, MIN, MAX, MIN, MAX, MAX, MIN, MAX, MIN,
				sprite, red, green, blue, alpha);
		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, 0, 1, 0,
				MIN, MAX, MAX, MAX, MAX, MAX, MAX, MAX, MIN, MIN, MAX, MIN,
				sprite, red, green, blue, alpha);
		quad(builder, pose, mat, combinedLightIn, combinedOverlayIn, 0, -1, 0,
				MIN, MIN, MIN, MAX, MIN, MIN, MAX, MIN, MAX, MIN, MIN, MAX,
				sprite, red, green, blue, alpha);
	}

	private static void quad(
			VertexConsumer builder, PoseStack.Pose pose, Matrix4f mat,
			int light, int overlay, float normalX, float normalY, float normalZ,
			float x0, float y0, float z0, float x1, float y1, float z1,
			float x2, float y2, float z2, float x3, float y3, float z3,
			TextureAtlasSprite sprite, int red, int green, int blue, int alpha
	)
	{
		vertex(builder, pose, mat, x0, y0, z0, light, overlay, normalX, normalY, normalZ, red, green, blue, alpha, sprite.getU0(), sprite.getV1());
		vertex(builder, pose, mat, x1, y1, z1, light, overlay, normalX, normalY, normalZ, red, green, blue, alpha, sprite.getU1(), sprite.getV1());
		vertex(builder, pose, mat, x2, y2, z2, light, overlay, normalX, normalY, normalZ, red, green, blue, alpha, sprite.getU1(), sprite.getV0());
		vertex(builder, pose, mat, x3, y3, z3, light, overlay, normalX, normalY, normalZ, red, green, blue, alpha, sprite.getU0(), sprite.getV0());
	}

	private static void vertex(
			VertexConsumer builder, PoseStack.Pose pose, Matrix4f mat,
			float x, float y, float z, int light, int overlay,
			float normalX, float normalY, float normalZ, int red, int green, int blue, int alpha, float u, float v
	)
	{
		builder.addVertex(mat, x, y, z).setColor(red, green, blue, alpha)
				.setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ);
	}
}
