/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.mirror;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static blusunrize.immersiveengineering.client.utils.ModelUtils.copyTypes;

public record MirroredGeometry(Object inner) implements IUnbakedGeometry<MirroredGeometry>
{
	public BakedModel bake(
			IGeometryBakingContext owner, ModelBaker bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
			ItemOverrides overrides
	)
	{
		return new SimpleBakedModel();
	}
}
