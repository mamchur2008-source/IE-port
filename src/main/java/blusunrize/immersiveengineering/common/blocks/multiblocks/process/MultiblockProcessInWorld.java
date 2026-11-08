/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext.ProcessContextInWorld;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.BiFunction;

public class MultiblockProcessInWorld<R extends MultiblockRecipe>
		extends MultiblockProcess<R, ProcessContextInWorld<R>>
{
	public NonNullList<ItemStack> inputItems;
	protected float transformationPoint;

	public MultiblockProcessInWorld(
			Identifier recipeId, BiFunction<Level, Identifier, R> getRecipe,
			float transformationPoint, NonNullList<ItemStack> inputItem
	)
	{
		super(recipeId, getRecipe);
		this.inputItems = inputItem;
		this.transformationPoint = transformationPoint;
	}

	public MultiblockProcessInWorld(
			RecipeHolder<R> recipe, float transformationPoint, NonNullList<ItemStack> inputItem
	)
	{
		super(recipe);
		this.inputItems = inputItem;
		this.transformationPoint = transformationPoint;
	}

	public MultiblockProcessInWorld(
			BiFunction<Level, Identifier, R> getRecipe, CompoundTag data, Provider provider
	)
	{
		super(getRecipe, data);
		this.inputItems = NonNullList.withSize(data.getIntOr("numInputs", 0), ItemStack.EMPTY);
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(data, this.inputItems, provider);
		this.transformationPoint = data.getFloatOr("process_transformationPoint", 0);
	}

	public MultiblockProcessInWorld(RecipeHolder<R> recipe, ItemStack input)
	{
		this(recipe, 0.5f, Utils.createNonNullItemStackListFromItemStack(input));
	}

	public List<ItemStack> getDisplayItem(Level level)
	{
		LevelDependentData<R> levelData = getLevelData(level);
		if(processTick/(float)levelData.maxTicks() > transformationPoint&&levelData.recipe()!=null)
		{
			List<ItemStack> list = levelData.recipe().getItemOutputs();
			if(!list.isEmpty())
				return list;
		}
		return inputItems;
	}

	public void writeExtraDataToNBT(CompoundTag nbt, Provider provider)
	{
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, inputItems, provider);
		nbt.putInt("numInputs", inputItems.size());
		nbt.putFloat("process_transformationPoint", transformationPoint);
	}

	protected boolean canOutputItem(ProcessContextInWorld<R> context, ItemStack output)
	{
		return true;
	}

	protected boolean canOutputFluid(ProcessContextInWorld<R> context, FluidStack output)
	{
		return false;
	}

	protected void outputItem(ProcessContextInWorld<R> context, ItemStack output, IMultiblockLevel level)
	{
		context.doProcessOutput(output, level);
	}

	protected void outputFluid(ProcessContextInWorld<R> context, FluidStack output)
	{
		context.doProcessFluidOutput(output);
	}

	protected void processFinish(ProcessContextInWorld<R> context, IMultiblockLevel level)
	{
		super.processFinish(context, level);
		int size = -1;

		R recipe = getLevelData(level.getRawLevel()).recipe();
		if(recipe==null)
			return;
		for(ItemStack inputItem : this.inputItems)
		{
			for(IngredientWithSize s : recipe.getItemInputs())
				if(s.test(inputItem))
				{
					size = s.getCount();
					break;
				}

			if(size > 0&&inputItem.getCount() > size)
			{
				inputItem.split(size);
				processTick = 0;
				clearProcess = false;
			}
		}
	}
}
