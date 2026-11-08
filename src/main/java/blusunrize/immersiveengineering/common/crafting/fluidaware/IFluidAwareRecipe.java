/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.IECraftingRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractFluidAwareRecipe.IMatchLocation;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IFluidAwareRecipe<MatchLocation extends IMatchLocation> extends IECraftingRecipe
{
	@Nonnull
	NonNullList<Ingredient> getIngredients();

	@Nonnull
	ItemStack getResultItem(Provider access);

	@Nonnull
	default NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInput inv)
	{
		return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
	}

	@Nullable
	MatchLocation findMatch(CraftingInput inv);

	default boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn)
	{
		return findMatch(inv)!=null;
	}

	@Nonnull
	default ItemStack assemble(@Nonnull CraftingInput inv)
	{
		return this.getResultItem(null).copy();
	}
}
