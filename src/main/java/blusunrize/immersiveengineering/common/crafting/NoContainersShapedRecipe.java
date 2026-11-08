/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NoContainersShapedRecipe<T extends ShapedRecipe> implements INoContainersRecipe
{
	private final T baseRecipe;

	public NoContainersShapedRecipe(T baseRecipe)
	{
		this.baseRecipe = baseRecipe;
	}

	public int getWidth()
	{
		return baseRecipe.getWidth();
	}

	public int getHeight()
	{
		return baseRecipe.getHeight();
	}

	public T baseRecipe()
	{
		return baseRecipe;
	}

	public RecipeSerializer<? extends CraftingRecipe> getSerializer()
	{
		return INoContainersRecipe.super.getSerializer();
	}
}
