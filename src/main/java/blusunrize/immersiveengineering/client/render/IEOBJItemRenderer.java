/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class IEOBJItemRenderer extends BlockEntityWithoutLevelRenderer
{
	public IEOBJItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_)
	{
		super(p_172550_, p_172551_);
	}

	public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack matrixStackIn, @Nonnull MultiBufferSource bufferIn,
							 int combinedLightIn, int combinedOverlayIn)
	{
	}

	public <T> void renderByItem(
			ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn, SpecificIEOBJModel<T> model
	)
	{
	}
}
