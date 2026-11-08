/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public class ShaderBannerRenderer extends IEBlockEntityRenderer<ShaderBannerBlockEntity>
{
	public ShaderBannerRenderer(BlockEntityRendererProvider.Context ctx)
	{
	}

	public void render(ShaderBannerBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
	}

	@Nullable
	public static Identifier getShaderResourceLocation(Identifier shader, Identifier shaderType)
	{
		return null;
	}
}
