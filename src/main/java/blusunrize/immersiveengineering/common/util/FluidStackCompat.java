package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidStackCompat
{
	public static CompoundTag saveOptional(FluidStack stack, Provider provider)
	{
		return FluidStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.flatMap(Tag::asCompound)
				.orElseGet(CompoundTag::new);
	}

	public static CompoundTag save(FluidStack stack, Provider provider)
	{
		return FluidStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.flatMap(Tag::asCompound)
				.orElseGet(CompoundTag::new);
	}

	public static FluidStack parseOptional(Provider provider, CompoundTag tag)
	{
		return FluidStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
				.result()
				.orElse(FluidStack.EMPTY);
	}
}
