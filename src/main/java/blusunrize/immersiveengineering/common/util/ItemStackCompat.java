package blusunrize.immersiveengineering.common.util;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ItemStackCompat
{
	public static CompoundTag saveOptional(ItemStack stack, Provider provider)
	{
		return ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.flatMap(Tag::asCompound)
				.orElseGet(CompoundTag::new);
	}

	public static CompoundTag save(ItemStack stack, Provider provider)
	{
		return ItemStack.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack)
				.result()
				.flatMap(Tag::asCompound)
				.orElseGet(CompoundTag::new);
	}

	public static ItemStack parseOptional(Provider provider, CompoundTag tag)
	{
		return ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
				.result()
				.orElse(ItemStack.EMPTY);
	}
}
