/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity.DirectionalFluidOutput;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidPumpBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IBlockBounds, IHasDummyBlocks,
		IConfigurableSides, IScrewdriverInteraction, IFluidPipe, IBlockOverlayText, IPlacementInteraction,
		IInteractionObjectIE<FluidPumpBlockEntity>
{
	public static final int MIN_PUMP_SPEED = 1;
	public static final int MAX_PUMP_SPEED = 4;

	public Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : DirectionUtils.VALUES)
		{
			if(d==Direction.DOWN)
				sideConfig.put(d, IOSideConfig.INPUT);
			else
				sideConfig.put(d, IOSideConfig.NONE);
		}
	}

	private final FluidTank tank = new FluidTank(4*FluidType.BUCKET_VOLUME);
	private final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
	private boolean placeCobble = true;
	private boolean pumpEnabled = true;
	private int pumpSpeed = MIN_PUMP_SPEED;
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, FluidPumpBlockEntity::master, makeEnergyInput(energyStorage)
	);
	public boolean redstoneControlInverted = false;

	private boolean checkingArea = false;
	private Fluid searchFluid = null;
	private final List<BlockPos> openList = new ArrayList<>();
	private final List<BlockPos> closedList = new ArrayList<>();
	private final Set<BlockPos> checked = new HashSet<>();

	public FluidPumpBlockEntity(BlockEntityType<FluidPumpBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	private final Map<Direction, IEBlockCapabilityCache<IFluidHandler>> blockFluidHandlers = IEBlockCapabilityCaches.allNeighbors(
			Capabilities.Fluid.BLOCK, this
	);

	public void tickServer()
	{
		if(isDummy())
			return;
		if(pumpEnabled&&tank.getFluidAmount() > 0)
		{
			int amount = Math.min(tank.getFluidAmount(), getTransferRate(canOutputPressurized(false)));
			int i = amount > 0?outputFluid(Utils.copyFluidStackWithAmount(tank.getFluid(), amount, true), FluidAction.EXECUTE): 0;
			tank.drain(i, FluidAction.EXECUTE);
		}

		int consumption = IEServerConfig.MACHINES.pump_consumption.get();
		if(canRun())
		{
			for(Direction f : Direction.values())
				if(getSideConfig(f)==IOSideConfig.INPUT)
				{
					IFluidHandler input = blockFluidHandlers.get(f).getCapability();
					// attempt to find an adjacent entity fluid handler
					if(input==null)
						input = getEntityFluidHandler(f);
					if(input!=null)
					{
						int drainAmount = getTransferRate(this.canOutputPressurized(false));
						FluidStack drain = input.drain(drainAmount, FluidAction.SIMULATE);
						if(drain.isEmpty())
							continue;
						int out = this.outputFluid(drain, FluidAction.EXECUTE);
						input.drain(out, FluidAction.EXECUTE);
					}
					else
						gatherInfiniteFluidFromWorld(f);
				}
			if(level.getGameTime()%40==(((getBlockPos().getX()^getBlockPos().getZ()))%40+40)%40)
			{
				if(closedList.isEmpty())
					prepareAreaCheck();
				else
				{
					for(int i = 0; i < getSpeedMultiplier()&&!closedList.isEmpty(); i++)
						drainWorldFluidBlock(consumption);
				}
			}
		}

		if(checkingArea)
			checkAreaTick();
	}

	private boolean canRun()
	{
		boolean isPowered = isRSPowered();
		if(!isPowered)
		{
			BlockEntity dummy = level.getBlockEntity(getDummyPos());
			if(dummy instanceof FluidPumpBlockEntity pumpDummy)
				isPowered = pumpDummy.isRSPowered();
			else if(getFacing()!=Direction.UP&&level.getBlockEntity(getBlockPos().above()) instanceof FluidPumpBlockEntity legacyDummy)
				isPowered = legacyDummy.isRSPowered();
		}
		return pumpEnabled&&(isPowered^redstoneControlInverted);
	}

	private void drainWorldFluidBlock(int consumption)
	{
		int target = closedList.size()-1;
		BlockPos pos = closedList.get(target);
		FluidStack fs = Utils.drainFluidBlock(level, pos, FluidAction.SIMULATE);
		if(fs==null)
			closedList.remove(target);
		else if(tank.fill(fs, FluidAction.SIMULATE)==fs.getAmount()
				&&this.energyStorage.extractEnergy(consumption, true) >= consumption)
		{
			this.energyStorage.extractEnergy(consumption, false);
			fs = Utils.drainFluidBlock(level, pos, FluidAction.EXECUTE);
			if(IEServerConfig.MACHINES.pump_placeCobble.get()&&placeCobble)
				level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
			this.tank.fill(fs, FluidAction.EXECUTE);
			closedList.remove(target);
		}
	}

	private Direction getFacing()
	{
		BlockState state = getBlockState();
		if(state.hasProperty(IEProperties.FACING_ALL))
			return state.getValue(IEProperties.FACING_ALL);
		if(state.hasProperty(IEProperties.FACING_HORIZONTAL))
			return state.getValue(IEProperties.FACING_HORIZONTAL);
		return Direction.UP;
	}

	private boolean isPumpAt(BlockPos pos)
	{
		return Utils.isBlockAt(level, pos, MetalDevices.FLUID_PUMP.get());
	}

	private BlockPos getMasterPos()
	{
		if(!isDummy())
			return worldPosition;
		BlockPos vertical = worldPosition.below();
		if(isPumpAt(vertical))
			return vertical;
		Direction facing = getFacing();
		BlockPos configured = worldPosition.relative(facing);
		if(isPumpAt(configured))
			return configured;
		BlockPos previousHorizontal = worldPosition.relative(facing.getOpposite());
		if(isPumpAt(previousHorizontal))
			return previousHorizontal;
		return vertical;
	}

	private BlockPos getDummyPos()
	{
		BlockPos masterPos = getMasterPos();
		BlockPos vertical = masterPos.above();
		if(isPumpAt(vertical))
			return vertical;
		Direction facing = getFacing();
		BlockEntity master = Utils.getExistingTileEntity(level, masterPos);
		if(master instanceof FluidPumpBlockEntity pump)
			facing = pump.getFacing();
		BlockPos configured = masterPos.relative(facing.getOpposite());
		if(isPumpAt(configured))
			return configured;
		BlockPos previousHorizontal = masterPos.relative(facing);
		if(isPumpAt(previousHorizontal))
			return previousHorizontal;
		return vertical;
	}

	private boolean hasLegacyVerticalDummy()
	{
		return isDummy()?isPumpAt(worldPosition.below()): isPumpAt(worldPosition.above());
	}

	private Direction getShapeFacing()
	{
		return hasLegacyVerticalDummy()?Direction.UP: getFacing();
	}

	private boolean removePumpPart(BlockPos pos)
	{
		if(isPumpAt(pos))
		{
			level.removeBlock(pos, false);
			return true;
		}
		return false;
	}

	public boolean setSideConfig(Direction side, IOSideConfig config)
	{
		if(isDummy())
		{
			FluidPumpBlockEntity master = master();
			return master!=null&&master.setSideConfig(side, config);
		}
		if(!isFluidSideConfigurable(side))
			return false;
		if(getSideConfig(side)==config)
			return true;
		sideConfig.put(side, config);
		this.setChanged();
		this.markContainingBlockForUpdate(null);
		getLevelNonnull().blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	private boolean toggleSideConfig(Direction side)
	{
		return setSideConfig(side, IOSideConfig.next(getSideConfig(side)));
	}

	private void notifyPlaceCobble(Player player)
	{
		if(player!=null)
			player.sendOverlayMessage(Component.translatable(Lib.CHAT_INFO+"pump.placeCobble."+placeCobble));
	}

	private void notifyRedstone(Player player)
	{
		if(player!=null)
			player.sendSystemMessage(Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")));
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		if(level==null||level.isClientSide()||isDummy())
			return;
		sideConfig.clear();
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.NONE);
		sideConfig.put(Direction.DOWN, IOSideConfig.INPUT);
		setChanged();
		markContainingBlockForUpdate(null);
	}

	public FluidTank getTank()
	{
		return tank;
	}

	public MutableEnergyStorage getEnergyStorage()
	{
		return energyStorage;
	}

	public boolean isPlaceCobble()
	{
		return placeCobble;
	}

	public boolean isPumpEnabled()
	{
		return pumpEnabled;
	}

	public int getPumpSpeed()
	{
		return getSpeedMultiplier();
	}

	public int getDisplayedFluidRate()
	{
		return getTransferRate(true);
	}

	public int getDisplayedPowerRate()
	{
		return getAccelerationEnergyCost();
	}

	private int getSpeedMultiplier()
	{
		return Mth.clamp(pumpSpeed, MIN_PUMP_SPEED, MAX_PUMP_SPEED);
	}

	private int getTransferRate(boolean pressurized)
	{
		return IFluidPipe.getTransferableAmount(pressurized)*getSpeedMultiplier();
	}

	private int getAccelerationEnergyCost()
	{
		return IEServerConfig.MACHINES.pump_consumption_accelerate.get()*getSpeedMultiplier();
	}

	public void setPumpEnabled(boolean pumpEnabled)
	{
		if(this.pumpEnabled!=pumpEnabled)
		{
			this.pumpEnabled = pumpEnabled;
			setChanged();
			markContainingBlockForUpdate(null);
		}
	}

	public void setPumpSpeed(int speed)
	{
		int clamped = Mth.clamp(speed, MIN_PUMP_SPEED, MAX_PUMP_SPEED);
		if(this.pumpSpeed!=clamped)
		{
			this.pumpSpeed = clamped;
			setChanged();
			markContainingBlockForUpdate(null);
		}
	}

	public void togglePlaceCobble()
	{
		placeCobble = !placeCobble;
		setChanged();
		markContainingBlockForUpdate(null);
	}

	public void toggleRedstoneControl()
	{
		redstoneControlInverted = !redstoneControlInverted;
		setChanged();
		markContainingBlockForUpdate(null);
	}

	@Nonnull
	public Component getDisplayName()
	{
		return Component.translatable("block."+Lib.MODID+".fluid_pump");
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public FluidPumpBlockEntity getGuiMaster()
	{
		FluidPumpBlockEntity master = master();
		return master!=null?master: this;
	}

	public ArgContainer<FluidPumpBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.FLUID_PUMP;
	}

	public void prepareAreaCheck()
	{
		openList.clear();
		closedList.clear();
		checked.clear();
		searchFluid = null;
		for(Direction f : Direction.values())
			if(getSideConfig(f)==IOSideConfig.INPUT)
			{
				openList.add(getBlockPos().relative(f));
				checkingArea = true;
			}
	}

	private void gatherInfiniteFluidFromWorld(Direction gatherFrom)
	{
		if(level.getGameTime()%20!=Mth.positiveModulo(getBlockPos().getX()^getBlockPos().getZ(), 20))
			return;
		int consumption = IEServerConfig.MACHINES.pump_consumption.get();
		final BlockPos neighborPos = getBlockPos().relative(gatherFrom);
		final FluidState neighborFluidState = level.getFluidState(neighborPos);
		if(!neighborFluidState.isSource()||!neighborFluidState.canConvertToSource((net.minecraft.server.level.ServerLevel)getLevelNonnull(), neighborPos))
			return;
		final Fluid fluid = neighborFluidState.getType();
		int buckets = Math.min(getSpeedMultiplier(), (tank.getCapacity()-tank.getFluidAmount())/FluidType.BUCKET_VOLUME);
		if(buckets <= 0)
			return;
		int cost = consumption*buckets;
		if(this.energyStorage.extractEnergy(cost, true) < cost)
			return;
		final FluidStack gatheredFluid = new FluidStack(fluid, FluidType.BUCKET_VOLUME*buckets);
		if(tank.fill(gatheredFluid, FluidAction.SIMULATE)!=gatheredFluid.getAmount())
			return;
		int connectedSources = 0;
		for(Direction sourceNeighbor : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			FluidState neighboringSource = level.getFluidState(neighborPos.relative(sourceNeighbor));
			if(neighboringSource.getType()==fluid&&neighboringSource.isSource())
				connectedSources++;
		}
		if(connectedSources > 1)
		{
			this.energyStorage.extractEnergy(cost, false);
			this.tank.fill(gatheredFluid, FluidAction.EXECUTE);
		}
	}

	public void checkAreaTick()
	{
		final int closedListMax = 2048;
		int timeout = 0;
		while(timeout < 64&&closedList.size() < closedListMax&&!openList.isEmpty())
		{
			timeout++;
			BlockPos next = openList.remove(0);
			if(!checked.add(next))
				continue;
			final FluidState fluidState = getLevelNonnull().getFluidState(next);
			if(fluidState.isEmpty()||fluidState.canConvertToSource((net.minecraft.server.level.ServerLevel)getLevelNonnull(), next))
				continue;
			Fluid fluid = fluidState.getType();
			if(searchFluid!=null&&fluid!=searchFluid)
				continue;
			if(searchFluid==null)
				searchFluid = fluid;

			if(!Utils.drainFluidBlock(level, next, FluidAction.SIMULATE).isEmpty())
				closedList.add(next);
			for(Direction f : Direction.values())
			{
				BlockPos neighborPos = next.relative(f);
				if(checked.contains(neighborPos))
					continue;
				FluidState neighborFluidState = getLevelNonnull().getFluidState(neighborPos);
				if(neighborFluidState.isEmpty())
					continue;
				Fluid neighborFluid = Utils.getRelatedFluid(level, neighborPos);
				if(!neighborFluidState.canConvertToSource((net.minecraft.server.level.ServerLevel)getLevelNonnull(), neighborPos)&&neighborFluid==searchFluid)
					openList.add(neighborPos);
			}
		}
		if(closedList.size() >= closedListMax||openList.isEmpty())
			checkingArea = false;
	}

	public int outputFluid(FluidStack fs, FluidAction action)
	{
		if(fs.isEmpty())
			return 0;

		int canAccept = fs.getAmount();
		if(canAccept <= 0)
			return 0;

		int accelPower = getAccelerationEnergyCost();
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
		for(Direction f : Direction.values())
			if(getSideConfig(f)==IOSideConfig.OUTPUT)
			{
				IFluidHandler handler = blockFluidHandlers.get(f).getCapability();
				if(handler!=null)
				{
					// TODO check if there are bad BE assumptions here
					BlockEntity tile = getLevelNonnull().getBlockEntity(worldPosition.relative(f));
					FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
					if(tile instanceof FluidPipeBlockEntity&&canAccelerateOutput(insertResource, accelPower))
						insertResource.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
					int temp = handler.fill(insertResource, FluidAction.SIMULATE);
					if(temp > 0)
					{
						sorting.put(new DirectionalFluidOutput(handler, f, tile, worldPosition.relative(f)), temp);
						sum += temp;
					}
				}
				else
				{
					handler = getEntityFluidHandler(f);
					if(handler!=null)
					{
						FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
						int temp = handler.fill(insertResource, FluidAction.SIMULATE);
						if(temp > 0)
						{
							sorting.put(new DirectionalFluidOutput(handler, f, null, worldPosition.relative(f)), temp);
							sum += temp;
						}
					}
				}
			}
		if(sum > 0)
		{
			int f = 0;
			int i = 0;
			for(DirectionalFluidOutput output : sorting.keySet())
			{
				float prio = sorting.get(output)/(float)sum;
				int amount = (int)(fluidForSort*prio);
				if(i++==sorting.size()-1)
					amount = canAccept;
				boolean accelerated = canAccelerateOutput(amount, accelPower);
				int outputAmount = accelerated?amount: Math.min(amount, getTransferRate(false));
				FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, outputAmount, true);
				if(output.containingTile() instanceof FluidPipeBlockEntity&&accelerated)
					insertResource.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
				int r = output.output().fill(insertResource, action);
				if(r > 0&&accelerated&&action.execute())
					this.energyStorage.extractEnergy(accelPower, false);
				f += r;
				canAccept -= r;
				if(canAccept <= 0)
					break;
			}
			return f;
		}
		return 0;
	}

	private boolean canAccelerateOutput(FluidStack stack, int accelPower)
	{
		return canAccelerateOutput(stack.getAmount(), accelPower);
	}

	private boolean canAccelerateOutput(int amount, int accelPower)
	{
		return amount > getTransferRate(false)&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower;
	}

	@Nullable
	private IFluidHandler getEntityFluidHandler(Direction direction)
	{
		return null;
	}


	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] sideConfigArray = nbt.getIntArray("sideConfig").orElse(new int[0]);
		sideConfig.clear();
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, readSideConfig(sideConfigArray, d));
		if(nbt.contains("placeCobble"))
			placeCobble = nbt.getBooleanOr("placeCobble", false);
		pumpEnabled = nbt.getBooleanOr("pumpEnabled", true);
		pumpSpeed = Mth.clamp(nbt.getIntOr("pumpSpeed", MIN_PUMP_SPEED), MIN_PUMP_SPEED, MAX_PUMP_SPEED);
		blusunrize.immersiveengineering.common.util.FluidTankCompat.readFromNBT(tank, provider, nbt.getCompoundOrEmpty("tank"));
		EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
		redstoneControlInverted = nbt.getBooleanOr("redstoneInverted", false);
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	private static IOSideConfig readSideConfig(int[] sideConfigArray, Direction side)
	{
		if(side.ordinal() >= sideConfigArray.length)
			return getDefaultSideConfig(side);
		int value = sideConfigArray[side.ordinal()];
		if(value < 0||value >= IOSideConfig.VALUES.length)
			return getDefaultSideConfig(side);
		return IOSideConfig.VALUES[value];
	}

	private static IOSideConfig getDefaultSideConfig(Direction side)
	{
		return side==Direction.DOWN?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] sideConfigArray = new int[6];
		for(Direction d : DirectionUtils.VALUES)
			sideConfigArray[d.ordinal()] = getSideConfig(d).ordinal();
		nbt.putIntArray("sideConfig", sideConfigArray);
		nbt.putBoolean("placeCobble", placeCobble);
		nbt.putBoolean("pumpEnabled", pumpEnabled);
		nbt.putInt("pumpSpeed", getSpeedMultiplier());
		nbt.put("tank", blusunrize.immersiveengineering.common.util.FluidTankCompat.writeToNBT(tank, provider));
		EnergyHelper.serializeTo(energyStorage, nbt, provider);
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
	}

	public IOSideConfig getSideConfig(Direction side)
	{
		if(side==Direction.DOWN)
			return IOSideConfig.INPUT;
		if(side==Direction.UP)
			return IOSideConfig.NONE;
		return sideConfig.getOrDefault(side, getDefaultSideConfig(side));
	}

	public boolean isFluidSideConfigurable(Direction side)
	{
		return side.getAxis().isHorizontal();
	}

	public boolean canConnectFluidSide(Direction side)
	{
		return side==Direction.DOWN||isFluidSideConfigurable(side);
	}

	public boolean toggleSide(Direction side, Player p)
	{
		if(isDummy())
		{
			FluidPumpBlockEntity master = master();
			return master!=null&&master.toggleSide(side, p);
		}
		if(p!=null&&p.isShiftKeyDown())
		{
			togglePlaceCobble();
			notifyPlaceCobble(p);
			return true;
		}
		if(!isFluidSideConfigurable(side))
			return false;
		return toggleSideConfig(side);
	}

	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(isDummy())
		{
			FluidPumpBlockEntity master = master();
			if(master!=null)
				return master.screwdriverUseSide(side, player, hand, hitVec);
			return InteractionResult.PASS;
		}
		if(!level.isClientSide())
		{
			toggleRedstoneControl();
			notifyRedstone(player);
		}
		return InteractionResult.SUCCESS;
	}

	private final Map<Direction, IFluidHandler> sidedFluidHandler = new EnumMap<>(Direction.class);

	public static void registerCapabilities(BECapabilityRegistrar<FluidPumpBlockEntity> registrar)
	{
		registrar.register(Capabilities.Fluid.BLOCK, (be, facing) -> {
			if(facing!=null&&!be.isDummy()&&be.canConnectFluidSide(facing))
			{
				if(!be.sidedFluidHandler.containsKey(facing))
					be.sidedFluidHandler.put(facing, new SidedFluidHandler(be, facing));
				return be.sidedFluidHandler.get(facing);
			}
			else
				return null;
		});
		registrar.register(
				Energy.BLOCK,
				(be, facing) -> be.isDummy()&&facing==Direction.UP?be.energyCap.get(): null
		);
	}

	private Direction getEnergyConnectorSide()
	{
		if(isDummy())
		{
			if(isPumpAt(worldPosition.below()))
				return Direction.UP;
			Direction facing = getFacing();
			if(isPumpAt(worldPosition.relative(facing)))
				return facing.getOpposite();
			if(isPumpAt(worldPosition.relative(facing.getOpposite())))
				return facing;
		}
		return Direction.UP;
	}

	public Direction getEnergyInputSide()
	{
		return Direction.UP;
	}

	public Direction getJoinedSide()
	{
		return Direction.UP;
	}

	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult mop, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&!isDummy()&&mop instanceof BlockHitResult)
		{
			BlockHitResult brtr = (BlockHitResult)mop;
			IOSideConfig i = getSideConfig(brtr.getDirection());
			IOSideConfig j = getSideConfig(brtr.getDirection().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectCapabilities.Fluid.", i, j);
		}
		return null;
	}

	public void setDummy(boolean dummy)
	{
		BlockState old = getBlockState();
		BlockState newState = old.setValue(IEProperties.MULTIBLOCKSLAVE, dummy);
		setState(newState);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		FluidPumpBlockEntity pump;
		Direction facing;

		SidedFluidHandler(FluidPumpBlockEntity pump, Direction facing)
		{
			this.pump = pump;
			this.facing = facing;
		}

		public int getTanks()
		{
			return pump.tank.getTanks();
		}

		@Nonnull
		public FluidStack getFluidInTank(int tank)
		{
			return pump.tank.getFluidInTank(tank);
		}

		public int getTankCapacity(int tank)
		{
			return pump.tank.getTankCapacity(tank);
		}

		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			if(pump.getSideConfig(facing)!=IOSideConfig.INPUT)
				return false;
			return pump.tank.isFluidValid(tank, stack);
		}

		public int fill(FluidStack resource, FluidAction action)
		{
			if(resource.isEmpty()||pump.getSideConfig(facing)!=IOSideConfig.INPUT)
				return 0;
			return pump.tank.fill(resource, action);
		}

		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			return this.drain(resource.getAmount(), action);
		}

		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(!pump.pumpEnabled||pump.getSideConfig(facing)!=IOSideConfig.OUTPUT)
				return FluidStack.EMPTY;
			return pump.tank.drain(maxDrain, action);
		}
	}

	public boolean isDummy()
	{
		return getBlockState().getValue(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	public FluidPumpBlockEntity master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getMasterPos();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof FluidPumpBlockEntity pump?pump: null;
	}

	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		BlockPos dummyPos = worldPosition.above();
		getLevelNonnull().setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state.setValue(IEProperties.MULTIBLOCKSLAVE, true), getLevelNonnull(), dummyPos
		));
	}

	public void breakDummies(BlockPos pos, BlockState state)
	{
		BlockPos masterPos = getMasterPos();
		BlockPos dummyPos = getDummyPos();
		removePumpPart(dummyPos);
		removePumpPart(masterPos);
	}

	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(!isDummy())
			return Shapes.block();
		return switch(getShapeFacing().getAxis())
		{
			case X -> Shapes.box(0, .1875f, .1875f, 1, .8125f, .8125f);
			case Y -> Shapes.box(.1875f, 0, .1875f, .8125f, 1, .8125f);
			case Z -> Shapes.box(.1875f, .1875f, 0, .8125f, .8125f, 1);
		};
	}

	public boolean canOutputPressurized(boolean consumePower)
	{
		int accelPower = getAccelerationEnergyCost();
		if(energyStorage.extractEnergy(accelPower, true) >= accelPower)
		{
			if(consumePower)
				energyStorage.extractEnergy(accelPower, false);
			return true;
		}
		return false;
	}
}
