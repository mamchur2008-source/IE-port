/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class IERenderTypes
{
	public static final RenderType TRANSLUCENT_FULLBRIGHT = RenderTypeCompat.translucent();
	public static final RenderType SOLID_FULLBRIGHT = RenderTypeCompat.solid();
	public static final RenderType LINES = RenderTypes.linesTranslucent();
	public static final RenderType LINES_NONTRANSLUCENT = RenderTypes.lines();
	public static final RenderType POINTS = RenderTypes.debugPoint();
	public static final RenderType TRANSLUCENT_TRIANGLES = RenderTypeCompat.translucent();
	public static final RenderType TRANSLUCENT_POSITION_COLOR = RenderTypes.debugQuads();
	public static final RenderType TRANSLUCENT_NO_DEPTH = RenderTypeCompat.translucent();
	public static final RenderType CHUNK_MARKER = RenderTypes.linesTranslucent();
	public static final RenderType POSITION_COLOR_LIGHTMAP = RenderTypeCompat.solid();
	public static final RenderType ITEM_DAMAGE_BAR = RenderTypes.debugQuads();
	public static final RenderType PARTICLES = RenderTypeCompat.translucent();

	public static RenderType getGui(Identifier texture)
	{
		return RenderTypes.itemCutout(texture);
	}

	public static RenderType getGuiTranslucent(Identifier texture)
	{
		return RenderTypes.itemTranslucent(texture);
	}

	public static RenderType getLines(float lineWidth)
	{
		return RenderTypes.linesTranslucent();
	}

	public static RenderType getParticleLines(float lineWidth)
	{
		return RenderTypes.linesTranslucent();
	}

	public static RenderType getPositionTex(Identifier texture)
	{
		return RenderTypes.itemCutout(texture);
	}

	public static RenderType getFullbrightTranslucent(Identifier texture)
	{
		return RenderTypes.entityTranslucentEmissive(texture);
	}

	public static MultiBufferSource wrapWithStencil(
			MultiBufferSource in,
			Consumer<VertexConsumer> setupStencilArea,
			String name,
			int ref
	)
	{
		return in;
	}

	public static MultiBufferSource whiteLightmap(MultiBufferSource in)
	{
		return in;
	}
}
