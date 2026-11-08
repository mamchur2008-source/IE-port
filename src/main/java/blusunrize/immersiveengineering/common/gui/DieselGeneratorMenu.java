/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic.State;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

public class DieselGeneratorMenu extends IEContainerMenu
{
	public final FluidTank tank;
	public boolean active;
	@Nullable
	private final State state;

	public static DieselGeneratorMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, MultiblockMenuContext<State> ctx
	)
	{
		State state = ctx.mbContext().getState();
		return new DieselGeneratorMenu(
				multiblockCtx(type, id, ctx), invPlayer, state.tank, state, state.isActive()
		);
	}

	public static DieselGeneratorMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new DieselGeneratorMenu(
				clientCtx(type, id), invPlayer, new FluidTank(24*FluidType.BUCKET_VOLUME), null, false
		);
	}

	private DieselGeneratorMenu(
			MenuContext ctx, Inventory inventoryPlayer, FluidTank tank, @Nullable State state, boolean active
	)
	{
		super(ctx);
		this.tank = tank;
		this.state = state;
		this.active = active;
		ownSlotCount = 0;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));

		addGenericData(GenericContainerData.fluid(tank));
		addGenericData(GenericContainerData.bool(
				() -> this.state!=null?this.state.isActive(): this.active,
				value -> this.active = value
		));
	}
}
