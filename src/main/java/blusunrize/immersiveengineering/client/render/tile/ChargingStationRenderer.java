/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.blocks.metal.ChargingStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class ChargingStationRenderer extends IEBlockEntityRenderer<ChargingStationBlockEntity>
{
	@Override
	public void submit(
			RenderState<ChargingStationBlockEntity> state, PoseStack matrixStack,
			SubmitNodeCollector nodes, CameraRenderState cameraState
	)
	{
		ChargingStationBlockEntity te = state.blockEntity;
		if(te==null||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		ItemStack stack = te.inventory.get(0);
		if(stack.isEmpty())
			return;

		matrixStack.pushPose();
		applyItemTransform(te, matrixStack);
		ItemStackRenderState itemState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
				itemState, stack, ItemDisplayContext.FIXED, te.getLevel(), null, 0
		);
		itemState.submit(matrixStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		matrixStack.popPose();
	}

	private static void applyItemTransform(ChargingStationBlockEntity te, PoseStack matrixStack)
	{
		matrixStack.translate(.5, .3125, .5);
		matrixStack.scale(.75f, .75f, .75f);
		switch(te.getFacing())
		{
			case NORTH:
				matrixStack.mulPose(new Quaternionf().rotateY(Mth.PI));
				break;
			case SOUTH:
				break;
			case WEST:
				matrixStack.mulPose(new Quaternionf().rotateY(-Mth.HALF_PI));
				break;
			case EAST:
				matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI));
				break;
		}
		float scale = .625f;
		matrixStack.scale(scale, scale, 1);
	}
}
