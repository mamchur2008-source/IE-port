/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

public abstract class IEBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, IEBlockEntityRenderer.RenderState<T>>
{
	private static final Map<Direction, Quaternionf> ROTATE_FOR_FACING = Util.make(
			new EnumMap<>(Direction.class), m -> {
				for(Direction facing : DirectionUtils.BY_HORIZONTAL_INDEX)
					m.put(facing, new Quaternionf().rotateY(Mth.DEG_TO_RAD*(180-facing.toYRot())));
			}
	);

	public RenderState<T> createRenderState()
	{
		return new RenderState<>();
	}

	public void extractRenderState(T blockEntity, RenderState<T> state, float partialTicks, Vec3 cameraPos, CrumblingOverlay overlay)
	{
		BlockEntityRenderState.extractBase(blockEntity, state, overlay);
		state.blockEntity = blockEntity;
		state.partialTicks = partialTicks;
	}

	public void submit(RenderState<T> state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState cameraState)
	{
		if(state.blockEntity==null)
			return;
		nodes.submitCustomGeometry(
				poseStack,
				RenderTypeCompat.solid(),
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

	public int getViewDistance()
	{
		double increase = IEClientConfig.increasedTileRenderdistance.get();
		return (int)(BlockEntityRenderer.super.getViewDistance()*increase);
	}

	public void render(
			T blockEntity,
			float partialTicks,
			PoseStack poseStack,
			MultiBufferSource buffers,
			int packedLight,
			int packedOverlay
	)
	{
	}

	public static class RenderState<T extends BlockEntity> extends BlockEntityRenderState
	{
		protected T blockEntity;
		protected float partialTicks;
	}

	protected static void rotateForFacingNoCentering(PoseStack stack, Direction facing)
	{
		stack.mulPose(ROTATE_FOR_FACING.get(facing));
	}

	protected static void rotateForFacing(PoseStack stack, Direction facing)
	{
		stack.translate(0.5, 0.5, 0.5);
		rotateForFacingNoCentering(stack, facing);
		stack.translate(-0.5, -0.5, -0.5);
	}
}
