/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.mixin.accessors.client.ParticleManagerAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.config.IEServerConfig.getOrDefault;

public class ClocheBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IEClientTickableBE,
		IStateBasedDirectional, IHasDummyBlocks, IIEInventory,
		IInteractionObjectIE<ClocheBlockEntity>, IModelOffsetProvider
{
	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;
	public static final int NUM_SLOTS = 7;
	public static final int TANK_CAPACITY = 4*FluidType.BUCKET_VOLUME;
	public static final int ENERGY_CAPACITY = 16000;

	public int dummy = 0;
	private final NonNullList<ItemStack> inventory = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
	public final FluidTank tank = new FluidTank(TANK_CAPACITY)
	{
		public boolean isFluidValid(FluidStack fluid)
		{
			return ClocheRecipe.getValidFluids(getLevel()).anyMatch(req -> req.test(fluid));
		}
	};
	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(
			ENERGY_CAPACITY, Math.max(256, getOrDefault(IEServerConfig.MACHINES.cloche_consumption))
	);
	public final Supplier<ClocheRecipe> cachedRecipe = CachedRecipe.cached(
			ClocheRecipe::findRecipe, () -> level, () -> inventory.get(SLOT_SEED), () -> inventory.get(SLOT_SOIL), tank::getFluid
	);

	public int fertilizerAmount = 0;
	public float fertilizerMod = 1;
	private float growth = 0;
	public float renderGrowth = 0;
	public boolean renderActive = false;
	private final IEBlockCapabilityCache<IItemHandler> output = IEBlockCapabilityCaches.create(
			Capabilities.Item.BLOCK,
			() -> worldPosition.above().relative(getFacing().getOpposite()),
			this::getFacing,
			this::getLevel
	);


	public ClocheBlockEntity(BlockEntityType<ClocheBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public boolean canTickAny()
	{
		return !isRSPowered();
	}

	public void tickClient()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		if(renderActive)
		{
			ClocheRecipe recipe = cachedRecipe.get();
			if(recipe!=null&&fertilizerAmount > 0)
			{
				double addGrow = IEServerConfig.MACHINES.cloche_growth_mod.get()*fertilizerMod;
				if(renderGrowth < recipe.getTime(seed, soil)+addGrow)
					renderGrowth += addGrow;
				else
					renderGrowth = 0;
				if(ApiUtils.getRandom().nextInt(8)==0)
				{
				}
			}
		}
	}

	public void tickServer()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		if(!seed.isEmpty())
		{
			ClocheRecipe recipe = cachedRecipe.get();
			int consumption = IEServerConfig.MACHINES.cloche_consumption.get();
			//Consume needs to be outside of seed check in case power runs down while there is still a seed, to properly propagate updates to the client
			boolean consume = false;
			if(recipe!=null&&fertilizerAmount > 0&&energyStorage.extractEnergy(consumption, true)==consumption)
			{
				if(growth >= recipe.getTime(seed, soil))
				{
					NonNullList<ItemStack> outputs = recipe.getOutputs(seed, soil);
					int canFit = 0;
					boolean[] emptySlotsUsed = new boolean[4];
					for(ItemStack output : outputs)
						if(!output.isEmpty())
							for(int j = 3; j < 7; j++)
							{
								ItemStack existing = inventory.get(j);
								if((existing.isEmpty()&&!emptySlotsUsed[j-3])||(ItemStack.isSameItemSameComponents(existing, output)&&existing.getCount()+output.getCount() <= existing.getMaxStackSize()))
								{
									canFit++;
									if(existing.isEmpty())
										emptySlotsUsed[j-3] = true;
									break;
								}
							}
					if(canFit >= outputs.size())
					{
						for(ItemStack output : outputs)
							for(int j = 3; j < 7; j++)
							{
								ItemStack existing = inventory.get(j);
								if(existing.isEmpty())
								{
									inventory.set(j, output.copy());
									break;
								}
								else if(ItemStack.isSameItemSameComponents(existing, output)&&existing.getCount()+output.getCount() <= existing.getMaxStackSize())
								{
									existing.grow(output.getCount());
									break;
								}
							}
						growth = 0;
						consume = true;
					}
				}
				else
				{
					growth += IEServerConfig.MACHINES.cloche_growth_mod.get()*fertilizerMod;
					consume = true;
					if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
						sendSyncPacket();
				}
				if(consume)
				{
					energyStorage.extractEnergy(consumption, false);
					fertilizerAmount--;
					if(!renderActive)
					{
						renderActive = true;
						sendSyncPacket();
					}
				}
			}
			else
			{
				growth = 0;
				if(renderActive)
				{
					renderActive = false;
					sendSyncPacket();
				}
			}

			int fluidConsumption = IEServerConfig.MACHINES.cloche_fluid.get();
			if(fertilizerAmount <= 0&&tank.getFluidAmount() >= fluidConsumption)
			{
				fertilizerMod = 1;
				tank.drain(fluidConsumption, FluidAction.EXECUTE);
				ItemStack fertilizer = inventory.get(SLOT_FERTILIZER);
				if(!fertilizer.isEmpty())
				{
					float itemMod = ClocheFertilizer.getFertilizerGrowthModifier(level, fertilizer);
					if(itemMod > 0)
					{
						fertilizerMod *= itemMod;
						fertilizer.shrink(1);
						if(fertilizer.getCount() <= 0)
							inventory.set(2, ItemStack.EMPTY);
					}
				}
				fertilizerAmount = IEServerConfig.MACHINES.cloche_fertilizer.get();
			}
		}
		else
			growth = 0;

		if(level.getGameTime()%8==0)
		{
			IItemHandler outputHandler = output.getCapability();
			if(outputHandler!=null)
				for(int j = 3; j < 7; j++)
				{
					ItemStack outStack = inventory.get(j);
					if(!outStack.isEmpty())
					{
						int outCount = Math.min(outStack.getCount(), 16);
						ItemStack stack = outStack.copyWithCount(outCount);
						stack = ItemHandlerHelper.insertItem(outputHandler, stack, false);
						if(!stack.isEmpty())
							outCount -= stack.getCount();
						outStack.shrink(outCount);
						if(outStack.getCount() <= 0)
							this.inventory.set(j, ItemStack.EMPTY);
					}
				}
		}
	}

	public float getGuiProgress()
	{
		ItemStack seed = inventory.get(SLOT_SEED);
		ItemStack soil = inventory.get(SLOT_SOIL);
		ClocheRecipe recipe = cachedRecipe.get();
		if(recipe==null)
			return 0;
		return Mth.clamp(growth/recipe.getTime(seed, soil), 0, 1);
	}

	protected void sendSyncPacket()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("growth", growth);
		nbt.putBoolean("renderActive", renderActive);
		PacketDistributor.sendToPlayersTrackingChunk(
				(ServerLevel)level, new ChunkPos(worldPosition.getX() >> 4, worldPosition.getZ() >> 4), new MessageBlockEntitySync(getBlockPos(), nbt)
		);
	}

	public void receiveMessageFromServer(CompoundTag message)
	{
		renderGrowth = message.getFloatOr("growth", 0);
		renderActive = message.getBooleanOr("renderActive", false);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		final ItemStack oldSoil = inventory.get(SLOT_SOIL);
		dummy = nbt.getIntOr("dummy", 0);
		// loadAllItems skips empty items, so if a slot was emptied it won't be properly synced without the fill call
		Collections.fill(inventory, ItemStack.EMPTY);
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(nbt, inventory, provider);
		fertilizerAmount = nbt.getIntOr("fertilizerAmount", 0);
		fertilizerMod = nbt.getFloatOr("fertilizerMod", 0);
		blusunrize.immersiveengineering.common.util.FluidTankCompat.readFromNBT(tank, provider, nbt.getCompoundOrEmpty("tank"));
		if(!descPacket)
		{
			EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
			growth = nbt.getFloatOr("growth", 0);
		}
		renderBB = null;
		if(descPacket&&level!=null&&!ItemStack.matches(oldSoil, inventory.get(SLOT_SOIL)))
			markContainingBlockForUpdate(null);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.putInt("dummy", dummy);
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, inventory, provider);
		nbt.putInt("fertilizerAmount", fertilizerAmount);
		nbt.putFloat("fertilizerMod", fertilizerMod);
		CompoundTag tankTag = blusunrize.immersiveengineering.common.util.FluidTankCompat.writeToNBT(tank, provider);
		nbt.put("tank", tankTag);
		if(!descPacket)
		{
			EnergyHelper.serializeTo(energyStorage, nbt, provider);
			nbt.putFloat("growth", growth);
		}
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_HORIZONTAL;
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.HORIZONTAL;
	}

	public void setFacing(Direction facing)
	{
		BlockPos lowest = worldPosition.below(dummy);
		for(int i = 0; i < 3; ++i)
		{
			BlockPos pos = lowest.above(i);
			BlockState state = getLevelNonnull().getBlockState(pos);
			if(state.getBlock()==MetalDevices.CLOCHE.get())
				getLevelNonnull().setBlockAndUpdate(pos, state.setValue(getFacingProperty(), facing));
		}
	}

	public boolean isDummy()
	{
		return dummy!=0;
	}

	@Nullable
	public ClocheBlockEntity master()
	{
		if(!isDummy())
			return this;
		// Used to provide tile-dependant drops after breaking
		if(tempMasterBE instanceof ClocheBlockEntity)
			return (ClocheBlockEntity)tempMasterBE;
		BlockPos masterPos = getBlockPos().below(dummy);
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof ClocheBlockEntity?(ClocheBlockEntity)te: null;
	}

	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			level.setBlockAndUpdate(worldPosition.above(i), state);
			((ClocheBlockEntity)level.getBlockEntity(worldPosition.above(i))).dummy = i;
			((ClocheBlockEntity)level.getBlockEntity(worldPosition.above(i))).setFacing(getFacing());
		}
	}

	public void breakDummies(BlockPos pos, BlockState state)
	{
		tempMasterBE = master();
		for(int i = 0; i <= 2; i++)
		{
			BlockPos p = getBlockPos().below(dummy).above(i);
			if(level.getBlockEntity(p) instanceof ClocheBlockEntity)
				level.removeBlock(p, false);
		}
	}

	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	public boolean isStackValid(int slot, ItemStack stack)
	{
		if(slot==SLOT_FERTILIZER)
			return ClocheFertilizer.isValidFertilizer(level, stack);
		else
			return true;
	}

	public int getSlotLimit(int slot)
	{
		return slot < 2?1: 64;
	}

	public void doGraphicalUpdates()
	{
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}


	private final MultiblockCapability<IItemHandler> inputHandler = MultiblockCapability.make(
			this, be -> be.inputHandler, ClocheBlockEntity::master,
			new IEInventoryHandler(1, this, 2, true, false)
	);

	private final MultiblockCapability<IItemHandler> outputHandler = MultiblockCapability.make(
			this, be -> be.outputHandler, ClocheBlockEntity::master,
			new IEInventoryHandler(4, this, 3, false, true)
	);
	private final MultiblockCapability<IFluidHandler> tankCap = MultiblockCapability.make(
			this, be -> be.tankCap, ClocheBlockEntity::master, tank
	);
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, ClocheBlockEntity::master, energyStorage
	);


	public static void registerCapabilities(BECapabilityRegistrar<ClocheBlockEntity> registrar)
	{
		registrar.register(Energy.BLOCK, (be, facing) -> {
			if(facing==null||
					(be.dummy==0&&facing.getAxis()==be.getFacing().getClockWise().getAxis())||
					(be.dummy==2&&facing==Direction.UP))
				return be.energyCap.get();
			else
				return null;
		});
		registrar.register(Capabilities.Item.BLOCK, (be, facing) -> {
			final var clocheFacing = be.getFacing();
			if(facing==null||(be.dummy==0&&facing.getAxis()!=clocheFacing.getClockWise().getAxis()))
				return be.inputHandler.get();
			else if(be.dummy==1&&facing==clocheFacing.getOpposite())
				return be.outputHandler.get();
			else
				return null;
		});
		registrar.register(Capabilities.Fluid.BLOCK, (be, facing) -> {
			if(facing==null||(be.dummy==0&&facing.getAxis()!=be.getFacing().getClockWise().getAxis()))
				return be.tankCap.get();
			else
				return null;
		});
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public ClocheBlockEntity getGuiMaster()
	{
		if(dummy==0)
			return this;
		BlockEntity te = level.getBlockEntity(getBlockPos().below(dummy));
		if(te instanceof ClocheBlockEntity)
			return (ClocheBlockEntity)te;
		return null;
	}

	public ArgContainer<ClocheBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.CLOCHE;
	}

	public AABB renderBB;

	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		return new BlockPos(0, dummy, 0);
	}
}
