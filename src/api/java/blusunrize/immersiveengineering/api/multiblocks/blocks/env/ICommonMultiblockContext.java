/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.util.function.Supplier;

@NonExtendable
public interface ICommonMultiblockContext
{
	@SuppressWarnings("rawtypes")
	default Supplier getCapabilityAt(BlockCapability capability, MultiblockFace face)
	{
		return getCapabilityAt(capability, face.posInMultiblock(), face.face());
	}

	@SuppressWarnings("rawtypes")
	Supplier getCapabilityAt(
			BlockCapability capability, BlockPos posRelativeToMB, RelativeBlockFace face
	);

	@SuppressWarnings("rawtypes")
	default Supplier getVoidCapabilityAt(
			BlockCapability capability, BlockPos posRelativeToMB
	)
	{
		return getCapabilityAt(capability, posRelativeToMB, null);
	}

	@SuppressWarnings("rawtypes")
	Supplier getCapabilityAt(
			BlockCapability capability, BlockPos posRelativeToMB, Object context
	);
}
