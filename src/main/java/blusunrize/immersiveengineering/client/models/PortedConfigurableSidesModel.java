/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public record PortedConfigurableSidesModel(
		String type,
		Identifier baseName,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();
	public static final MapCodec<UnbakedBlockStateModel> BLOCK_STATE_CODEC = UnbakedBlockStateModel.MAP_CODEC;

	@Override
	public TextureSlots.Data textureSlots()
	{
		return textureSlots;
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		return bakeGeometry(textureSlots, modelBaker, modelState, name, defaultConfig(IOSideConfig.NONE));
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name,
			Map<Direction, IOSideConfig> sideConfig
	)
	{
		QuadCollection.Builder result = new QuadCollection.Builder();
		Transformation transform = modelState.transformation().blockCenterToCorner();
		putFace(result, transform, Direction.DOWN, new Vec3[]{
				new Vec3(0, 0, 0), new Vec3(0, 0, 1), new Vec3(1, 0, 1), new Vec3(1, 0, 0)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.DOWN, sideConfig, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.UP, new Vec3[]{
				new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, 0)
		}, new double[]{0, 0, 16, 16}, false, spriteFor(Direction.UP, sideConfig, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.NORTH, new Vec3[]{
				new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 0)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.NORTH, sideConfig, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.SOUTH, new Vec3[]{
				new Vec3(1, 0, 1), new Vec3(1, 1, 1), new Vec3(0, 1, 1), new Vec3(0, 0, 1)
		}, new double[]{16, 16, 0, 0}, false, spriteFor(Direction.SOUTH, sideConfig, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.WEST, new Vec3[]{
				new Vec3(0, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(0, 0, 1)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.WEST, sideConfig, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.EAST, new Vec3[]{
				new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(1, 1, 1), new Vec3(1, 0, 1)
		}, new double[]{16, 16, 0, 0}, false, spriteFor(Direction.EAST, sideConfig, textureSlots, modelBaker, name));
		return result.build();
	}

	private TextureAtlasSprite spriteFor(
			Direction side, Map<Direction, IOSideConfig> sideConfig, TextureSlots textureSlots,
			ModelBaker modelBaker, ModelDebugName debugName
	)
	{
		Identifier texture = textureFor(side, sideConfig.getOrDefault(side, IOSideConfig.NONE));
		Material.Baked material = modelBaker.materials().resolveSlot(textureSlots, texture.toString(), debugName);
		return material.sprite();
	}

	private Material.Baked materialFor(
			Direction side, IOSideConfig cfg, TextureSlots textureSlots, ModelBaker modelBaker,
			ModelDebugName debugName
	)
	{
		Identifier texture = textureFor(side, cfg);
		return modelBaker.materials().resolveSlot(textureSlots, texture.toString(), debugName);
	}

	private Identifier textureFor(Direction side, IOSideConfig cfg)
	{
		return baseName.withSuffix("_"+textureName(side, cfg));
	}

	private String textureName(Direction side, IOSideConfig cfg)
	{
		String sideName = switch(type)
		{
			case "side_top_bottom" -> side.getAxis()==Direction.Axis.Y?side.getSerializedName(): "side";
			case "side_vertical" -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case "vertical" -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case "all_same_texture" -> "side";
			default -> side.getSerializedName();
		};
		if("vertical".equals(type)&&side.getAxis()!=Direction.Axis.Y)
			return sideName;
		return sideName+"_"+cfg.getTextureName();
	}

	private static void putFace(
			QuadCollection.Builder result, Transformation transform, Direction direction, Vec3[] vertices,
			double[] uv, boolean invert, TextureAtlasSprite sprite
	)
	{
		Vec3[] transformed = new Vec3[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			transformed[i] = transformPosition(transform, vertices[i]);
		BakedQuad quad = ModelUtils.createBakedQuad(transformed, direction, sprite, uv, new float[]{1, 1, 1, 1}, invert);
		result.addCulledFace(direction, quad);
	}

	private static Vec3 transformPosition(Transformation transform, Vec3 position)
	{
		Vector4f transformed = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
		transform.transformPosition(transformed);
		transformed.mul(1/transformed.w());
		return new Vec3(
				snap(transformed.x()),
				snap(transformed.y()),
				snap(transformed.z())
		);
	}

	private static float snap(float value)
	{
		final float epsilon = 1e-5f;
		if(Math.abs(value) < epsilon)
			return 0;
		if(Math.abs(value-1) < epsilon)
			return 1;
		return value;
	}

	private static Map<Direction, IOSideConfig> defaultConfig(IOSideConfig config)
	{
		EnumMap<Direction, IOSideConfig> result = new EnumMap<>(Direction.class);
		for(Direction d : DirectionUtils.VALUES)
			result.put(d, config);
		return Collections.unmodifiableMap(result);
	}

	private static Map<Direction, IOSideConfig> copyConfig(@Nullable Map<Direction, IOSideConfig> source)
	{
		EnumMap<Direction, IOSideConfig> result = new EnumMap<>(Direction.class);
		for(Direction d : DirectionUtils.VALUES)
			result.put(d, source!=null?source.getOrDefault(d, IOSideConfig.NONE): IOSideConfig.NONE);
		return Collections.unmodifiableMap(result);
	}

	private static @Nullable PortedConfigurableSidesModel findConfigurableSidesModel(ResolvedModel model)
	{
		ResolvedModel current = model;
		while(current!=null)
		{
			if(current.wrapped() instanceof PortedConfigurableSidesModel configurableSidesModel)
				return configurableSidesModel;
			current = current.parent();
		}
		return null;
	}

	public record UnbakedBlockStateModel(
			Identifier model,
			Variant.SimpleModelState modelState
	) implements CustomUnbakedBlockStateModel
	{
		public static final MapCodec<UnbakedBlockStateModel> MAP_CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						Identifier.CODEC.fieldOf("model").forGetter(UnbakedBlockStateModel::model),
						Variant.SimpleModelState.MAP_CODEC.forGetter(UnbakedBlockStateModel::modelState)
				).apply(instance, UnbakedBlockStateModel::new)
		);

		@Override
		public BlockStateModel bake(ModelBaker modelBaker)
		{
			ResolvedModel resolvedModel = modelBaker.getModel(model);
			PortedConfigurableSidesModel configurableSidesModel = findConfigurableSidesModel(resolvedModel);
			ModelState finalTransform = modelState.asModelState();
			if(configurableSidesModel==null)
				return new SingleVariant(SimpleModelWrapper.bake(modelBaker, model, finalTransform));

			TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
			boolean ambientOcclusion = resolvedModel.getTopAmbientOcclusion();
			Material.Baked particleMaterial = resolvedModel.resolveParticleMaterial(textureSlots, modelBaker);
			return new ConfigurableSidesBlockStateModel(
					configurableSidesModel, textureSlots, modelBaker, finalTransform, () -> model.toString(),
					ambientOcclusion, particleMaterial
			);
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver)
		{
			resolver.markDependency(model);
		}

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec()
		{
			return MAP_CODEC;
		}
	}

	private static class ConfigurableSidesBlockStateModel implements DynamicBlockStateModel
	{
		private final PortedConfigurableSidesModel configurableSidesModel;
		private final TextureSlots textureSlots;
		private final ModelBaker modelBaker;
		private final ModelState modelState;
		private final ModelDebugName debugName;
		private final boolean ambientOcclusion;
		private final Material.Baked defaultParticle;
		private final LoadingCache<Map<Direction, IOSideConfig>, BlockStateModelPart> partCache;

		private ConfigurableSidesBlockStateModel(
				PortedConfigurableSidesModel configurableSidesModel, TextureSlots textureSlots,
				ModelBaker modelBaker, ModelState modelState, ModelDebugName debugName,
				boolean ambientOcclusion, Material.Baked defaultParticle
		)
		{
			this.configurableSidesModel = configurableSidesModel;
			this.textureSlots = textureSlots;
			this.modelBaker = modelBaker;
			this.modelState = modelState;
			this.debugName = debugName;
			this.ambientOcclusion = ambientOcclusion;
			this.defaultParticle = defaultParticle;
			this.partCache = CacheBuilder.newBuilder()
					.expireAfterAccess(60, TimeUnit.SECONDS)
					.build(CacheLoader.from(this::bakePart));
		}

		@Override
		public void collectParts(RandomSource random, List<BlockStateModelPart> output)
		{
			output.add(getPart(defaultConfig(IOSideConfig.NONE)));
		}

		@Override
		public void collectParts(
				BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
				List<BlockStateModelPart> parts
		)
		{
			parts.add(getPart(getConfig(level, pos)));
		}

		@Override
		public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random)
		{
			return new GeometryKey(this, getConfig(level, pos));
		}

		@Override
		public Material.Baked particleMaterial()
		{
			return defaultParticle;
		}

		@Override
		public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state)
		{
			return getPart(getConfig(level, pos)).particleMaterial();
		}

		@Override
		public int materialFlags()
		{
			return getPart(defaultConfig(IOSideConfig.NONE)).materialFlags();
		}

		@Override
		public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state)
		{
			return getPart(getConfig(level, pos)).materialFlags();
		}

		private BlockStateModelPart getPart(Map<Direction, IOSideConfig> config)
		{
			return partCache.getUnchecked(copyConfig(config));
		}

		private BlockStateModelPart bakePart(Map<Direction, IOSideConfig> config)
		{
			QuadCollection quads = configurableSidesModel.bakeGeometry(
					textureSlots, modelBaker, modelState, debugName, config
			);
			IOSideConfig downConfig = config.getOrDefault(Direction.DOWN, IOSideConfig.NONE);
			Material.Baked particle = configurableSidesModel.materialFor(
					Direction.DOWN, downConfig, textureSlots, modelBaker, debugName
			);
			return new StaticPart(quads, ambientOcclusion, particle);
		}

		private Map<Direction, IOSideConfig> getConfig(BlockAndTintGetter level, BlockPos pos)
		{
			ModelData modelData = level.getModelData(pos);
			Map<Direction, IOSideConfig> fromModelData = modelData.get(Model.SIDECONFIG);
			if(fromModelData!=null)
				return copyConfig(fromModelData);

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof IConfigurableSides configurableSides)
			{
				EnumMap<Direction, IOSideConfig> config = new EnumMap<>(Direction.class);
				for(Direction d : DirectionUtils.VALUES)
					config.put(d, configurableSides.getSideConfig(d));
				return Collections.unmodifiableMap(config);
			}
			return defaultConfig(IOSideConfig.NONE);
		}
	}

	private record GeometryKey(
			ConfigurableSidesBlockStateModel model,
			Map<Direction, IOSideConfig> config
	)
	{
	}

	private record StaticPart(
			QuadCollection quads,
			boolean useAmbientOcclusion,
			Material.Baked particleMaterial
	) implements BlockStateModelPart
	{
		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction)
		{
			return quads.getQuads(direction);
		}

		@Override
		public int materialFlags()
		{
			return quads.materialFlags();
		}
	}

	public static class Loader implements UnbakedModelLoader<PortedConfigurableSidesModel>
	{
		@Override
		public PortedConfigurableSidesModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			String type = modelContents.get("type").getAsString();
			Identifier baseName = Identifier.parse(modelContents.get("base_name").getAsString());
			TextureSlots.Data textures = TextureSlots.parseTextureMap(collectTextureSlots(modelContents, type, baseName));
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			return new PortedConfigurableSidesModel(type, baseName, textures, transforms, parent);
		}

		private static JsonObject collectTextureSlots(JsonObject modelContents, String type, Identifier baseName)
		{
			JsonObject textures = modelContents.has("textures")?
					modelContents.getAsJsonObject("textures").deepCopy():
					new JsonObject();
			PortedConfigurableSidesModel namer = new PortedConfigurableSidesModel(
					type, baseName, TextureSlots.Data.EMPTY, null, null
			);
			for(Direction side : DirectionUtils.VALUES)
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					Identifier texture = namer.textureFor(side, cfg);
					if(!textures.has(texture.toString()))
						textures.addProperty(texture.toString(), texture.toString());
				}
			Identifier particle = namer.textureFor(Direction.DOWN, IOSideConfig.NONE);
			if(!textures.has("particle"))
				textures.addProperty("particle", particle.toString());
			return textures;
		}
	}
}
