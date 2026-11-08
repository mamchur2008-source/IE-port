/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.PipeCallbacks;
import blusunrize.immersiveengineering.client.models.obj.callback.block.PipeCallbacks.Key;
import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity.ConnectionStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.EnumMap;
import java.util.Map;

public class FluidPipeRenderer extends IEBlockEntityRenderer<FluidPipeBlockEntity>
{
	public static final String NAME = "block/metal_device/fluid_pipe";
	public static DynamicModel MODEL;

	public void render(
			FluidPipeBlockEntity pipe,
			float partialTicks,
			PoseStack matrixStack,
			MultiBufferSource bufferIn,
			int combinedLightIn,
			int combinedOverlayIn
	)
	{
		if(MODEL==null)
			return;

		Map<Direction, ConnectionStyle> connections = new EnumMap<>(Direction.class);
		for(Direction face : DirectionUtils.VALUES)
			connections.put(face, pipe.getConnectionStyle(face));
		Key key = new Key(connections, pipe.cover==Blocks.AIR?null: pipe.cover, pipe.getColor());
		ModelData data = ModelDataUtils.single(IEOBJCallbacks.getModelProperty(PipeCallbacks.INSTANCE), key);
		VertexConsumer buffer = bufferIn.getBuffer(RenderTypeCompat.cutout());
		RenderUtils.renderModelTESRFast(
				MODEL.get().getQuads(pipe.getBlockState(), data, RenderTypeCompat.cutout()),
				buffer, matrixStack, combinedLightIn, combinedOverlayIn
		);
	}
}
