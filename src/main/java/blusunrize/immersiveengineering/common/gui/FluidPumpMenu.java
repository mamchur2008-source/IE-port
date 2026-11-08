/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.energy.IMutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;

public class FluidPumpMenu extends IEContainerMenu
{
	public final IEnergyStorage energy;
	public final FluidTank tank;
	public boolean placeCobble;
	public boolean redstoneInverted;
	public boolean pumpEnabled;
	public int pumpSpeed;
	public int fluidRate;
	public int powerRate;
	public int energyInputSide = Direction.UP.ordinal();
	public int joinedSide = Direction.UP.ordinal();
	private final int[] sideConfig = new int[DirectionUtils.VALUES.length];
	@Nullable
	private final FluidPumpBlockEntity pump;

	public static FluidPumpMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, FluidPumpBlockEntity be
	)
	{
		FluidPumpBlockEntity master = be.master();
		if(master==null)
			master = be;
		return new FluidPumpMenu(
				blockCtx(type, id, master), invPlayer, master.getEnergyStorage(), master.getTank(),
				master, master.isPlaceCobble(), master.redstoneControlInverted, master.isPumpEnabled(),
				master.getPumpSpeed(), master.getDisplayedFluidRate(), master.getDisplayedPowerRate()
		);
	}

	public static FluidPumpMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new FluidPumpMenu(
				clientCtx(type, id), invPlayer, new MutableEnergyStorage(8000),
				new FluidTank(4*FluidType.BUCKET_VOLUME), null, true, false, true,
				FluidPumpBlockEntity.MIN_PUMP_SPEED,
				FluidPumpBlockEntity.MIN_PUMP_SPEED*FluidType.BUCKET_VOLUME,
				0
		);
	}

	private FluidPumpMenu(
			MenuContext ctx, Inventory inventoryPlayer, IMutableEnergyStorage energy, FluidTank tank,
			@Nullable FluidPumpBlockEntity pump, boolean placeCobble, boolean redstoneInverted,
			boolean pumpEnabled, int pumpSpeed, int fluidRate, int powerRate
	)
	{
		super(ctx);
		this.energy = energy;
		this.tank = tank;
		this.pump = pump;
		this.placeCobble = placeCobble;
		this.redstoneInverted = redstoneInverted;
		this.pumpEnabled = pumpEnabled;
		this.pumpSpeed = pumpSpeed;
		this.fluidRate = fluidRate;
		this.powerRate = powerRate;
		ownSlotCount = 0;

		for(Direction side : DirectionUtils.VALUES)
			sideConfig[side.ordinal()] = (side==Direction.DOWN?IOSideConfig.INPUT: IOSideConfig.NONE).ordinal();

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 134+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 192));

		addGenericData(GenericContainerData.energy(energy));
		addGenericData(GenericContainerData.fluid(tank));
		addGenericData(GenericContainerData.bool(
				() -> this.pump!=null?this.pump.isPlaceCobble(): this.placeCobble,
				value -> this.placeCobble = value
		));
		addGenericData(GenericContainerData.bool(
				() -> this.pump!=null?this.pump.redstoneControlInverted: this.redstoneInverted,
				value -> this.redstoneInverted = value
		));
		addGenericData(GenericContainerData.bool(
				() -> this.pump!=null?this.pump.isPumpEnabled(): this.pumpEnabled,
				value -> this.pumpEnabled = value
		));
		addGenericData(GenericContainerData.int32(
				() -> this.pump!=null?this.pump.getPumpSpeed(): this.pumpSpeed,
				value -> this.pumpSpeed = value
		));
		addGenericData(GenericContainerData.int32(
				() -> this.pump!=null?this.pump.getDisplayedFluidRate(): this.fluidRate,
				value -> this.fluidRate = value
		));
		addGenericData(GenericContainerData.int32(
				() -> this.pump!=null?this.pump.getDisplayedPowerRate(): this.powerRate,
				value -> this.powerRate = value
		));
		addGenericData(GenericContainerData.int32(
				() -> this.pump!=null?this.pump.getEnergyInputSide().ordinal(): this.energyInputSide,
				value -> this.energyInputSide = value
		));
		addGenericData(GenericContainerData.int32(
				() -> this.pump!=null?this.pump.getJoinedSide().ordinal(): this.joinedSide,
				value -> this.joinedSide = value
		));
		for(Direction side : DirectionUtils.VALUES)
			addGenericData(GenericContainerData.int32(
					() -> this.pump!=null?this.pump.getSideConfig(side).ordinal(): this.sideConfig[side.ordinal()],
					value -> this.sideConfig[side.ordinal()] = value
			));
	}

	public IOSideConfig getSideConfig(Direction side)
	{
		int ordinal = sideConfig[side.ordinal()];
		if(ordinal < 0||ordinal >= IOSideConfig.VALUES.length)
			return IOSideConfig.NONE;
		return IOSideConfig.VALUES[ordinal];
	}

	public boolean isJoinedSide(Direction side)
	{
		return joinedSide==side.ordinal();
	}

	public Direction getEnergyInputSide()
	{
		if(energyInputSide < 0||energyInputSide >= DirectionUtils.VALUES.length)
			return Direction.UP;
		return DirectionUtils.VALUES[energyInputSide];
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(pump==null)
			return;
		if(nbt.getBooleanOr("togglePlaceCobble", false))
		{
			pump.togglePlaceCobble();
			placeCobble = pump.isPlaceCobble();
		}
		if(nbt.getBooleanOr("toggleRedstone", false))
		{
			pump.toggleRedstoneControl();
			redstoneInverted = pump.redstoneControlInverted;
		}
		if(nbt.contains("pumpEnabled"))
		{
			pump.setPumpEnabled(nbt.getBooleanOr("pumpEnabled", true));
			pumpEnabled = pump.isPumpEnabled();
		}
		if(nbt.contains("pumpSpeed"))
		{
			pump.setPumpSpeed(nbt.getIntOr("pumpSpeed", FluidPumpBlockEntity.MIN_PUMP_SPEED));
			pumpSpeed = pump.getPumpSpeed();
			fluidRate = pump.getDisplayedFluidRate();
			powerRate = pump.getDisplayedPowerRate();
		}
		if(nbt.contains("side")&&nbt.contains("sideConfig"))
		{
			int sideId = nbt.getIntOr("side", 0);
			int configId = nbt.getIntOr("sideConfig", IOSideConfig.NONE.ordinal());
			if(sideId >= 0&&sideId < DirectionUtils.VALUES.length&&configId >= 0&&configId < IOSideConfig.VALUES.length)
			{
				Direction side = DirectionUtils.VALUES[sideId];
				IOSideConfig config = IOSideConfig.VALUES[configId];
				if(pump.setSideConfig(side, config))
					sideConfig[side.ordinal()] = config.ordinal();
			}
		}
	}
}
