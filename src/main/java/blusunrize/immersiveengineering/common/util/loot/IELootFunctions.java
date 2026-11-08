/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author BluSunrize - 16.08.2018
 */
public class IELootFunctions
{
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final DeferredRegister<MapCodec<? extends LootItemFunction>> FUNCTION_REGISTER = (DeferredRegister)DeferredRegister.create(
			Registries.LOOT_FUNCTION_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<MapCodec<? extends LootItemFunction>> BLUPRINTZ = registerFunction("secret_bluprintz", BluprintzLootFunction.CODEC);
	public static final Holder<MapCodec<? extends LootItemFunction>> REVOLVERPERK = registerFunction("revolverperk", RevolverperkLootFunction.CODEC);
	public static final Holder<MapCodec<? extends LootItemFunction>> WINDMILL = registerFunction("windmill", WindmillLootFunction.CODEC);
	public static final Holder<MapCodec<? extends LootItemFunction>> CONVEYOR_COVER = registerFunction("conveyor_cover", ConveyorCoverLootFunction.CODEC);
	public static final Holder<MapCodec<? extends LootItemFunction>> PROPERTY_COUNT = registerFunction("property_count", PropertyCountLootFunction.CODEC);

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final DeferredRegister<MapCodec<? extends LootPoolEntryContainer>> ENTRY_REGISTER = (DeferredRegister)DeferredRegister.create(
			Registries.LOOT_POOL_ENTRY_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<MapCodec<? extends LootPoolEntryContainer>> DROP_INVENTORY = registerEntry("drop_inv", DropInventoryLootEntry.CODEC);
	public static final Holder<MapCodec<? extends LootPoolEntryContainer>> TILE_DROP = registerEntry("tile_drop", BEDropLootEntry.CODEC);
	public static final Holder<MapCodec<? extends LootPoolEntryContainer>> MULTIBLOCK_DROPS = registerEntry("multiblock", MultiblockDropsLootContainer.CODEC);

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static final DeferredRegister<MapCodec<? extends LootItemCondition>> CONDITION_REGISTER = (DeferredRegister)DeferredRegister.create(
			Registries.LOOT_CONDITION_TYPE, ImmersiveEngineering.MODID
	);
	public static final Holder<MapCodec<? extends LootItemCondition>> BLOCKSTATE = registerCondition("blockstate", LootBlockStateFromLocationPredicate.CODEC);

	public static void init(IEventBus modBus)
	{
		FUNCTION_REGISTER.register(modBus);
		ENTRY_REGISTER.register(modBus);
		CONDITION_REGISTER.register(modBus);
	}

	private static Holder<MapCodec<? extends LootPoolEntryContainer>> registerEntry(
			String id, MapCodec<? extends LootPoolEntryContainer> serializer
	)
	{
		return ENTRY_REGISTER.register(id, () -> serializer);
	}

	private static Holder<MapCodec<? extends LootItemFunction>> registerFunction(
			String id, MapCodec<? extends LootItemFunction> serializer
	)
	{
		return FUNCTION_REGISTER.register(id, () -> serializer);
	}

	private static Holder<MapCodec<? extends LootItemCondition>> registerCondition(
			String id, MapCodec<? extends LootItemCondition> serializer
	)
	{
		return CONDITION_REGISTER.register(id, () -> serializer);
	}
}
