/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

import javax.annotation.Nonnull;
import java.util.List;

public class InventoryCraftingFalse
{
	private static final AbstractContainerMenu NULL_CONTAINER = new AbstractContainerMenu(MenuType.CRAFTING, 0)
	{
		public ItemStack quickMoveStack(Player p_38941_, int p_38942_)
		{
			return ItemStack.EMPTY;
		}

		public void slotsChanged(Container paramIInventory)
		{
		}

		public boolean stillValid(@Nonnull Player playerIn)
		{
			return false;
		}
	};

	public static CraftingInput createFilledCraftingInventory(int w, int h, List<ItemStack> stacks)
	{
		CraftingContainer invC = new TransientCraftingContainer(NULL_CONTAINER, w, h);
		for(int j = 0; j < w*h; j++)
			if(!stacks.get(j).isEmpty())
				invC.setItem(j, stacks.get(j).copy());
		return invC.asCraftInput();
	}
}
