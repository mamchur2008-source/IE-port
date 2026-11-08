/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.mojang.serialization.MapCodec;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe<?, ?>>
{
	private static final MapCodec<GeneratedListRecipe<?, ?>> CODEC = Identifier.CODEC
			.fieldOf("generatorID")
			.xmap(GeneratedListRecipe::from, GeneratedListRecipe::getGeneratorID);
	private static final StreamCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, GeneratedListRecipe::getGeneratorID,
			Recipe.STREAM_CODEC.apply(ByteBufCodecs.list()), GeneratedListRecipe::getSubRecipes,
			GeneratedListRecipe::resolved
	);
	public static final DualMapCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> CODECS = new DualMapCodec<>(
			CODEC, STREAM_CODEC
	);

	protected DualMapCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> codecs()
	{
		return CODECS;
	}

	public ItemStack getIcon()
	{
		return new ItemStack(Misc.WIRE_COILS.get(WireType.COPPER));
	}
}
