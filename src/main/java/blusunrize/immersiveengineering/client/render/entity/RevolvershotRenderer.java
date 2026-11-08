/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class RevolvershotRenderer extends IEEntityRenderer<RevolvershotEntity>
{
	public RevolvershotRenderer(Context renderManager)
	{
		super(renderManager);
	}

	public void render(@Nonnull RevolvershotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn,
						 MultiBufferSource bufferIn, int packedLightIn)
	{
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(
				new Quaternionf()
						.rotateY(Mth.DEG_TO_RAD * (entity.yRotO+(entity.getYRot()-entity.yRotO)*partialTicks-90.0F))
						.rotateZ(Mth.DEG_TO_RAD * (entity.xRotO+(entity.getXRot()-entity.xRotO)*partialTicks))
		);
		matrixStackIn.scale(0.25F, 0.25F, 0.25F);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(
				bufferIn, RenderTypeCompat.entityCutout(getTextureLocation(entity)), matrixStackIn
		);
		builder.defaultColor(1, 1, 1, 1);
		builder.setDefaultOverlay(OverlayTexture.NO_OVERLAY);
		builder.setDefaultLight(packedLightIn);
		builder.setDefaultNormal(0, 1, 0);

		builder.addVertex(0, 0, -.25f).setUv(5/32f, 10/32f);
		builder.addVertex(0, 0, .25f).setUv(0/32f, 10/32f);
		builder.addVertex(0, .5f, .25f).setUv(0/32f, 5/32f);
		builder.addVertex(0, .5f, -.25f).setUv(5/32f, 5/32f);

		builder.addVertex(.375f, .125f, 0).setUv(8/32f, 5/32f);
		builder.addVertex(0, .125f, 0).setUv(0/32f, 5/32f);
		builder.addVertex(0, .375f, 0).setUv(0/32f, 0/32f);
		builder.addVertex(.375f, .375f, 0).setUv(8/32f, 0/32f);

		builder.addVertex(.375f, .25f, -.25f).setUv(8/32f, 5/32f);
		builder.addVertex(0, .25f, -.25f).setUv(0/32f, 5/32f);
		builder.addVertex(0, .25f, .25f).setUv(0/32f, 0/32f);
		builder.addVertex(.375f, .25f, .25f).setUv(8/32f, 0/32f);

		matrixStackIn.popPose();
	}

	@Nonnull
	public Identifier getTextureLocation(@Nonnull RevolvershotEntity entity)
	{
		return IEApi.ieLoc("textures/models/bullet.png");
	}

	@Override
	protected RenderType getRenderType(RevolvershotEntity entity)
	{
		return RenderTypeCompat.entityCutout(getTextureLocation(entity));
	}

}
