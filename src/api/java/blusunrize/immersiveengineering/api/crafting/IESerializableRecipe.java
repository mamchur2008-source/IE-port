/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public abstract class IESerializableRecipe implements Recipe<RecipeInput>
{
	protected final TagOutput outputDummy;
	protected final RecipeType<?> type;

	protected <T extends Recipe<?>>
	IESerializableRecipe(TagOutput outputDummy, IERecipeTypes.TypeWithClass<T> type)
	{
		this.outputDummy = outputDummy;
		this.type = type.get();
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public boolean matches(RecipeInput inv, Level worldIn)
	{
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput inv)
	{
		return this.outputDummy.get();
	}

	@Override
	public boolean showNotification()
	{
		return false;
	}

	@Override
	public String group()
	{
		return "";
	}

	@Override
	public PlacementInfo placementInfo()
	{
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory()
	{
		return new RecipeBookCategory();
	}

	@SuppressWarnings("unchecked")
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer()
	{
		return (RecipeSerializer<? extends Recipe<RecipeInput>>)getIESerializer();
	}

	protected abstract RecipeSerializer<?> getIESerializer();

	@Override
	@SuppressWarnings("unchecked")
	public RecipeType<? extends Recipe<RecipeInput>> getType()
	{
		return (RecipeType<? extends Recipe<RecipeInput>>)this.type;
	}
}
