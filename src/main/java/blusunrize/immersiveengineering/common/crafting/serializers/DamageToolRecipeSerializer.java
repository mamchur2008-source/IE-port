/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DamageToolRecipeSerializer
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, DamageToolRecipe> CODECS = DualCompositeMapCodecs.composite(
			DualCodecs.STRING.fieldOf("group"), DamageToolRecipe::group,
			DualCodecs.ITEM_STACK.fieldOf("result"), r -> r.getResultItem(null),
			DualCodecs.INGREDIENT.fieldOf("tool"), DamageToolRecipe::getTool,
			IEDualCodecs.NONNULL_INGREDIENTS.fieldOf("ingredients"), DamageToolRecipe::getIngredients,
			DamageToolRecipe::new
	);

	public MapCodec<DamageToolRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	public StreamCodec<RegistryFriendlyByteBuf, DamageToolRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}

	public RecipeSerializer<DamageToolRecipe> serializer()
	{
		return new RecipeSerializer<>(codec(), streamCodec());
	}
}
