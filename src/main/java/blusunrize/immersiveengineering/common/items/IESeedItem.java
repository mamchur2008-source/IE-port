/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

// TODO ItemNameBlockItem
public class IESeedItem extends BlockItem
{
	public IESeedItem(Block cropBlock)
	{
		super(cropBlock, IEItems.defaultProperties());
	}

}
