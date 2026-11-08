/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.PositionedItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class ManualRecipeRef
{
	private final ItemStack output;
	private final PositionedItemStack[] layout;
	private final Identifier recipeName;

	public ManualRecipeRef(ItemStack output)
	{
		this.output = Objects.requireNonNull(output);
		this.layout = null;
		this.recipeName = null;
	}

	public ManualRecipeRef(PositionedItemStack[] layout)
	{
		this.output = null;
		this.layout = Objects.requireNonNull(layout);
		this.recipeName = null;
	}

	public ManualRecipeRef(Identifier recipeName)
	{
		this.output = null;
		this.layout = null;
		this.recipeName = Objects.requireNonNull(recipeName);
	}

	public boolean isLayout()
	{
		return layout!=null;
	}

	public PositionedItemStack[] getLayout()
	{
		return Objects.requireNonNull(layout);
	}

	public boolean isResult()
	{
		return output!=null;
	}

	public ItemStack getResult()
	{
		return Objects.requireNonNull(output);
	}

	public boolean isRecipeName()
	{
		return recipeName!=null;
	}

	public Identifier getRecipeName()
	{
		return Objects.requireNonNull(recipeName);
	}

	public <C extends RecipeInput, R extends Recipe<C>>
	void forEachMatchingRecipe(RecipeType<R> type, Consumer<R> out)
	{
		RecipeManager recipeManager = getRecipeManager();
		if(recipeManager==null)
			return;
		if(isRecipeName())
			recipeManager.byKey(ResourceKey.create(Registries.RECIPE, getRecipeName()))
					.ifPresent(recipeHolder -> out.accept((R)recipeHolder.value()));
		else
			for(RecipeHolder<?> recipe : recipeManager.getRecipes())
			{
				if(!recipe.value().getType().equals(type))
					continue;
				if(ManualUtils.stackMatchesObject(
						ManualUtils.getRecipeResult(recipe.value()), getResult()
				))
					out.accept((R)recipe.value());
			}
	}

	@Nullable
	private RecipeManager getRecipeManager()
	{
		Minecraft mc = Minecraft.getInstance();
		if(mc.getSingleplayerServer()!=null)
			return mc.getSingleplayerServer().getRecipeManager();
		return null;
	}
}
