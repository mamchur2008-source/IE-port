/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CompositeBakedModel<T extends BakedModel> implements BakedModel
{
	protected final T base;

	public CompositeBakedModel(T base)
	{
		this.base = base;
	}

	@Nonnull
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand)
	{
		return base.getQuads(state, side, rand, ModelData.EMPTY, null);
	}

	public boolean useAmbientOcclusion()
	{
		return base.useAmbientOcclusion();
	}

	public boolean isGui3d()
	{
		return base.isGui3d();
	}

	public boolean usesBlockLight()
	{
		return base.usesBlockLight();
	}

	public boolean isCustomRenderer()
	{
		return base.isCustomRenderer();
	}

	@Nonnull
	public TextureAtlasSprite getParticleIcon()
	{
		return base.getParticleIcon(ModelData.EMPTY);
	}

	@Nonnull
	public ItemOverrides getOverrides()
	{
		return base.getOverrides();
	}

	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType)
	{
		return base.getQuads(state, side, rand, data, renderType);
	}

	public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType)
	{
		return base.useAmbientOcclusion(state, data, renderType);
	}

	@Nonnull
	public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData)
	{
		return base.getModelData(world, pos, state, tileData);
	}

	public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data)
	{
		return base.getParticleIcon(data);
	}

	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous)
	{
		return base.getRenderTypes(itemStack, fabulous);
	}

	public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data)
	{
		return base.getRenderTypes(state, rand, data);
	}
}
