/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class EmptyContainer implements Container
{
	public static final Container INSTANCE = new EmptyContainer();

	private EmptyContainer()
	{
	}

	public int getContainerSize()
	{
		return 1;
	}

	public boolean isEmpty()
	{
		return true;
	}

	@Nonnull
	public ItemStack getItem(int p_18941_)
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	public ItemStack removeItem(int p_18942_, int p_18943_)
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	public ItemStack removeItemNoUpdate(int p_18951_)
	{
		return ItemStack.EMPTY;
	}

	public void setItem(int p_18944_, @Nonnull ItemStack p_18945_)
	{

	}

	public void setChanged()
	{

	}

	public boolean stillValid(@Nonnull Player p_18946_)
	{
		return true;
	}

	public void clearContent()
	{

	}
}
