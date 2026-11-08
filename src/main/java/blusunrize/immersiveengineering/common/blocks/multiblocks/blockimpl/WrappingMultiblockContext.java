/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;

import java.util.function.BooleanSupplier;

public record WrappingMultiblockContext<State>(
		IMultiblockContext<?> inner, State ownState
) implements IMultiblockContext<State>
{
	public void markMasterDirty()
	{
		inner.markMasterDirty();
	}

	public State getState()
	{
		return ownState;
	}

	public IMultiblockLevel getLevel()
	{
		return inner.getLevel();
	}

	public BooleanSupplier isValid()
	{
		return inner.isValid();
	}

	public void requestMasterBESync()
	{
		inner.requestMasterBESync();
	}

	public void setComparatorOutputFor(BlockPos posInMultiblock, int newValue)
	{
		inner.setComparatorOutputFor(posInMultiblock, newValue);
	}

	public int getRedstoneInputValue(BlockPos posInMultiblock, RelativeBlockFace side, int fallback)
	{
		return inner.getRedstoneInputValue(posInMultiblock, side, fallback);
	}

	public int getRedstoneInputValue(BlockPos posInMultiblock, int fallback)
	{
		return inner.getRedstoneInputValue(posInMultiblock, fallback);
	}
}
