/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.RevolverAssemblyRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

public class RevolverAssemblyRecipeSerializer
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> CODECS = DualCompositeMapCodecs.composite(
			new DualMapCodec<>(
					ShapedRecipe.SERIALIZER.codec(), ShapedRecipe.SERIALIZER.streamCodec()
			), RevolverAssemblyRecipe::toVanilla,
			DualCodecs.INT.listOf().optionalFieldOf("copyNBT", List.of()), RevolverAssemblyRecipe::getCopyTargets,
			RevolverAssemblyRecipe::new
	);

	public MapCodec<RevolverAssemblyRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	public StreamCodec<RegistryFriendlyByteBuf, RevolverAssemblyRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}

	public RecipeSerializer<RevolverAssemblyRecipe> serializer()
	{
		return new RecipeSerializer<>(codec(), streamCodec());
	}
}
