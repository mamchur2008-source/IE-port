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
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;

public class MixerRenderer extends IEMultiblockRenderer<State>
{
	public static final String NAME = "mixer_agitator";
	public static DynamicModel AGITATOR;

	public void render(IMultiblockContext<State> ctx, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final State state = ctx.getState();
		final MultiblockOrientation orientation = ctx.getLevel().getOrientation();

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
		matrixStack.pushPose();
		final Direction front = orientation.front();
		matrixStack.translate(front==Direction.SOUTH||front==Direction.WEST?-.5: .5, 0, front==Direction.SOUTH||front==Direction.EAST?.5: -.5);
		float agitator = state.animation_agitator-(!state.isActive?0: (1-partialTicks)*9f);
		matrixStack.mulPose(new Quaternionf().rotateY(agitator *Mth.DEG_TO_RAD));

		matrixStack.translate(-0.5, -0.5, -0.5);
		RenderUtils.renderModelTESRFast(
				AGITATOR.get().getQuads(null, ModelData.EMPTY, blusunrize.immersiveengineering.client.utils.RenderTypeCompat.solid()),
				bufferIn.getBuffer(blusunrize.immersiveengineering.client.utils.RenderTypeCompat.solid()),
				matrixStack, combinedLightIn, combinedOverlayIn
		);

		matrixStack.popPose();

		matrixStack.translate(front==Direction.SOUTH||front==Direction.WEST?-.5: .5, -.625f, front==Direction.SOUTH||front==Direction.EAST?.5: -.5);
		matrixStack.scale(.0625f, 1, .0625f);
		matrixStack.mulPose(new Quaternionf().rotateX(Mth.HALF_PI));

		for(int i = state.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = state.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				float yy = fs.getAmount()/(float)state.tank.getCapacity()*1.0625f;
				matrixStack.translate(0, 0, -yy);
				float w = (i < state.tank.getFluidTypes()-1||yy >= .125)?26: 16+yy/.0125f;
				GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(blusunrize.immersiveengineering.client.utils.RenderTypeCompat.translucent()), matrixStack, fs,
						-w/2, -w/2, w, w);
			}
		}

		matrixStack.popPose();
	}
}
