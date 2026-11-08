/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;

public interface IECraftingRecipe extends CraftingRecipe
{
	default boolean isSpecial()
	{
		return true;
	}

	default boolean showNotification()
	{
		return false;
	}

	default String group()
	{
		return "";
	}

	default PlacementInfo placementInfo()
	{
		return PlacementInfo.NOT_PLACEABLE;
	}

	default CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}
