/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.common.register.IEIngredients;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public record IngredientFluidStack(SizedFluidIngredient fluidIngredient) implements ICustomIngredient
{
	private static final MapCodec<SizedFluidIngredient> SIMPLE_SIZED_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			FluidIngredient.CODEC.fieldOf("fluid").forGetter(SizedFluidIngredient::ingredient),
			Codec.INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
	).apply(inst, SizedFluidIngredient::new));

	public static final MapCodec<IngredientFluidStack> MAP_CODEC = NeoForgeExtraCodecs.mapWithAlternative(
			SIMPLE_SIZED_CODEC.xmap(
					IngredientFluidStack::new,
					IngredientFluidStack::fluidIngredient
			),
			SizedFluidIngredient.CODEC.optionalFieldOf("ingredient").xmap(
					sizedFluidIngredient -> sizedFluidIngredient.map(IngredientFluidStack::new).orElse(null),
					ingredientFluidStack -> Optional.of(ingredientFluidStack.fluidIngredient)
			)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientFluidStack> STREAM_CODEC = SizedFluidIngredient.STREAM_CODEC.map(
			IngredientFluidStack::new, IngredientFluidStack::fluidIngredient
	);

	public IngredientFluidStack(TagKey<Fluid> tag, int amount)
	{
		this(new SizedFluidIngredient(FluidIngredient.of(Stream.<Fluid>empty()), amount));
	}

	public Stream<Holder<Item>> items()
	{
		return Stream.empty();
	}

	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null||stack.isEmpty())
			return false;
		Optional<FluidStack> fluid = FluidUtils.getFluidContained(stack);
		return fluid.isPresent()&&fluidIngredient.test(fluid.get());
	}

	public boolean isSimple()
	{
		return false;
	}

	public ItemStack getExtractedStack(ItemStack input)
	{
		IFluidHandlerItem handler = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(input.copyWithCount(1), Capabilities.Fluid.ITEM);
		if(handler!=null)
		{
			handler.drain(fluidIngredient.amount(), FluidAction.EXECUTE);
			return handler.getContainer();
		}
		return ItemStack.EMPTY;
	}

	public IngredientType<?> getType()
	{
		return IEIngredients.FLUID_STACK.value();
	}
}
