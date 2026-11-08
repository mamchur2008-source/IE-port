/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import blusunrize.immersiveengineering.common.gui.IESlot.NewFluidContainer.Filter;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class SheetmetalTankMenu extends IEContainerMenu
{
	public final FluidTank tank;

	public static SheetmetalTankMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		return new SheetmetalTankMenu(
				multiblockCtx(type, id, ctx), invPlayer,
				ctx.mbContext().getState().inventory, ctx.mbContext().getState().tank
		);
	}

	public static SheetmetalTankMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new SheetmetalTankMenu(
				clientCtx(type, id), invPlayer, new ItemStackHandler(State.NUM_SLOTS),
				new FluidTank(512*FluidType.BUCKET_VOLUME)
		);
	}

	private SheetmetalTankMenu(MenuContext ctx, Inventory inventoryPlayer, IItemHandler inv, FluidTank tank)
	{
		super(ctx);
		this.tank = tank;

		this.addSlot(new IESlot.NewFluidContainer(inv, State.SLOT_INPUT, 52, 35, Filter.ANY));
		this.addSlot(new IESlot.NewOutput(inv, State.SLOT_OUTPUT, 108, 35));
		ownSlotCount = State.NUM_SLOTS;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));

		addGenericData(GenericContainerData.fluid(tank));
	}
}
