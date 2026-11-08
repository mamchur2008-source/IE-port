package net.neoforged.neoforge.client.model.geometry;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;

import java.util.function.Function;

public interface IUnbakedGeometry<T>
{
	BakedModel bake(
			IGeometryBakingContext context,
			ModelBaker bakery,
			Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelState,
			ItemOverrides overrides
	);
}
