/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.BlockPos;

public class ShelfMultiblock extends IETemplateMultiblock
{
	public ShelfMultiblock()
	{
		super(IEApi.ieLoc("multiblocks/shelf"),
				new BlockPos(0, 0, 0), new BlockPos(1, 1, 1), new BlockPos(4, 4, 2),
				IEMultiblockLogic.SHELF);
	}

	public float getManualScale()
	{
		return 13;
	}
}
