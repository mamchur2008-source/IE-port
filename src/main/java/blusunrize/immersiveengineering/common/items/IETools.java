/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.world.item.*;

import java.util.function.Supplier;

public class IETools
{
	public static Supplier<ShovelItem> createShovel(ToolMaterial tier)
	{
		return () -> new ShovelItem(tier, 1.5F, -3F, toolProperties());
	}

	public static Supplier<AxeItem> createAxe(ToolMaterial tier)
	{
		return () -> new AxeItem(tier, 6F, -3.1F, toolProperties());
	}

	public static Supplier<Item> createPickaxe(ToolMaterial tier)
	{
		return () -> new Item(toolProperties().pickaxe(tier, 1, -2.8F));
	}

	public static Supplier<Item> createSword(ToolMaterial tier)
	{
		return () -> new Item(toolProperties().sword(tier, 3, -2.4F));
	}

	public static Supplier<HoeItem> createHoe(ToolMaterial tier)
	{
		return () -> new HoeItem(tier, -2F, -1F, toolProperties());
	}

	private static Item.Properties toolProperties()
	{
		return IEItems.defaultProperties().stacksTo(1);
	}
}
