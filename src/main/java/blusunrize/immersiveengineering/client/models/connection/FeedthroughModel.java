package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.BakedIEModel;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FeedthroughModel extends BakedIEModel implements ICacheKeyProvider<Pair<FeedthroughModel.FeedthroughCacheKey, Direction>>
{
	public static final LoadingCache<FeedthroughCacheKey, SpecificFeedthroughModel> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build(CacheLoader.from(key -> new SpecificFeedthroughModel()));

	@Nonnull
	public List<BakedQuad> getQuads(Pair<FeedthroughCacheKey, Direction> key)
	{
		return List.of();
	}

	@Nonnull
	public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData)
	{
		return tileData;
	}

	@Nonnull
	public TextureAtlasSprite getParticleIcon()
	{
		return null;
	}

	@Nonnull
	public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data)
	{
		return null;
	}

	@Nonnull
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	@Nullable
	public Pair<FeedthroughCacheKey, Direction> getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		return Pair.of(new FeedthroughCacheKey(WireType.COPPER, state, 0, Direction.NORTH, layer), side);
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
		return List.of();
	}

	public ChunkRenderTypeSet getRenderTypes(@Nonnull BlockState state, @Nonnull RandomSource rand, @Nonnull ModelData data)
	{
		return ChunkRenderTypeSet.all();
	}

	public static class FeedthroughCacheKey
	{
		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing, RenderType layer)
		{
		}

		public FeedthroughCacheKey(WireType type, BlockState baseState, int offset, Direction facing, RenderType layer, int colorMultiplier)
		{
		}
	}

	public static class SpecificFeedthroughModel extends FeedthroughModel
	{
	}
}
