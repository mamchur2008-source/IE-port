package blusunrize.immersiveengineering.client.utils;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class RenderTypeCompat
{
	public static final RenderType TRANSLUCENT = translucent();

	private RenderTypeCompat()
	{
	}

	public static RenderType solid()
	{
		return RenderTypes.itemCutout(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType cutout()
	{
		return RenderTypes.itemCutout(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType cutoutMipped()
	{
		return cutout();
	}

	public static RenderType translucent()
	{
		return RenderTypes.itemTranslucent(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType entitySolid(Identifier texture)
	{
		return RenderTypes.entitySolid(texture);
	}

	public static RenderType entityCutout(Identifier texture)
	{
		return RenderTypes.entityCutout(texture);
	}

	public static RenderType entityCutoutNoCull(Identifier texture)
	{
		return RenderTypes.entityCutout(texture);
	}

	public static RenderType entityTranslucent(Identifier texture)
	{
		return RenderTypes.entityTranslucent(texture);
	}

	public static RenderType debugQuads()
	{
		return RenderTypes.debugQuads();
	}

	public static RenderType glint()
	{
		return RenderTypes.glint();
	}

	public static RenderType entityGlint()
	{
		return RenderTypes.entityGlint();
	}

	public static RenderType guiGhostRecipeOverlay()
	{
		return translucent();
	}

	public static List<RenderType> chunkBufferLayers()
	{
		return List.of(solid(), cutout(), translucent());
	}
}
