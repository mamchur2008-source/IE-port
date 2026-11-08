/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.ShelfCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ShelfLogic;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ShelfCallbacks implements BlockCallback<Key>
{
	public static final ShelfCallbacks INSTANCE = new ShelfCallbacks();

	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState blockState, BlockEntity blockEntity)
	{
		if(blockEntity instanceof IMultiblockBE<?> multiblockBE&&
				multiblockBE.getHelper().getState() instanceof ShelfLogic.State state)
			return extractKey(state);
		return getDefaultKey();
	}

	public Key extractKey(ShelfLogic.State state)
	{
		Function<Identifier, TextureAtlasSprite> atlas = ClientUtils::getSprite;
		Map<String, TextureAtlasSprite> texMap = new HashMap<>();
		for(int i = 0; i < state.renderCrates.length; i++)
			if(state.renderCrates[i]!=null)
				texMap.put("crate_"+i, atlas.apply(state.renderCrates[i]));
		return new Key(texMap);
	}

	public Key getDefaultKey()
	{
		return new Key(Collections.emptyMap());
	}

	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		if(group.startsWith("crate_"))
			return key.texMap().containsKey(group);
		return true;
	}

	public @Nullable TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		return key.texMap().get(group);
	}

	public record Key(Map<String, TextureAtlasSprite> texMap)
	{
	}
}
