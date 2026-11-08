/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler;
import blusunrize.immersiveengineering.api.tool.assembler.AssemblerHandler.IRecipeAdapter;
import blusunrize.immersiveengineering.api.tool.assembler.RecipeQuery;
import blusunrize.immersiveengineering.common.util.FakePlayerUtil;
import blusunrize.immersiveengineering.common.util.InventoryCraftingFalse;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;

public class DefaultAssemblerAdapter implements IRecipeAdapter<Recipe<CraftingInput>>
{
	public List<RecipeQuery> getQueriedInputs(Recipe<CraftingInput> recipe, NonNullList<ItemStack> input, Level world)
	{
		CraftingInput craftingInput = InventoryCraftingFalse.createFilledCraftingInventory(3, 3, input);
		NonNullList<ItemStack> remains = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

		List<RecipeQuery> queries = new ArrayList<>();
		for(int i = 0; i < craftingInput.size(); i++)
		{
			final RecipeQuery query = AssemblerHandler.createQueryFromItemStack(craftingInput.getItem(i), remains.get(i));
			if(query!=null)
				queries.add(query);
		}
		return queries;
	}
}
