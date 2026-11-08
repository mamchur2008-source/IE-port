package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;

public interface ItemSubPredicate
{
	record Type<T>(Codec<T> codec)
	{
	}
}
