/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Implement in IBakedModel when using dynamic split models. Models with equivalent cache keys will only be queried
 * and split once
 */
public interface ICacheKeyProvider<K>
{
	List<BakedQuad> getQuads(K key);

	@Nullable
	K getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	);
}
