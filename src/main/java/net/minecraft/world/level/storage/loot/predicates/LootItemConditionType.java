package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;

public record LootItemConditionType(MapCodec<?> codec)
{
}
