/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.CreativeFluidTankBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class CreativeFluidTankMenu extends IEContainerMenu
{
	public final FluidTank tank;
	@Nullable
	private final CreativeFluidTankBlockEntity tankBE;

	public static CreativeFluidTankMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, CreativeFluidTankBlockEntity be
	)
	{
		return new CreativeFluidTankMenu(
				blockCtx(type, id, be), invPlayer, be.getInventory(), be.getTank(), be
		);
	}

	public static CreativeFluidTankMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new CreativeFluidTankMenu(
				clientCtx(type, id), invPlayer, new ItemStackHandler(CreativeFluidTankBlockEntity.NUM_SLOTS),
				new FluidTank(FluidType.BUCKET_VOLUME), null
		);
	}

	private CreativeFluidTankMenu(
			MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank tank,
			@Nullable CreativeFluidTankBlockEntity tankBE
	)
	{
		super(ctx);
		this.tank = tank;
		this.tankBE = tankBE;

		this.addSlot(new IESlot.NewFluidContainer(inv, CreativeFluidTankBlockEntity.SLOT_INPUT, 74, 35, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, CreativeFluidTankBlockEntity.SLOT_OUTPUT, 101, 35));
		ownSlotCount = CreativeFluidTankBlockEntity.NUM_SLOTS;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));

		addGenericData(GenericContainerData.fluid(tank));
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(tankBE!=null&&nbt.getBooleanOr("clearFluid", false))
			tankBE.clearFluid();
	}
}
