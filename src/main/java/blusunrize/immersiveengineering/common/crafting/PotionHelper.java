/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionBottleType;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.mixin.accessors.PotionBrewingAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PotionHelper
{
	public static BiFunction<Holder<Potion>, PotionBottleType, FluidIngredient> CREATE_POTION_BUILDER;

	public static SizedFluidIngredient getFluidIngredientForType(Holder<Potion> type, int amount, PotionBottleType potionBottleType)
	{
		if((type==Potions.WATER||type==null)&&potionBottleType==PotionBottleType.REGULAR)
			return SizedFluidIngredient.of(Fluids.WATER, amount);
		else
		{
			FluidIngredient fluidIngredient = FluidIngredient.of(IEFluids.POTION.get());

			// Support Create if installed
			if(CREATE_POTION_BUILDER!=null)
				fluidIngredient = CompoundFluidIngredient.of(
						fluidIngredient,
						CREATE_POTION_BUILDER.apply(type, potionBottleType)
				);

			return new SizedFluidIngredient(fluidIngredient, amount);
		}
	}

	public static void applyToAllPotionRecipes(PotionRecipeProcessor out)
	{
		final PotionBrewing brewingData;
		if(ServerLifecycleHooks.getCurrentServer()!=null)
			brewingData = ServerLifecycleHooks.getCurrentServer().potionBrewing();
		else
			brewingData = ImmersiveEngineering.proxy.getClientWorld().potionBrewing();
		// Vanilla
		for(var mixPredicate : ((PotionBrewingAccess)brewingData).getConversions())
			if(mixPredicate.getTo()!=Potions.MUNDANE&&mixPredicate.getTo()!=Potions.THICK)
				out.apply(
						mixPredicate.getTo(), mixPredicate.getFrom(),
						new IngredientWithSize(mixPredicate.getIngredient())
				);

		// Modded
		for(IBrewingRecipe recipe : brewingData.getRecipes())
			if(recipe instanceof BrewingRecipe brewingRecipe)
			{
				IngredientWithSize ingredient = new IngredientWithSize(brewingRecipe.getIngredient());
				Ingredient input = brewingRecipe.getInput();
				ItemStack output = brewingRecipe.getOutput();
				if(output.getItem()==Items.POTION)
					out.apply(getPotion(output), Potions.WATER, ingredient);
			}
	}

	private static Holder<Potion> getPotion(ItemStack potion)
	{
		PotionContents potionData = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		return potionData.potion().orElse(Potions.WATER);
	}

	public interface PotionRecipeProcessor
	{
		void apply(Holder<Potion> output, Holder<Potion> input, IngredientWithSize reagent);
	}
}
