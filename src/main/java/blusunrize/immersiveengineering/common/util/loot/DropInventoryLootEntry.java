/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.util.inventory.IDropInventory;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class DropInventoryLootEntry extends LootPoolSingletonContainer
{
	public static final MapCodec<DropInventoryLootEntry> CODEC = RecordCodecBuilder.mapCodec(
			inst -> singletonFields(inst).apply(inst, DropInventoryLootEntry::new)
	);

	protected DropInventoryLootEntry(int weightIn, int qualityIn, List<LootItemCondition> conditionsIn, List<LootItemFunction> functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.hasParameter(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
			if(te instanceof IGeneralMultiblock dummyBE)
				te = (BlockEntity)dummyBE.master();
			if(te instanceof IDropInventory ieInvBE&&ieInvBE.getDroppedItems()!=null)
				ieInvBE.getDroppedItems().forEach(output);
			else if(te!=null&&te.getLevel()!=null)
			{
				IItemHandler itemHandler = null;
				if(itemHandler instanceof IEInventoryHandler ieHandler)
				{
					for(int i = 0; i < ieHandler.getSlots(); i++)
						if(!ieHandler.getStackInSlot(i).isEmpty())
						{
							output.accept(ieHandler.getStackInSlot(i));
							ieHandler.setStackInSlot(i, ItemStack.EMPTY);
						}
				}
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(DropInventoryLootEntry::new);
	}

	public MapCodec<? extends LootPoolSingletonContainer> codec()
	{
		return CODEC;
	}
}
