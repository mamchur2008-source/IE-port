/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.neoforge.model.data.ModelData;

public class SqueezerRenderer extends IEMultiblockRenderer<State>
{
	public static final String NAME = "squeezer_piston";
	public static DynamicModel PISTON;

	public void render(IMultiblockContext<State> ctx, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final MultiblockOrientation orientation = ctx.getLevel().getOrientation();

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
		VertexConsumer buffer = bufferIn.getBuffer(blusunrize.immersiveengineering.client.utils.RenderTypeCompat.solid());

		float piston = ctx.getState().animation_piston;
		//Smoothstep! TODO partial ticks?
		piston = piston*piston*(3.0f-2.0f*piston);

		matrixStack.translate(0, piston, 0);

		matrixStack.translate(-.5, -.5, -.5);
		rotateForFacing(matrixStack, orientation.front());
		RenderUtils.renderModelTESRFast(
				PISTON.get().getQuads(null, ModelData.EMPTY, blusunrize.immersiveengineering.client.utils.RenderTypeCompat.solid()),
				buffer, matrixStack, combinedLightIn, combinedOverlayIn
		);

		matrixStack.popPose();
	}
}
