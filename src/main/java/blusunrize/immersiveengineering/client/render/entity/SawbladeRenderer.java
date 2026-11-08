/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;

import javax.annotation.Nonnull;

public class SawbladeRenderer extends IEEntityRenderer<SawbladeEntity>
{
	public static final String NAME = "sawblade_entity";
	public static DynamicModel MODEL;

	public static final Identifier SAWBLADE = IEApi.ieLoc("item/sawblade_blade");

	public SawbladeRenderer(Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public void render(SawbladeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
	}

	@Override
	public Identifier getTextureLocation(@Nonnull SawbladeEntity entity)
	{
		return SAWBLADE;
	}

}
