/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractFluidAwareRecipe.IMatchLocation;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import javax.annotation.Nonnull;
import java.util.Optional;
import net.minecraft.world.item.Items;

public abstract class AbstractShapedRecipe<MatchLocation extends IMatchLocation>
		extends AbstractFluidAwareRecipe<MatchLocation>
{
	private static final Ingredient EMPTY_PLACEHOLDER = Ingredient.of(Items.BARRIER);
	private final int recipeWidth;
	private final int recipeHeight;

	public AbstractShapedRecipe(ShapedRecipe vanilla)
	{
		this(
				vanilla.group(),
				vanilla.getWidth(), vanilla.getHeight(),
				vanilla.assemble(null), vanilla.category(),
				null
		);
	}

	public AbstractShapedRecipe(
			String groupIn, int recipeWidth, int recipeHeight, ItemStack recipeOutput,
			CraftingBookCategory category, ShapedRecipePattern pattern
	)
	{
		super(groupIn, ingredientsFromPattern(recipeWidth, recipeHeight, pattern), recipeOutput);
		this.recipeWidth = recipeWidth;
		this.recipeHeight = recipeHeight;
	}

	private static NonNullList<Ingredient> ingredientsFromPattern(int width, int height, ShapedRecipePattern pattern)
	{
		NonNullList<Ingredient> result = NonNullList.withSize(width*height, emptyIngredient());
		if(pattern!=null)
			for(int i = 0; i < Math.min(result.size(), pattern.ingredients().size()); ++i)
				result.set(i, pattern.ingredients().get(i).orElse(emptyIngredient()));
		return result;
	}

	protected static Ingredient emptyIngredient()
	{
		return EMPTY_PLACEHOLDER;
	}

	protected static boolean isEmptyIngredient(Ingredient ingredient)
	{
		return ingredient==EMPTY_PLACEHOLDER;
	}

	public int getWidth()
	{
		return this.recipeWidth;
	}

	public int getHeight()
	{
		return this.recipeHeight;
	}

	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= this.recipeWidth&&height >= this.recipeHeight;
	}

	@Nonnull
	public RecipeSerializer<? extends CraftingRecipe> getSerializer()
	{
		return RecipeSerializers.IE_SHAPED_SERIALIZER.get();
	}

	public ShapedRecipe toVanilla()
	{
		return null;
	}

	public boolean isIncomplete()
	{
		NonNullList<Ingredient> nonnulllist = getIngredients();
		if(nonnulllist.isEmpty())
			return true;
		else
			return nonnulllist.stream()
					.filter(ingredient -> !isEmptyIngredient(ingredient)&&!ingredient.isEmpty())
					.anyMatch(ingredient -> !ingredient.items().findAny().isPresent());
	}
}
