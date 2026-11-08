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
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.common.register.IEItems.Molds;
import blusunrize.immersiveengineering.common.util.InventoryCraftingFalse;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MetalPressPackingRecipes
{
	public static final CachedRecipeList<CraftingRecipe> CRAFTING_RECIPE_MAP = new CachedRecipeList<>(() -> RecipeType.CRAFTING);
	public static final Identifier UNPACK_ID = ImmersiveEngineering.rl("unpacking");
	public static final Identifier PACK4_ID = ImmersiveEngineering.rl("packing4");
	public static final Identifier PACK9_ID = ImmersiveEngineering.rl("packing9");
	// TODO clear at recipe reload!
	private static final HashMap<ComparableItemStack, RecipeHolder<MetalPressRecipe>> UNPACKING_CACHE = new HashMap<>();

	public static void init()
	{
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/packing2x2"),
				new MetalPressPackingRecipe(Molds.MOLD_PACKING_4.asItem(), 2)
		);
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/packing3x3"),
				new MetalPressPackingRecipe(Molds.MOLD_PACKING_9.asItem(), 3)
		);
		MetalPressRecipe.addSpecialRecipe(
				IEApi.ieLoc("metalpress/unpacking"),
				new MetalPressContainerRecipe(Molds.MOLD_UNPACKING.asItem())
				{
					protected RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world)
					{
						return getUnpackingCached(input, world);
					}
				}
		);
	}

	public static abstract class MetalPressContainerRecipe extends MetalPressRecipe
	{
		public MetalPressContainerRecipe(Item mold)
		{
			super(TagOutput.EMPTY, new IngredientWithSize(Ingredient.of(Items.BARRIER)), mold, 3200);
		}

		public boolean listInJEI()
		{
			return false;
		}

		public boolean matches(ItemStack mold, ItemStack input, Level world)
		{
			return getRecipeFunction(input, world)!=null;
		}

		public RecipeHolder<MetalPressRecipe> getActualRecipe(Identifier ownId, ItemStack mold, ItemStack input, Level world)
		{
			return getRecipeFunction(input, world);
		}

		protected abstract RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world);
	}

	public static class MetalPressPackingRecipe extends MetalPressContainerRecipe
	{
		private final int size;
		private final Map<ComparableItemStack, RecipeHolder<MetalPressRecipe>> PACKING_CACHE = new HashMap<>();

		public MetalPressPackingRecipe(Item mold, int size)
		{
			super(mold);
			this.size = size;
		}

		public boolean matches(ItemStack mold, ItemStack input, Level world)
		{
			return input.getCount() >= size*size&&super.matches(mold, input, world);
		}

		protected RecipeHolder<MetalPressRecipe> getRecipeFunction(ItemStack input, Level world)
		{
			ComparableItemStack comp = new ComparableItemStack(input, false);
			if(PACKING_CACHE.containsKey(comp))
				return PACKING_CACHE.get(comp);

			int totalSize = size*size;
			comp.copy();
			Pair<RecipeHolder<CraftingRecipe>, ItemStack> out = getPackedOutput(size, input, world);
			if(out==null)
				return null;
			ItemStack outStack = out.getSecond();
			if(outStack.isEmpty())
			{
				PACKING_CACHE.put(comp, null);
				return null;
			}

			RecipeHolder<MetalPressRecipe> delegate = RecipeDelegate.getPacking(out, input.copyWithCount(totalSize), size==3);
			PACKING_CACHE.put(comp, delegate);
			return delegate;
		}
	}

	public static class RecipeDelegate extends MetalPressRecipe
	{
		public final RecipeHolder<CraftingRecipe> baseRecipe;

		private RecipeDelegate(ItemStack output, ItemStack input, Item mold, RecipeHolder<CraftingRecipe> baseRecipe)
		{
			super(new TagOutput(output), IngredientWithSize.of(input), mold, 3200);
			this.baseRecipe = baseRecipe;
		}

		public static RecipeHolder<MetalPressRecipe> getPacking(
				Pair<RecipeHolder<CraftingRecipe>, ItemStack> originalRecipe, ItemStack input, boolean big
		)
		{
			ItemStack output = originalRecipe.getSecond();
			input = input.copyWithCount(big?9: 4);
			return new RecipeHolder<>(
					recipeKey(big?PACK9_ID: PACK4_ID),
					new RecipeDelegate(
							output, input, (big?Molds.MOLD_PACKING_9: Molds.MOLD_PACKING_4).get(),
							originalRecipe.getFirst()
					)
			);
		}

		public static RecipeHolder<MetalPressRecipe> getUnpacking(
				Pair<RecipeHolder<CraftingRecipe>, ItemStack> originalRecipe, ItemStack input
		)
		{
			ItemStack output = originalRecipe.getSecond();
			return new RecipeHolder<>(
					recipeKey(UNPACK_ID),
					new RecipeDelegate(output, input, Molds.MOLD_UNPACKING.get(), originalRecipe.getFirst())
			);
		}

		public boolean listInJEI()
		{
			return false;
		}
	}

	@Nullable
	public static RecipeHolder<MetalPressRecipe> getRecipeDelegate(
			RecipeHolder<CraftingRecipe> recipeHolder, Identifier id, RegistryAccess access
	)
	{
		return null;
	}

	public static Pair<RecipeHolder<CraftingRecipe>, ItemStack> getPackedOutput(int gridSize, ItemStack stack, Level world)
	{
		return null;
	}

	private static ResourceKey<Recipe<?>> recipeKey(Identifier id)
	{
		return ResourceKey.create(Registries.RECIPE, id);
	}

	private static RecipeHolder<MetalPressRecipe> getUnpackingCached(ItemStack input, Level world)
	{
		ComparableItemStack comp = new ComparableItemStack(input, false);
		if(UNPACKING_CACHE.containsKey(comp))
			return UNPACKING_CACHE.get(comp);

		comp.copy();
		Pair<RecipeHolder<CraftingRecipe>, ItemStack> out = getPackedOutput(1, input, world);
		if(out==null)
			return null;
		ItemStack outStack = out.getSecond();

		int count = outStack.getCount();
		if(count!=4&&count!=9)
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		Pair<RecipeHolder<CraftingRecipe>, ItemStack> rePacked = getPackedOutput(count==4?2: 3, outStack, world);
		ItemStack singleInput = input.copyWithCount(1);
		if(rePacked==null||rePacked.getSecond().isEmpty()||!ItemStack.matches(singleInput, rePacked.getSecond()))
		{
			UNPACKING_CACHE.put(comp, null);
			return null;
		}

		RecipeHolder<MetalPressRecipe> delegate = RecipeDelegate.getUnpacking(out, singleInput);
		UNPACKING_CACHE.put(comp, delegate);
		return delegate;
	}
}
