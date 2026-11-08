/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public record SimpleRecipeSerializer<R extends Recipe<?>>(Supplier<R> create)
{
	public MapCodec<R> codec()
	{
		return MapCodec.unit(create);
	}

	public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec()
	{
		return new StreamCodec<>()
		{
			public R decode(RegistryFriendlyByteBuf p_320376_)
			{
				return create.get();
			}

			public void encode(RegistryFriendlyByteBuf p_320158_, R p_320396_)
			{
			}
		};
	}

	public RecipeSerializer<R> serializer()
	{
		return new RecipeSerializer<>(codec(), streamCodec());
	}
}
