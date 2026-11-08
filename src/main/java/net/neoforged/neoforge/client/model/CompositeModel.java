package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import java.util.ArrayList;
import java.util.List;

public class CompositeModel
{
	public static class Baked
	{
		public static Builder builder(
				IGeometryBakingContext context,
				TextureAtlasSprite particle,
				ItemOverrides overrides,
				ItemTransforms transforms
		)
		{
			return new Builder(particle, overrides, transforms);
		}

		public static class Builder
		{
			private final TextureAtlasSprite particle;
			private final ItemOverrides overrides;
			private final ItemTransforms transforms;
			private final List<BakedQuad> quads = new ArrayList<>();

			private Builder(TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms)
			{
				this.particle = particle;
				this.overrides = overrides;
				this.transforms = transforms;
			}

			public Builder addQuads(RenderTypeGroup group, BakedQuad quad)
			{
				quads.add(quad);
				return this;
			}

			public BakedModel build()
			{
				return new SimpleBakedModel()
				{
					public TextureAtlasSprite getParticleIcon()
					{
						return particle;
					}

					public ItemOverrides getOverrides()
					{
						return overrides;
					}

					public ItemTransforms getTransforms()
					{
						return transforms;
					}
				};
			}
		}
	}
}
