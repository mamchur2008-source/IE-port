/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.LightTextureCompat;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;

public class ChemthrowerShotRenderer extends IEEntityRenderer<ChemthrowerShotEntity>
{
	public ChemthrowerShotRenderer(EntityRendererProvider.Context renderManager)
	{
		super(renderManager);
	}

	public void render(ChemthrowerShotEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		FluidStack f = entity.getFluid();
		if(f.isEmpty())
		{
			f = entity.getFluidSynced();
			if(f.isEmpty())
				return;
		}

		matrixStackIn.pushPose();

		TextureAtlasSprite sprite = GuiHelper.getFluidStillSprite(f);
		int colour = GuiHelper.getFluidColor(f);
		float a = (colour>>24&255)/255f;
		float r = (colour>>16&255)/255f;
		float g = (colour>>8&255)/255f;
		float b = (colour&255)/255f;
		int lightAll = entity.getBrightnessForRender();
		int blockLight = Math.max(LightTextureCompat.block(lightAll), LightTextureCompat.block(packedLightIn));
		int skyLight = Math.max(LightTextureCompat.sky(lightAll), LightTextureCompat.sky(packedLightIn));
		packedLightIn = LightTextureCompat.pack(blockLight, skyLight);
		matrixStackIn.scale(.25f, .25f, .25f);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(
				bufferIn, blusunrize.immersiveengineering.client.utils.RenderTypeCompat.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), matrixStackIn
		);
		builder.defaultColor(r, g, b, a);
		builder.setDefaultNormal(0, 1, 0);
		builder.setDefaultLight(packedLightIn);
		builder.setDefaultOverlay(OverlayTexture.NO_OVERLAY);
		builder.addVertex(-.25f, -.25f, 0)
				.setUv(sprite.getU(0.25f), sprite.getV(0.25f));
		builder.addVertex(.25f, -.25f, 0)
				.setUv(sprite.getU(0), sprite.getV(0.25f));
		builder.addVertex(.25f, .25f, 0)
				.setUv(sprite.getU(0), sprite.getV(0));
		builder.addVertex(-.25f, .25f, 0)
				.setUv(sprite.getU(0.25f), sprite.getV(0));
		matrixStackIn.popPose();
	}

	@Nonnull
	public Identifier getTextureLocation(@Nonnull ChemthrowerShotEntity chemthrowerShotEntity)
	{
		return IEApi.ieLoc("textures/models/bullet.png");
	}

	@Override
	protected RenderType getRenderType(ChemthrowerShotEntity entity)
	{
		return blusunrize.immersiveengineering.client.utils.RenderTypeCompat.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
	}

}
