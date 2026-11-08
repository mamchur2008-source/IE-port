/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ShaderInventory implements Container
{
	private ShaderWrapper wrapper;
	private AbstractContainerMenu container;
	@Nonnull
	public ItemStack shader;
	private Identifier name;

	public ShaderInventory(AbstractContainerMenu par1Container, ShaderWrapper wrapper)
	{
		this.container = par1Container;
		this.wrapper = wrapper;
		this.shader = ShaderRegistry.makeShaderStack(wrapper.getShader());
		this.name = wrapper.getShaderType();
	}


	public int getContainerSize()
	{
		return 1;
	}

	public boolean isEmpty()
	{
		return this.shader.isEmpty();
	}

	public ItemStack getItem(int i)
	{
		return this.shader;
	}

	public ItemStack removeItemNoUpdate(int i)
	{
		if(!this.shader.isEmpty())
		{
			ItemStack itemstack = this.shader.copy();
			this.shader = ItemStack.EMPTY;
			return itemstack;
		}
		return ItemStack.EMPTY;
	}

	public ItemStack removeItem(int i, int j)
	{
		if(!this.shader.isEmpty())
		{
			ItemStack itemstack;
			if(shader.getCount() <= j)
			{
				itemstack = this.shader.copy();
				this.shader = ItemStack.EMPTY;
				this.setChanged();
				this.container.slotsChanged(this);
				return itemstack;
			}
			itemstack = this.shader.split(j);

			if(shader.getCount()==0)
				this.shader = ItemStack.EMPTY;
			this.container.slotsChanged(this);
			return itemstack;
		}
		return ItemStack.EMPTY;
	}


	public void setItem(int i, ItemStack stack)
	{
		this.shader = stack;
		if(!stack.isEmpty()&&stack.getCount() > this.getMaxStackSize())
			stack.setCount(this.getMaxStackSize());
		this.container.slotsChanged(this);
	}

	public int getMaxStackSize()
	{
		return 64;
	}

	public void setChanged()
	{
		if(wrapper!=null)
			if(shader.getItem() instanceof IShaderItem shaderItem)
				wrapper.setShader(shaderItem.getShaderName());
			else
				wrapper.setShader(null);
	}

	public boolean stillValid(Player entityplayer)
	{
		return true;
	}

	public void startOpen(Player player)
	{
	}

	public void stopOpen(Player player)
	{
	}

	public boolean canPlaceItem(int i, ItemStack itemstack)
	{
		return true;
	}

	public void clearContent()
	{
	}
}
