/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.blocks.wooden.ChoppingBlockBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.ChoppingBlockBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class ChoppingBlockRenderer extends IEBlockEntityRenderer<ChoppingBlockBlockEntity>
{
	@Override
	public void submit(
			RenderState<ChoppingBlockBlockEntity> state, PoseStack transform, SubmitNodeCollector nodes,
			CameraRenderState cameraState
	)
	{
		ChoppingBlockBlockEntity te = state.blockEntity;
		if(te==null||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		ItemStack stack = te.getLogStackForRendering();
		if(stack.isEmpty())
		{
			BlockState blockState = te.getBlockState();
			if(!blockState.hasProperty(ChoppingBlockBlock.HAS_LOG)||!blockState.getValue(ChoppingBlockBlock.HAS_LOG))
				return;
			stack = new ItemStack(Items.OAK_LOG);
		}

		float progress = te.getChopAnimation(state.partialTicks);
		submitLog(te, stack, progress, transform, nodes, state.lightCoords);
	}

	private static void submitLog(
			ChoppingBlockBlockEntity te, ItemStack stack, float animationProgress, PoseStack transform,
			SubmitNodeCollector nodes, int light
	)
	{
		float impact = getImpactAmount(animationProgress);
		float rebound = Mth.sin(Mth.clamp(animationProgress, 0, 1)*Mth.PI);
		float squash = 1-.08F*impact;
		float spread = 1+.04F*impact;

		transform.pushPose();
		transform.translate(.5, ChoppingBlockBlock.LOG_RENDER_Y+.015F*rebound, .5);
		transform.scale(
				ChoppingBlockBlock.LOG_RENDER_ITEM_SCALE*spread,
				ChoppingBlockBlock.LOG_RENDER_ITEM_SCALE*squash,
				ChoppingBlockBlock.LOG_RENDER_ITEM_SCALE*spread
		);
		submitItem(te, stack, ItemDisplayContext.FIXED, transform, nodes, light);
		transform.popPose();
	}

	private static float getImpactAmount(float progress)
	{
		progress = Mth.clamp(progress, 0, 1);
		if(progress < .45F)
			return Mth.sin(progress/.45F*Mth.HALF_PI);
		return Mth.cos((progress-.45F)/.55F*Mth.HALF_PI);
	}

	private static void submitItem(
			ChoppingBlockBlockEntity te, ItemStack stack, ItemDisplayContext displayContext, PoseStack transform,
			SubmitNodeCollector nodes, int light
	)
	{
		ItemStackRenderState itemState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
				itemState, stack, displayContext, te.getLevel(), null, 0
		);
		itemState.submit(transform, nodes, light, OverlayTexture.NO_OVERLAY, 0);
	}
}
