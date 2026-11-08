/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.crafting.cache.IListRecipe;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class GeneratedListRecipe<R extends Recipe<?>, E> extends IESerializableRecipe implements IListRecipe
{
	public static Map<Identifier, RecipeListGenerator<?, ?>> LIST_GENERATORS = new HashMap<>();
	public static Supplier<RecipeSerializer<GeneratedListRecipe<?, ?>>> SERIALIZER;

	static
	{
		LIST_GENERATORS.put(rl("mixer_potion_list"), RecipeListGenerator.simple(
				PotionRecipeGenerators::initPotionRecipes, MixerRecipe.SERIALIZER,
				IERecipeTypes.MIXER
		));
		LIST_GENERATORS.put(rl("potion_bottling_list"), RecipeListGenerator.simple(
				PotionRecipeGenerators::getPotionBottlingRecipes, BottlingMachineRecipe.SERIALIZER,
				IERecipeTypes.BOTTLING_MACHINE
		));
		LIST_GENERATORS.put(rl("arc_recycling_list"), RecipeListGenerator.fromSerializer(
				ArcRecyclingCalculator::makeFuture,
				recyclingList -> Objects.requireNonNull(recyclingList.getValue()),
				ArcFurnaceRecipe.SERIALIZER,
				IERecipeTypes.ARC_FURNACE
		));
	}

	@Nullable
	private List<Recipe<?>> cachedRecipes;
	private final RecipeListGenerator<R, E> generator;
	private E earlyResult;
	private final Identifier generatorID;

	public static GeneratedListRecipe<?, ?> from(Identifier id)
	{
		GeneratedListRecipe<?, ?> result = fromInternal(id);
		result.initEarly();
		return result;
	}

	private static GeneratedListRecipe<?, ?> fromInternal(Identifier id)
	{
		RecipeListGenerator<?, ?> gen = LIST_GENERATORS.get(id);
		Preconditions.checkNotNull(gen, id);
		return new GeneratedListRecipe<>(id, gen);
	}

	public static GeneratedListRecipe<?, ?> resolved(Identifier id, List<Recipe<?>> recipes)
	{
		GeneratedListRecipe<?, ?> result = fromInternal(id);
		result.cachedRecipes = recipes;
		return result;
	}

	private GeneratedListRecipe(Identifier id, RecipeListGenerator<R, E> generator)
	{
		super(TagOutput.EMPTY, generator.recipeType);
		this.generator = generator;
		this.generatorID = id;
	}

	private void initEarly()
	{
		this.earlyResult = this.generator.makeEarlyResult().get();
	}

	protected RecipeSerializer<?> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	public ItemStack getResultItem(Provider access)
	{
		return ItemStack.EMPTY;
	}

	public boolean isSpecial()
	{
		return true;
	}

	public List<Recipe<?>> getSubRecipes()
	{
		if(cachedRecipes==null)
			cachedRecipes = List.copyOf(generator.generator().apply(earlyResult));
		return cachedRecipes;
	}

	public Identifier getGeneratorID()
	{
		return generatorID;
	}

	public record RecipeListGenerator<T extends Recipe<?>, EarlyResult>(
			Supplier<EarlyResult> makeEarlyResult,
			Function<EarlyResult, List<? extends T>> generator,
			Identifier serialized,
			IERecipeTypes.TypeWithClass<T> recipeType
	)
	{
		public static <T extends Recipe<?>, ER> RecipeListGenerator<T, ER> fromSerializer(
				Supplier<ER> makeEarlyResult,
				Function<ER, List<? extends T>> generator,
				Holder<? extends RecipeSerializer<?>> serialized,
				IERecipeTypes.TypeWithClass<T> recipeType
		)
		{
			Identifier serializedKey = serialized.unwrapKey().orElseThrow().identifier();
			return new RecipeListGenerator<>(makeEarlyResult, generator, serializedKey, recipeType);
		}

		public static <R extends Recipe<?>>
		RecipeListGenerator<R, ?> simple(
				Supplier<List<? extends R>> generator,
				Holder<? extends RecipeSerializer<?>> serialized,
				IERecipeTypes.TypeWithClass<R> recipeType
		)
		{
			return fromSerializer(() -> Unit.INSTANCE, $ -> generator.get(), serialized, recipeType);
		}
	}
}
