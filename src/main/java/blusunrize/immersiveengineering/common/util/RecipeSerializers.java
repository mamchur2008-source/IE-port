/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.crafting.*;
import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.ShapelessFluidAwareRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.crafting.serializers.*;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;

import java.util.function.Function;
import java.util.function.Supplier;

public class RecipeSerializers
{
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
			BuiltInRegistries.RECIPE_SERIALIZER, ImmersiveEngineering.MODID
	);

	public static final Supplier<RecipeSerializer<SpeedloaderLoadRecipe>> SPEEDLOADER_LOAD = RECIPE_SERIALIZERS.register(
			"crafting_special_speedloader_load", special(SpeedloaderLoadRecipe::new)
	);
	public static final Supplier<RecipeSerializer<FlareBulletColorRecipe>> FLARE_BULLET_COLOR = RECIPE_SERIALIZERS.register(
			"crafting_special_flare_bullet_color", special(FlareBulletColorRecipe::new)
	);
	public static final Supplier<RecipeSerializer<PotionBulletFillRecipe>> POTION_BULLET_FILL = RECIPE_SERIALIZERS.register(
			"crafting_special_potion_bullet_fill", special(PotionBulletFillRecipe::new)
	);
	public static final Supplier<RecipeSerializer<JerrycanRefillRecipe>> JERRYCAN_REFILL = RECIPE_SERIALIZERS.register(
			"crafting_special_jerrycan_refill", special(JerrycanRefillRecipe::new)
	);
	public static final Supplier<RecipeSerializer<PowerpackRecipe>> POWERPACK_SERIALIZER = RECIPE_SERIALIZERS.register(
			"powerpack", special(PowerpackRecipe::new)
	);
	public static final Supplier<RecipeSerializer<LazyShapelessRecipe>> HAMMER_CRUSHING_SERIALIZER = RECIPE_SERIALIZERS.register(
			"hammer_crushing", () -> new HammerCrushingRecipeSerializer().serializer()
	);
	public static final Supplier<RecipeSerializer<EarmuffsRecipe>> EARMUFF_SERIALIZER = RECIPE_SERIALIZERS.register(
			"earmuffs", special(EarmuffsRecipe::new)
	);
	public static final Supplier<RecipeSerializer<RGBColourationRecipe>> RGB_SERIALIZER = RECIPE_SERIALIZERS.register(
			"rgb", () -> new RGBRecipeSerializer().serializer()
	);
	public static final Supplier<RecipeSerializer<TurnAndCopyRecipe>> TURN_AND_COPY_SERIALIZER = RECIPE_SERIALIZERS.register(
			"turn_and_copy", () -> new TurnAndCopyRecipeSerializer().serializer()
	);
	public static final Supplier<RecipeSerializer<RevolverAssemblyRecipe>> REVOLVER_ASSEMBLY_SERIALIZER = RECIPE_SERIALIZERS.register(
			"revolver_assembly", () -> new RevolverAssemblyRecipeSerializer().serializer()
	);
	public static final Supplier<RecipeSerializer<RevolverCycleRecipe>> REVOLVER_CYCLE_SERIALIZER = RECIPE_SERIALIZERS.register(
			"revolver_cycle", special(RevolverCycleRecipe::new)
	);
	public static final Supplier<RecipeSerializer<IERepairItemRecipe>> IE_REPAIR_SERIALIZER = RECIPE_SERIALIZERS.register(
			"ie_item_repair", special(IERepairItemRecipe::new)
	);
	public static final Supplier<RecipeSerializer<ShaderBagRecipe>> SHADER_BAG_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shader_bag", special(ShaderBagRecipe::new)
	);
	public static final Supplier<RecipeSerializer<DamageToolRecipe>> DAMAGE_TOOL_SERIALIZER = RECIPE_SERIALIZERS.register(
			"damage_tool", () -> new DamageToolRecipeSerializer().serializer()
	);
	public static final Supplier<RecipeSerializer<BasicShapedRecipe>> IE_SHAPED_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shaped_fluid", () -> new WrappingRecipeSerializer<>(
					ShapedRecipe.SERIALIZER, BasicShapedRecipe::toVanilla, BasicShapedRecipe::new
			).serializer()
	);
	public static final Supplier<RecipeSerializer<ShapelessFluidAwareRecipe>> IE_SHAPELESS_SERIALIZER = RECIPE_SERIALIZERS.register(
			"shapeless_fluid", () -> new WrappingRecipeSerializer<>(
					ShapelessRecipe.SERIALIZER, ShapelessFluidAwareRecipe::toVanilla, ShapelessFluidAwareRecipe::new
			).serializer()
	);
	public static final Supplier<RecipeSerializer<INoContainersRecipe>> NO_CONTAINER_SERIALIZER = RECIPE_SERIALIZERS.register(
			"no_container_item", () -> new NoContainerSerializer().serializer()
	);

	static
	{
		AlloyRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"alloy", () -> new AlloyRecipeSerializer().serializer()
		);
		BlastFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blast_furnace", () -> new BlastFurnaceRecipeSerializer().serializer()
		);
		BlastFurnaceFuel.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blast_furnace_fuel", () -> new BlastFurnaceFuelSerializer().serializer()
		);
		CokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"coke_oven", () -> new CokeOvenRecipeSerializer().serializer()
		);
		ClocheRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"cloche", () -> new ClocheRecipeSerializer().serializer()
		);
		ClocheFertilizer.SERIALIZER = RECIPE_SERIALIZERS.register(
				"fertilizer", () -> new ClocheFertilizerSerializer().serializer()
		);
		BlueprintCraftingRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"blueprint", () -> new BlueprintCraftingRecipeSerializer().serializer()
		);
		MetalPressRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"metal_press", () -> new MetalPressRecipeSerializer().serializer()
		);
		ArcFurnaceRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"arc_furnace", () -> new ArcFurnaceRecipeSerializer().serializer()
		);
		BottlingMachineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"bottling_machine", () -> new BottlingMachineRecipeSerializer().serializer()
		);
		CrusherRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"crusher", () -> new CrusherRecipeSerializer().serializer()
		);
		SawmillRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"sawmill", () -> new SawmillRecipeSerializer().serializer()
		);
		FermenterRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"fermenter", () -> new FermenterRecipeSerializer().serializer()
		);
		SqueezerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"squeezer", () -> new SqueezerRecipeSerializer().serializer()
		);
		RefineryRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"refinery", () -> new RefineryRecipeSerializer().serializer()
		);
		MixerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"mixer", () -> new MixerRecipeSerializer().serializer()
		);
		MineralMix.SERIALIZER = RECIPE_SERIALIZERS.register(
				"mineral_mix", () -> new MineralMixSerializer().serializer()
		);
		GeneratorFuel.SERIALIZER = RECIPE_SERIALIZERS.register(
				"generator_fuel", () -> new GeneratorFuelSerializer().serializer()
		);
		ThermoelectricSource.SERIALIZER = RECIPE_SERIALIZERS.register(
				"thermoelectric_source", () -> new ThermoelectricSourceSerializer().serializer()
		);
		WindmillBiome.SERIALIZER = RECIPE_SERIALIZERS.register(
				"windmill_biome", () -> new WindmillBiomeSerializer().serializer()
		);
		GeneratedListRecipe.SERIALIZER = RECIPE_SERIALIZERS.register(
				"generated_list", () -> new GeneratedListSerializer().serializer()
		);
	}

	private static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> special(Supplier<T> create)
	{
		return () -> new SimpleRecipeSerializer<>(create).serializer();
	}

	private static <T extends Recipe<?>> Supplier<RecipeSerializer<T>> special(Function<CraftingBookCategory, T> create)
	{
		return special(() -> create.apply(CraftingBookCategory.MISC));
	}
}
