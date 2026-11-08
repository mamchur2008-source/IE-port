/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;

public abstract class IEMultiblockRenderer<State extends IMultiblockState>
		extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
		implements MultiblockRenderer<State>
{
	public void render(
			MultiblockBlockEntityMaster<State> blockEntity,
			float partialTicks,
			PoseStack poseStack,
			MultiBufferSource buffers,
			int packedLight,
			int packedOverlay
	)
	{
		MultiblockRenderer.super.render(blockEntity, partialTicks, poseStack, buffers, packedLight, packedOverlay);
	}

	public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<State> blockEntity)
	{
		return MultiblockRenderer.super.getRenderBoundingBox(blockEntity);
	}
}
