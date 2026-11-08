/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.obj.GeneralIEOBJModel.ModelKey;
import blusunrize.immersiveengineering.client.models.obj.SpecificIEOBJModel.ShadedQuads;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import malte0811.modelsplitter.model.Group;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GeneralIEOBJModel<T> implements BakedModel, ICacheKeyProvider<ModelKey<T>>
{
	private final Cache<GroupKey<T>, List<ShadedQuads>> groupCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();
	private final LoadingCache<ModelKey<T>, SpecificIEOBJModel<T>> modelCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build(CacheLoader.from(p -> new SpecificIEOBJModel<>(this, p.callbackKey(), p.shader(), p.renderTypeIfRelevant())));
	private final IEOBJCallback<T> callback;
	private final OBJModel<OBJMaterial> baseModel;
	private final ChunkRenderTypeSet blockLayers;
	private final List<RenderType> itemTypes;
	private final List<RenderType> fabulousItemTypes;
	private final TextureAtlasSprite particles;
	private final IGeometryBakingContext owner;
	private final Function<Material, TextureAtlasSprite> spriteGetter;
	private final ModelState sprite;
	private final boolean isDynamic;
	private final ItemOverrides overrides;
	private final ModelProperty<T> keyProperty;

	public GeneralIEOBJModel(
			IEOBJCallback<T> callback,
			OBJModel<OBJMaterial> baseModel,
			IGeometryBakingContext owner,
			Function<Material, TextureAtlasSprite> spriteGetter,
			ModelState sprite,
			boolean isDynamic,
			ChunkRenderTypeSet blockLayers,
			List<RenderType> itemTypes,
			List<RenderType> fabulousItemTypes
	)
	{
		this.callback = callback;
		this.baseModel = baseModel;
		this.blockLayers = blockLayers;
		this.itemTypes = itemTypes;
		this.fabulousItemTypes = fabulousItemTypes;
		this.particles = spriteGetter.apply(findParticleMaterial(baseModel, owner));
		this.owner = owner;
		this.spriteGetter = spriteGetter;
		this.sprite = sprite;
		this.isDynamic = isDynamic;
		if(callback instanceof ItemCallback<T> itemCB)
			this.overrides = new Overrides(itemCB);
		else
			this.overrides = new ItemOverrides()
			{
				public BakedModel resolve(
						@Nonnull BakedModel p_173465_, @Nonnull ItemStack p_173466_, @Nullable ClientLevel p_173467_, @Nullable LivingEntity p_173468_, int p_173469_
				)
				{
			return null;
				}
			};
		this.keyProperty = IEOBJCallbacks.getModelProperty(callback);
	}

	public List<BakedQuad> getQuads(ModelKey<T> key)
	{
		if(key==null)
			return ImmutableList.of();
		return modelCache.getUnchecked(key).getQuads(
				null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null
		);
	}

	@Nonnull
	public List<BakedQuad> getQuads(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		return getQuads(getKey(state, side, rand, extraData, layer));
	}

	@Nullable
	public ModelKey<T> getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		if(side!=null)
			return null;
		T key;
		if(extraData.has(keyProperty))
			key = extraData.get(keyProperty);
		else
			key = callback.getDefaultKey();
		boolean includeLayer = BlockCallback.castOrDefault(callback).dependsOnLayer();
		ShaderCase shader = extraData.get(CapabilityShader.MODEL_PROPERTY);
		return new ModelKey<>(key, shader, includeLayer?layer: null);
	}

	@Nonnull
	public ModelData getModelData(
			@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData
	)
	{
		BlockCallback<T> blockCB = BlockCallback.castOrDefault(callback);
		BlockEntity blockEntity = level.getBlockEntity(pos);
		T key = blockCB.extractKey(level, pos, state, blockEntity);
		ModelData.Builder modelData = tileData.derive();
		modelData.with(keyProperty, key);
		if(blockEntity!=null&&blockEntity.getLevel()!=null)
		{
			ShaderWrapper shaderCap = blockEntity.getLevel().getCapability(
					CapabilityShader.BLOCK, blockEntity.getBlockPos(), null
			);
			if(shaderCap!=null)
				modelData.with(CapabilityShader.MODEL_PROPERTY, shaderCap.getCase());
		}
		return modelData.build();
	}

	@Nonnull
	public ChunkRenderTypeSet getRenderTypes(
			@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data
	)
	{
		return blockLayers;
	}

	@Nonnull
	public List<RenderType> getRenderTypes(@Nonnull ItemStack itemStack, boolean fabulous)
	{
		return fabulous?fabulousItemTypes: itemTypes;
	}

	public boolean useAmbientOcclusion()
	{
		return true;
	}

	public boolean isGui3d()
	{
		return true;
	}

	public boolean usesBlockLight()
	{
		return true;
	}

	public boolean isCustomRenderer()
	{
		return isDynamic;
	}

	@Nonnull
	public TextureAtlasSprite getParticleIcon()
	{
		return getParticleIcon(ModelData.EMPTY);
	}

	@Nonnull
	public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data)
	{
		return particles;
	}

	@Nonnull
	public ItemOverrides getOverrides()
	{
		return overrides;
	}

	public Cache<GroupKey<T>, List<ShadedQuads>> getGroupCache()
	{
		return groupCache;
	}

	public Map<String, Group<OBJMaterial>> getGroups()
	{
		return baseModel.getFacesByGroup();
	}

	public OBJModel<OBJMaterial> getBaseModel()
	{
		return baseModel;
	}

	public ModelState getSprite()
	{
		return sprite;
	}

	public Function<Material, TextureAtlasSprite> getSpriteGetter()
	{
		return spriteGetter;
	}

	public IGeometryBakingContext getOwner()
	{
		return owner;
	}

	private static Material findParticleMaterial(OBJModel<OBJMaterial> model, IGeometryBakingContext owner)
	{
		for(Group<OBJMaterial> group : model.getFacesByGroup().values())
			for(Polygon<OBJMaterial> face : group.getFaces())
			{
				OBJMaterial material = face.getTexture();
				if(material!=null)
				{
					Material resolved = resolveMaterial(material.map_Kd(), owner);
					if(resolved!=null)
						return resolved;
				}
			}
		return owner.getMaterial("particle");
	}

	@Nullable
	private static Material resolveMaterial(String name, IGeometryBakingContext owner)
	{
		if(name==null)
			return null;
		if(name.indexOf(':') >= 0)
			return new Material(Identifier.parse(name));
		return owner.getMaterial(name);
	}

	public IEOBJCallback<T> getCallback()
	{
		return callback;
	}

	private class Overrides extends ItemOverrides
	{
		private final ItemCallback<T> callback;

		private Overrides(IEOBJCallback<T> callback)
		{
			this.callback = ItemCallback.castOrDefault(callback);
		}

		@Nullable
		public BakedModel resolve(
				@Nonnull BakedModel baseModel, @Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity holder, int p_173469_
		)
		{
			GlobalTempData.setActiveHolder(holder);
			T key = callback.extractKey(stack, holder);
			ShaderWrapper wrapper = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(stack, CapabilityShader.ITEM);
			ShaderCase shader = wrapper==null?null: wrapper.getCase();
			return modelCache.getUnchecked(new ModelKey<>(key, shader, null));
		}
	}

	public record ModelKey<T>(
			T callbackKey, ShaderCase shader, @Nullable RenderType renderTypeIfRelevant
	)
	{
	}

	public record GroupKey<T>(
			T callbackKey, ShaderCase shader, @Nullable RenderType renderTypeIfRelevant, String group
	)
	{
	}
}
