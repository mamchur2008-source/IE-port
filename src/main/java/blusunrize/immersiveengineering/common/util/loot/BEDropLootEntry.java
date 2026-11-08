/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class BEDropLootEntry extends LootPoolSingletonContainer
{
	public static final MapCodec<BEDropLootEntry> CODEC = RecordCodecBuilder.mapCodec(
			inst -> singletonFields(inst).apply(inst, BEDropLootEntry::new)
	);

	protected BEDropLootEntry(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.hasParameter(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
			if(te instanceof IBlockEntityDrop dropBE)
				dropBE.getBlockEntityDrop(context, output);
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(BEDropLootEntry::new);
	}

	public MapCodec<? extends LootPoolSingletonContainer> codec()
	{
		return CODEC;
	}
}
