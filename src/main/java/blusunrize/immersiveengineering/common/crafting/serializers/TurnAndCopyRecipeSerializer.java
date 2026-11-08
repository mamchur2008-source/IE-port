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
import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

public class TurnAndCopyRecipeSerializer
{
	private record AdditionalData(List<Integer> copySlots, boolean quarter, boolean eights)
	{
		private static final DualMapCodec<ByteBuf, AdditionalData> CODECS = DualCompositeMapCodecs.composite(
				DualCodecs.INT.listOf().optionalFieldOf("copyNBT", List.of()), AdditionalData::copySlots,
				DualCodecs.BOOL.optionalFieldOf("quarter_turn", false), AdditionalData::quarter,
				DualCodecs.BOOL.optionalFieldOf("eight_turn", false), AdditionalData::eights,
				AdditionalData::new
		);

		public AdditionalData(TurnAndCopyRecipe recipe)
		{
			this(recipe.getCopyTargets(), recipe.isQuarterTurn(), recipe.isEightTurn());
		}

		public TurnAndCopyRecipe apply(ShapedRecipe base)
		{
			TurnAndCopyRecipe result = new TurnAndCopyRecipe(base, copySlots());
			if(quarter())
				result.allowQuarterTurn();
			if(eights())
				result.allowEighthTurn();
			return result;
		}
	}

	public static final DualMapCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> CODECS = DualCompositeMapCodecs.composite(
			AdditionalData.CODECS, AdditionalData::new,
			new DualMapCodec<>(ShapedRecipe.SERIALIZER.codec(), ShapedRecipe.SERIALIZER.streamCodec()), AbstractShapedRecipe::toVanilla,
			AdditionalData::apply
	);

	public MapCodec<TurnAndCopyRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	public StreamCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}

	public RecipeSerializer<TurnAndCopyRecipe> serializer()
	{
		return new RecipeSerializer<>(codec(), streamCodec());
	}
}
