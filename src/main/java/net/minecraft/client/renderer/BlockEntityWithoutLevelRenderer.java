package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BlockEntityWithoutLevelRenderer
{
	public BlockEntityWithoutLevelRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet)
	{
	}

	public void renderByItem(
			ItemStack stack,
			ItemDisplayContext transformType,
			PoseStack poseStack,
			MultiBufferSource buffers,
			int packedLight,
			int packedOverlay
	)
	{
	}
}
