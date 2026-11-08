/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface INoContainersRecipe extends IECraftingRecipe
{
	CraftingRecipe baseRecipe();

	default boolean matches(@Nonnull CraftingInput pContainer, @Nonnull Level pLevel)
	{
		return baseRecipe().matches(pContainer, pLevel);
	}

	@Nonnull
	default ItemStack assemble(@Nonnull CraftingInput pContainer)
	{
		return baseRecipe().assemble(pContainer);
	}

	default boolean canCraftInDimensions(int pWidth, int pHeight)
	{
		return true;
	}

	@Nonnull
	default ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	default RecipeSerializer<? extends CraftingRecipe> getSerializer()
	{
		return RecipeSerializers.NO_CONTAINER_SERIALIZER.get();
	}

	@Nonnull
	default NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInput pContainer)
	{
		return NonNullList.withSize(pContainer.size(), ItemStack.EMPTY);
	}

	@Nonnull
	default NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.create();
	}

	default boolean isSpecial()
	{
		return baseRecipe().isSpecial();
	}

	@Nonnull
	default String group()
	{
		return baseRecipe().group();
	}

	default CraftingBookCategory category()
	{
		return baseRecipe().category();
	}
}
