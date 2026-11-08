/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.crafting.PotionHelper;
import blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionBottleType;
import blusunrize.immersiveengineering.common.util.compat.IECompatModules.StandardIECompatModule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class CreateCompatModule extends StandardIECompatModule
{
	public void init()
	{
		Fluid potionFluid = BuiltInRegistries.FLUID.get(Identifier.fromNamespaceAndPath("create", "potion"))
				.map(holder -> holder.value()).orElse(Fluids.EMPTY);
		PotionHelper.CREATE_POTION_BUILDER = (potionHolder, potionBottleType) -> FluidIngredient.of(potionFluid);
	}
}
