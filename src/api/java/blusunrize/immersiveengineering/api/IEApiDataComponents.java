/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import blusunrize.immersiveengineering.api.wires.utils.WireLink;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class IEApiDataComponents
{
	public static Supplier<DataComponentType<WireLink>> WIRE_LINK;
	public static Supplier<DataComponentType<String>> BLUEPRINT_TYPE;
	public static Supplier<DataComponentType<Identifier>> ATTACHED_SHADER;
	public static Supplier<DataComponentType<Unit>> FLUID_PRESSURIZED;

	public static final String INVALID_BLUEPRINT = "invalid_blueprint";

	public static String getBlueprintType(ItemStack stack)
	{
		return stack.getOrDefault(BLUEPRINT_TYPE, INVALID_BLUEPRINT);
	}
}
