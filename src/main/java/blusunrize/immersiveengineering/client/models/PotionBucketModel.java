/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.utils.QuadTransformer;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.register.IEFluids;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.CompositeModel.Baked.Builder;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionFluidType.TEXTURE_STILL;

public final class PotionBucketModel implements IUnbakedGeometry<PotionBucketModel>
{
	private final IQuadTransformer recolorTransformer;
	private final IUnbakedGeometry<?> baseGeometry;

	public PotionBucketModel(int color)
	{
		this.recolorTransformer = quad -> quad;
		JsonObject baseModelJSON = new JsonObject();
		baseModelJSON.addProperty("fluid", IEFluids.POTION.getId().toString());
		this.baseGeometry = DynamicFluidContainerModel.Loader.INSTANCE.read(baseModelJSON, null);
	}

	public BakedModel bake(
			IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState modelTransform, ItemOverrides overrides
	)
	{
		BakedModel baseModel = baseGeometry.bake(context, bakery, spriteGetter, modelTransform, overrides);
		StandaloneGeometryBakingContext itemContext = StandaloneGeometryBakingContext.builder(context)
				.withGui3d(false)
				.withUseBlockLight(false)
				.build(IEApi.ieLoc("temporary"));
		Builder builder = CompositeModel.Baked.builder(
				itemContext,
				baseModel.getParticleIcon(ModelData.EMPTY),
				new OverrideHandler(baseModel.getOverrides(), bakery, context, spriteGetter),
				context.getTransforms()
		);
		Identifier fluidMaskLocation = TEXTURE_STILL;
		for(var layerModel : baseModel.getRenderPasses(ItemStack.EMPTY, false))
		{
			var layerGroup = layerModel instanceof SimpleBakedModel simple?ModelUtils.copyTypes(simple): RenderTypeGroup.EMPTY;
			for(var layer : layerModel.getRenderTypes(ItemStack.EMPTY, false))
			{
				List<BakedQuad> baseQuads = layerModel.getQuads(
						null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, layer
				);
				for(BakedQuad baseQuad : baseQuads)
				{
					BakedQuad newQuad;
					if(baseQuad.materialInfo().sprite().contents().name().equals(fluidMaskLocation))
						newQuad = recolorTransformer.process(baseQuad);
					else
						newQuad = baseQuad;
					builder.addQuads(layerGroup, newQuad);
				}
			}
		}
		return builder.build();
	}

	public static class Loader implements IGeometryLoader<PotionBucketModel>
	{
		public static final Identifier LOADER_NAME = ImmersiveEngineering.rl("potion_bucket");

		@Nonnull
		public PotionBucketModel read(
				@Nonnull JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext
		)
		{
			return new PotionBucketModel(-1);
		}
	}

	private static class OverrideHandler extends ItemOverrides
	{
		private final Int2ObjectMap<BakedModel> coloredModels = new Int2ObjectOpenHashMap<>();
		private final ItemOverrides nested;
		private final ModelBaker bakery;
		private final IGeometryBakingContext owner;
		private final Function<Material, TextureAtlasSprite> textureGetter;

		private OverrideHandler(
				ItemOverrides nested,
				ModelBaker bakery,
				IGeometryBakingContext owner,
				Function<Material, TextureAtlasSprite> textureGetter
		)
		{
			this.nested = nested;
			this.bakery = bakery;
			this.owner = owner;
			this.textureGetter = textureGetter;
		}

		@Nullable
		public BakedModel resolve(
				@Nonnull BakedModel model, @Nonnull ItemStack stack, @Nullable ClientLevel world,
				@Nullable LivingEntity livingEntity, int unused
		)
		{
			final FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);
			if(fluid.isEmpty())
				return nested.resolve(model, stack, world, livingEntity, unused);
			int color = GuiHelper.getFluidColor(fluid);
			// flip to AGBR, because Mojang I guess
			color = (color&0xFF00FF00) // alpha and green same spot
					|((color>>16)&0x000000FF) // red moves to blue
					|((color<<16)&0x00FF0000); // blue moves to red
			return coloredModels.computeIfAbsent(color, i -> new PotionBucketModel(i).bake(
					owner, bakery, textureGetter,
					BlockModelRotation.X0_Y0, this
			));
		}
	}
}
