package net.minecraft.client.resources.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.client.renderer.block.model.ItemOverrides;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface BakedModel
{
	@Nonnull
	default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand)
	{
		return List.of();
	}

	@Nonnull
	default List<BakedQuad> getQuads(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType renderType
	)
	{
		return getQuads(state, side, rand);
	}

	default boolean useAmbientOcclusion()
	{
		return true;
	}

	default TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType)
	{
		return TriState.DEFAULT;
	}

	default boolean isGui3d()
	{
		return true;
	}

	default boolean usesBlockLight()
	{
		return true;
	}

	default boolean isCustomRenderer()
	{
		return false;
	}

	default TextureAtlasSprite getParticleIcon()
	{
		return null;
	}

	default TextureAtlasSprite getParticleIcon(@Nonnull ModelData data)
	{
		return getParticleIcon();
	}

	default ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	default ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	default ModelData getModelData(
			@Nonnull BlockAndTintGetter world,
			@Nonnull BlockPos pos,
			@Nonnull BlockState state,
			@Nonnull ModelData tileData
	)
	{
		return tileData;
	}

	default List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous)
	{
		return List.of(RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS));
	}

	default ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data)
	{
		return ChunkRenderTypeSet.of(RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS));
	}

	default List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous)
	{
		return List.of(this);
	}

	default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack stack, boolean applyLeftHandTransform)
	{
		return this;
	}
}
