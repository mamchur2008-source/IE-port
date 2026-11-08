/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class LazyShapelessRecipe implements IECraftingRecipe
{
	private final String group;
	private final TagOutput result;
	private final NonNullList<Ingredient> ingredients;
	private final RecipeSerializer<LazyShapelessRecipe> serializer;

	public LazyShapelessRecipe(
			String groups, TagOutput result, NonNullList<Ingredient> ingredients, IERecipeSerializer<LazyShapelessRecipe> serializer
	)
	{
		this.group = groups;
		this.result = result;
		this.ingredients = ingredients;
		this.serializer = serializer.serializer();
	}

	public boolean matches(@Nonnull CraftingInput input, @Nonnull Level level)
	{
		return false;
	}

	@Nonnull
	public ItemStack getResultItem(Provider access)
	{
		return result.get();
	}

	@Nonnull
	public ItemStack assemble(@Nonnull CraftingInput p_44260_)
	{
		return result.get().copy();
	}

	@Nonnull
	public NonNullList<Ingredient> getIngredients()
	{
		return ingredients;
	}

	@Nonnull
	public String group()
	{
		return group;
	}

	@Nonnull
	public RecipeSerializer<LazyShapelessRecipe> getSerializer()
	{
		return serializer;
	}

	public TagOutput getResult()
	{
		return result;
	}
}
