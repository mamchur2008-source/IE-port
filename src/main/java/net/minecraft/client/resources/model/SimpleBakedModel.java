package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.minecraft.client.renderer.block.model.ItemOverrides;

import java.util.List;
import java.util.Map;

public class SimpleBakedModel implements BakedModel
{
	private final List<BakedQuad> unculledQuads;
	private final Map<Direction, List<BakedQuad>> culledQuads;
	private final boolean ambientOcclusion;
	private final boolean blockLight;
	private final boolean gui3d;
	private final TextureAtlasSprite particle;
	private final ItemTransforms transforms;
	private final ItemOverrides overrides;
	private final ChunkRenderTypeSet blockRenderTypes;
	private final List<RenderType> itemRenderTypes;
	private final List<RenderType> fabulousItemRenderTypes;

	public SimpleBakedModel()
	{
		this(List.of(), Map.of(), true, true, true, null, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY);
	}

	public SimpleBakedModel(
			List<BakedQuad> unculledQuads,
			Map<Direction, List<BakedQuad>> culledQuads,
			boolean ambientOcclusion,
			boolean blockLight,
			boolean gui3d,
			TextureAtlasSprite particle,
			ItemTransforms transforms,
			ItemOverrides overrides
	)
	{
		this(
				unculledQuads, culledQuads, ambientOcclusion, blockLight, gui3d,
				particle, transforms, overrides, RenderTypeGroup.EMPTY
		);
	}

	public SimpleBakedModel(
			List<BakedQuad> unculledQuads,
			Map<Direction, List<BakedQuad>> culledQuads,
			boolean ambientOcclusion,
			boolean blockLight,
			boolean gui3d,
			TextureAtlasSprite particle,
			ItemTransforms transforms,
			ItemOverrides overrides,
			RenderTypeGroup renderTypes
	)
	{
		this.unculledQuads = unculledQuads;
		this.culledQuads = culledQuads;
		this.ambientOcclusion = ambientOcclusion;
		this.blockLight = blockLight;
		this.gui3d = gui3d;
		this.particle = particle;
		this.transforms = transforms;
		this.overrides = overrides;
		this.blockRenderTypes = ChunkRenderTypeSet.of(renderTypes.block());
		this.itemRenderTypes = List.of(renderTypes.item());
		this.fabulousItemRenderTypes = List.of(renderTypes.fabulous());
	}

	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand)
	{
		if(side==null)
			return unculledQuads;
		return culledQuads.getOrDefault(side, List.of());
	}

	public boolean useAmbientOcclusion()
	{
		return ambientOcclusion;
	}

	public boolean usesBlockLight()
	{
		return blockLight;
	}

	public boolean isGui3d()
	{
		return gui3d;
	}

	public TextureAtlasSprite getParticleIcon()
	{
		return particle;
	}

	public ItemTransforms getTransforms()
	{
		return transforms;
	}

	public ItemOverrides getOverrides()
	{
		return overrides;
	}

	public ChunkRenderTypeSet getBlockRenderTypes()
	{
		return blockRenderTypes;
	}

	public List<RenderType> getItemRenderTypes()
	{
		return itemRenderTypes;
	}

	public List<RenderType> getFabulousItemRenderTypes()
	{
		return fabulousItemRenderTypes;
	}
}
