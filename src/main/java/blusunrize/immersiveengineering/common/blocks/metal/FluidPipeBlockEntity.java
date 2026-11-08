/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.fluid.IPressurizedFluidOutput;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock.IELadderBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.WorldMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@EventBusSubscriber(modid = Lib.MODID)
public class FluidPipeBlockEntity extends IEBaseBlockEntity implements IFluidPipe, IColouredBE, IPlayerInteraction,
		IHammerInteraction, IPlacementInteraction, ISelectionBounds, ICollisionBounds, IAdditionalDrops
{
	static WorldMap<BlockPos, Set<DirectionalFluidOutput>> indirectConnections = new WorldMap<>();
	public static ArrayList<Predicate<Block>> validPipeCovers = new ArrayList<>();
	public static ArrayList<Predicate<Block>> climbablePipeCovers = new ArrayList<>();

	public FluidPipeBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FLUID_PIPE.get(), pos, state);
	}

	public static void initCovers()
	{
		validPipeCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingAlu));
		validPipeCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingSteel));
		validPipeCovers.add(input -> input==WoodenDecoration.TREATED_SCAFFOLDING.get());

		climbablePipeCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingAlu));
		climbablePipeCovers.add(b -> b.defaultBlockState().is(IETags.scaffoldingSteel));
		climbablePipeCovers.add(input -> input==WoodenDecoration.TREATED_SCAFFOLDING.get());
	}

	public Object2BooleanMap<Direction> sideConfig = new Object2BooleanOpenHashMap<>();

	{
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, true);
	}

	public Block cover = Blocks.AIR;
	private byte connections = 0;
	@Nullable
	private DyeColor color = null;

	public static Set<DirectionalFluidOutput> getConnectedFluidHandlers(BlockPos node, Level world)
	{
		if(world.isClientSide())
			return ImmutableSet.of();
		Set<DirectionalFluidOutput> cachedResult = indirectConnections.get(world, node);
		if(cachedResult!=null)
			return cachedResult;

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		Set<DirectionalFluidOutput> fluidHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());
		openList.add(node);
		while(!openList.isEmpty()&&closedList.size() < 1024)
		{
			BlockPos next = openList.get(0);
			BlockEntity pipeTile = SafeChunkUtils.getSafeBE(world, next); 
			if(!closedList.contains(next)&&(pipeTile instanceof FluidPipeBlockEntity pipe))
			{
				closedList.add(next);
				for(Direction fd : DirectionUtils.VALUES)
					if(pipe.hasOutputConnection(fd))
					{
						BlockPos nextPos = next.relative(fd);
						BlockEntity adjacentTile = SafeChunkUtils.getSafeBE(world, nextPos);
						if(adjacentTile instanceof FluidPipeBlockEntity)
							openList.add(nextPos);
						else
						{
							IFluidHandler handler = pipe.neighbors.get(fd).getCapability();
							if(handler!=null&&handler.getTanks() > 0)
								fluidHandlers.add(new DirectionalFluidOutput(handler, fd, adjacentTile, nextPos));
						}
					}
			}
			openList.remove(0);
		}
		indirectConnections.put(world, node, fluidHandlers);
		return fluidHandlers;
	}

	public void onLoad()
	{
		super.onLoad();
		if(level instanceof ServerLevel serverLevel)
		{
			EventHandler.SERVER_TASKS.add(() -> {
				boolean changed = updateAllConnectionBytes();
				if(changed)
				{
					level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
					markContainingBlockForUpdate(null);
				}
			});
		}
	}

	public void setRemovedIE()
	{
		super.setRemovedIE();
		if(level!=null&&!level.isClientSide())
			indirectConnections.clearDimension(level);
	}

	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		if(level!=null&&!level.isClientSide())
			indirectConnections.clearDimension(level);
	}

	public void onEntityCollision(Level world, Entity entity)
	{
		if(entity instanceof LivingEntity&&!((LivingEntity)entity).onClimbable()&&this.cover!=Blocks.AIR)
		{
			boolean climb = false;
			for(Predicate<Block> f : climbablePipeCovers)
				if(f!=null&&f.test(cover))
				{
					climb = true;
					break;
				}
			if(climb)
				IELadderBlock.applyLadderLogic(entity);
		}
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] config = nbt.getIntArray("sideConfig").orElse(new int[0]);
		for(int i = 0; i < 6; ++i)
		{
			Direction curDir = Direction.from3DDataValue(i);
			if(i < config.length)
			{
				boolean connected = config[i]!=0;
				sideConfig.put(curDir, connected);
				if(connected)
					setValidHandler(curDir);
				else
					invalidateHandler(curDir);
			}
			else
			{
				sideConfig.put(curDir, true);
				setValidHandler(curDir);
			}
		}
		final Block oldCover = cover;
		cover = BuiltInRegistries.BLOCK.get(Identifier.parse(nbt.getStringOr("cover", ""))).map(h -> h.value()).orElse(Blocks.AIR);
		DyeColor oldColor = color;
		if(nbt.contains("color"))
			color = DyeColor.byId(nbt.getIntOr("color", 0));
		else
			color = null;
		byte oldConns = connections;
		connections = nbt.getByteOr("connections", (byte)0);
		if(level!=null&&level.isClientSide()&&(connections!=oldConns||color!=oldColor||cover!=oldCover))
		{
			requestModelDataUpdate();
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 3);
		}
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] config = new int[6];
		for(int i = 0; i < 6; ++i)
			if(sideConfig.getOrDefault(Direction.from3DDataValue(i), false))
				config[i] = 1;
		nbt.putIntArray("sideConfig", config);
		if(hasCover())
			nbt.putString("cover", BuiltInRegistries.BLOCK.getKey(cover).toString());
		nbt.putByte("connections", connections);
		if(color!=null)
			nbt.putInt("color", color.getId());
	}

	boolean canOutputPressurized(@Nullable BlockEntity output, boolean consumePower)
	{
		if(output instanceof IFluidPipe)
			return ((IFluidPipe)output).canOutputPressurized(consumePower);
		return false;
	}

	private final Map<Direction, IFluidHandler> sidedHandlers = new EnumMap<>(Direction.class);
	private final Map<Direction, IEBlockCapabilityCache<IFluidHandler>> neighbors = IEBlockCapabilityCaches.allNeighbors(
			Capabilities.Fluid.BLOCK, this
	);

	{
		for(Direction f : DirectionUtils.VALUES)
			sidedHandlers.put(f, new PipeFluidHandler(this, f));
	}

	private void invalidateHandler(Direction side)
	{
		IFluidHandler handler = sidedHandlers.get(side);
		if(handler!=null)
		{
			sidedHandlers.put(side, null);
			invalidateCapabilities();
		}
	}

	private void setValidHandler(Direction side)
	{
		IFluidHandler handler = sidedHandlers.get(side);
		if(handler==null)
		{
			sidedHandlers.put(side, new PipeFluidHandler(this, side));
			invalidateCapabilities();
		}
	}

	public static void registerCapabilities(BECapabilityRegistrar<FluidPipeBlockEntity> registrar)
	{
		registrar.register(Capabilities.Fluid.BLOCK, (be, side) -> {
			if(side!=null&&be.sideConfig.getOrDefault(side, false))
				return be.sidedHandlers.get(side);
			else
				return null;
		});
	}

	protected boolean hasCover()
	{
		return this.cover!=Blocks.AIR;
	}

	public Collection<ItemStack> getExtraDrops(Player player, BlockState state)
	{
		if(hasCover())
			return Lists.newArrayList(new ItemStack(cover));
		return null;
	}

	public void onNeighborBlockChange(BlockPos otherPos)
	{
		super.onNeighborBlockChange(otherPos);
		Level world = getLevelNonnull();
		Direction dir = Direction.getNearest(otherPos.getX()-worldPosition.getX(),
				otherPos.getY()-worldPosition.getY(), otherPos.getZ()-worldPosition.getZ(), null);
		if(dir==null)
			return;
		boolean changed = updateConnectionByte(dir);
		boolean neighborChanged = false;
		if(!world.isClientSide())
		{
			BlockEntity neighborTile = world.getBlockEntity(worldPosition.relative(dir));
			if(neighborTile instanceof FluidPipeBlockEntity neighborPipe)
			{
				neighborChanged = neighborPipe.updateConnectionByte(dir.getOpposite());
				if(neighborChanged)
					neighborPipe.markContainingBlockForUpdate(null);
			}
			if(changed||neighborChanged)
				indirectConnections.clearDimension(world);
		}
		if(changed)
		{
			world.updateNeighborsAtExceptFromFacing(worldPosition, getBlockState().getBlock(), dir, null);
			markContainingBlockForUpdate(null);
		}
	}

	static class PipeFluidHandler implements IFluidHandler
	{
		private static final Random CURRENT_TICK_RANDOM = new Random();

		FluidPipeBlockEntity pipe;
		Direction facing;

		public PipeFluidHandler(FluidPipeBlockEntity pipe, Direction facing)
		{
			this.pipe = pipe;
			this.facing = facing;
		}

		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		public FluidStack getFluidInTank(int tank)
		{
			return FluidStack.EMPTY;
		}

		public int getTankCapacity(int tank)
		{
			return FluidType.BUCKET_VOLUME;
		}

		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return tank==0;
		}

		public int fill(FluidStack resource, FluidAction doFill)
		{
			if(resource==null)
				return 0;
			int canAccept = resource.getAmount();
			if(canAccept <= 0)
				return 0;
			Set<DirectionalFluidOutput> outputList = getConnectedFluidHandlers(pipe.getBlockPos(), pipe.level);

			if(outputList.isEmpty())
				//NO OUTPUTS!
				return 0;
			BlockPos ccFrom = new BlockPos(pipe.getBlockPos().relative(facing));
			int sum = 0;
			HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
			for(DirectionalFluidOutput output : outputList)
			{
				BlockPos cc = output.pos;
				if(!cc.equals(ccFrom)&&pipe.level.hasChunkAt(cc)&&!pipe.getBlockPos().equals(output.pos))
				{
					int limit = getTransferableAmount(resource, output.containingTile);
					int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
					int temp = output.output.fill(Utils.copyFluidStackWithAmount(resource, tileSpecificAcceptedFluid, output.stripPressure()), FluidAction.SIMULATE);
					if(temp > 0)
					{
						sorting.put(output, temp);
						sum += temp;
					}
				}
			}
			if(sum > 0)
			{
				int f = 0;
				for(DirectionalFluidOutput output : sorting.keySet())
				{
					int amount = sorting.get(output);
					if(sum > resource.getAmount())
					{
						int limit = getTransferableAmount(resource, output.containingTile);
						int tileSpecificAcceptedFluid = Math.min(limit, canAccept);
						float prio = amount/(float)sum;
						amount = (int)Math.ceil(Mth.clamp(amount, 1,
								Math.min(resource.getAmount()*prio, tileSpecificAcceptedFluid)));
						amount = Math.min(amount, canAccept);
					}
					int r = output.output.fill(Utils.copyFluidStackWithAmount(resource, amount, output.stripPressure()), doFill);
					if(r > IFluidPipe.AMOUNT_UNPRESSURIZED)
						pipe.canOutputPressurized(output.containingTile, true);
					f += r;
					canAccept -= r;
					if(canAccept <= 0)
						break;
				}
				return f;
			}
			return 0;
		}

		private int getTransferableAmount(FluidStack resource, @Nullable BlockEntity target)
		{
			// certain targets may override the transfer limits. This only works for direct pipe connections.
			if(target instanceof IPressurizedFluidOutput pressurizedOutput)
				return pressurizedOutput.getMaxAcceptedFluidAmount(resource);

			return IFluidPipe.getTransferableAmount(
					resource.has(IEApiDataComponents.FLUID_PRESSURIZED)||pipe.canOutputPressurized(target, false)
			);
		}

		@Nonnull
		public FluidStack drain(FluidStack resource, FluidAction doDrain)
		{
			return this.drain(resource.getAmount(), doDrain);
		}

		@Nonnull
		public FluidStack drain(int maxDrain, FluidAction doDrain)
		{
			if(maxDrain <= 0)
				return FluidStack.EMPTY;

			Level world = pipe.getLevelNonnull();
			List<DirectionalFluidOutput> outputList = new ArrayList<>(getConnectedFluidHandlers(pipe.getBlockPos(), world));
			BlockPos ccFrom = new BlockPos(pipe.getBlockPos().relative(facing));
			outputList.removeIf(output -> ccFrom.equals(output.pos));

			if(outputList.size() < 1)
				return FluidStack.EMPTY;

			CURRENT_TICK_RANDOM.setSeed(HashCommon.mix(world.getGameTime()));
			int chosen = outputList.size()==1?0: CURRENT_TICK_RANDOM.nextInt(outputList.size());
			DirectionalFluidOutput output = outputList.get(chosen);
			FluidStack available = output.output.drain(maxDrain, FluidAction.SIMULATE);
			BlockEntity drainingBE = SafeChunkUtils.getSafeBE(world, this.pipe.getBlockPos().relative(this.facing));
			int limit = getTransferableAmount(available, drainingBE);
			int actualTake = Math.min(limit, maxDrain);
			return output.output.drain(actualTake, doDrain);
		}
	}

	public record DirectionalFluidOutput(
			IFluidHandler output,
			Direction direction,
			@Nullable BlockEntity containingTile,
			BlockPos pos
	)
	{
		boolean stripPressure()
		{
			if(containingTile instanceof IFluidPipe pipe)
				return pipe.stripPressureTag();
			return true;
		}
	}

	public boolean updateConnectionByte(Direction dir)
	{
		if(level==null||level.isClientSide()||!SafeChunkUtils.isChunkSafe(level, worldPosition.relative(dir)))
			return false;
		final byte oldConn = connections;
		int i = dir.get3DDataValue();
		int mask = 1<<i;
		connections &= ~mask;
		if(sideConfig.getOrDefault(dir, false))
		{
			if(level.getBlockEntity(worldPosition.relative(dir)) instanceof FluidPipeBlockEntity)
				connections |= mask;
			else
			{
				IFluidHandler handler = neighbors.get(dir).getCapability();
				if(handler!=null&&handler.getTanks() > 0)
					connections |= mask;
			}
		}
		return oldConn!=connections;
	}

	private boolean updateAllConnectionBytes()
	{
		boolean changed = false;
		for(Direction f : DirectionUtils.VALUES)
			changed |= updateConnectionByte(f);
		return changed;
	}

	public byte getAvailableConnectionByte()
	{
		byte availableConnections = getVisualConnectionByte();
		int mask = 1;
		for(Direction dir : DirectionUtils.VALUES)
		{
			if((availableConnections&mask)==0)
			{
				if(level.getBlockEntity(getBlockPos().relative(dir)) instanceof FluidPipeBlockEntity)
					availableConnections |= mask;
				else
				{
					IFluidHandler handler = neighbors.get(dir).getCapability();
					if(handler!=null&&handler.getTanks() > 0)
						availableConnections |= mask;
				}
			}
			mask <<= 1;
		}
		return availableConnections;
	}

	private byte getVisualConnectionByte()
	{
		byte visualConnections = connections;
		if(level==null||!level.isClientSide())
			return visualConnections;

		for(Direction dir : DirectionUtils.VALUES)
		{
			int mask = 1<<dir.get3DDataValue();
			if((visualConnections&mask)!=0||!sideConfig.getOrDefault(dir, false))
				continue;

			BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
			if(neighbor instanceof FluidPipeBlockEntity pipe)
			{
				if(pipe.sideConfig.getOrDefault(dir.getOpposite(), false))
					visualConnections |= mask;
			}
			else
			{
				IFluidHandler handler = neighbors.get(dir).getCapability();
				if(handler!=null&&handler.getTanks() > 0)
					visualConnections |= mask;
			}
		}
		return visualConnections;
	}

	public ConnectionStyle getConnectionStyle(Direction connection)
	{
		byte visualConnections = getVisualConnectionByte();
		if((visualConnections&(1<<connection.get3DDataValue()))==0)
			return ConnectionStyle.NO_CONNECTION;

		if(visualConnections!=3&&visualConnections!=12&&visualConnections!=48) //add flange if not a straight pipe
			return ConnectionStyle.FLANGE;
		BlockEntity con = SafeChunkUtils.getSafeBE(level, getBlockPos().relative(connection));
		if(con instanceof FluidPipeBlockEntity pipe)
		{
			int tileConnections = pipe.getVisualConnectionByte()|(1<<connection.getOpposite().get3DDataValue());
			if(visualConnections==tileConnections) //if neighbor pipe is also straight and in same direction, don't add flanges
				return ConnectionStyle.PLAIN;
		}
		return ConnectionStyle.FLANGE;
	}

	public void toggleSide(Direction side)
	{
		boolean newSideConnected = !sideConfig.getOrDefault(side, false);
		setSide(side, newSideConnected);
	}

	public void setSide(Direction side, boolean connectable)
	{
		setSide(side, connectable, true);
	}

	public void setSide(Direction side, boolean connectable, boolean firstPipe)
	{
		sideConfig.put(side, connectable);
		if(connectable)
			setValidHandler(side);
		else
			invalidateHandler(side);
		setChanged();
		if(firstPipe)
		{
			BlockEntity neighborTile = level.getBlockEntity(getBlockPos().relative(side));
			if(neighborTile instanceof FluidPipeBlockEntity neighborPipe)
			{
				neighborPipe.setSide(side.getOpposite(), connectable, false);
				neighborPipe.updateConnectionByte(side.getOpposite());
				neighborPipe.markContainingBlockForUpdate(null);
			}
			updateConnectionByte(side); //yes, this is not meant for neighborTile
		}
		level.blockEvent(getBlockPos(), getBlockState().getBlock(), 0, 0);
	}

	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private static final CachedVoxelShapes<BoundingBoxKey> SHAPES = new CachedVoxelShapes<>(FluidPipeBlockEntity::getBoxes);

	public VoxelShape getCollisionShape(CollisionContext ctx)
	{
		return SHAPES.get(new BoundingBoxKey(false, this));
	}

	public VoxelShape getSelectionShape(@Nullable CollisionContext ctx)
	{
		//TODO needs to be a more generic check!
		boolean hammer = ctx!=null&&ctx.isHoldingItem(Tools.HAMMER.get());
		return SHAPES.get(new BoundingBoxKey(hammer, this));
	}

	private static List<AABB> getBoxes(BoundingBoxKey key)
	{
		List<AABB> list = Lists.newArrayList();
		if(!key.showToolView&&key.hasCover)
		{
			list.add(new AABB(0, 0, 0, 1, 1, 1).inflate(-.03125f));
			return list;
		}
		byte availableConnections = key.availableConnections;
		byte activeConnections = key.connections;
		double[] baseAABB = key.hasCover?new double[]{.002, .998, .002, .998, .002, .998}: new double[]{.25, .75, .25, .75, .25, .75};
		for(Direction d : DirectionUtils.VALUES)
		{
			int i = d.get3DDataValue();
			if((availableConnections&1)==1)
			{
				if((activeConnections&1)==1||key.showToolView)
				{
					list.add(new AABB(
							i==4?0: i==5?0.75: 0.25, i==0?0: i==1?0.75: 0.25, i==2?0: i==3?0.75: 0.25,
							i==4?0.25: i==5?1: 0.75, i==0?0.25: i==1?1: 0.75, i==2?0.25: i==3?1: 0.75
					));
					if(key.connectionStyles.get(d)==ConnectionStyle.FLANGE)
						list.add(new AABB(
								i==4?0: i==5?0.875: 0.125, i==0?0: i==1?0.875: 0.125, i==2?0: i==3?0.875: 0.125,
								i==4?0.125: i==5?1: 0.875, i==0?0.125: i==1?1: 0.875, i==2?0.125: i==3?1: 0.875
						));
				}
			}
			availableConnections = (byte)(availableConnections>>1);
			activeConnections = (byte)(activeConnections>>1);
		}
		list.add(new AABB(baseAABB[4], baseAABB[0], baseAABB[2], baseAABB[5], baseAABB[1], baseAABB[3]));
		return list;
	}

	private static class BoundingBoxKey
	{
		private final boolean showToolView;
		private final byte connections;
		private final byte availableConnections;
		private final boolean hasCover;
		private final Map<Direction, ConnectionStyle> connectionStyles = new EnumMap<>(Direction.class);

		private BoundingBoxKey(boolean showToolView, FluidPipeBlockEntity te)
		{
			this.showToolView = showToolView;
			this.connections = te.getVisualConnectionByte();
			this.availableConnections = te.getAvailableConnectionByte();
			this.hasCover = te.hasCover();
			for(Direction d : DirectionUtils.VALUES)
				connectionStyles.put(d, te.getConnectionStyle(d));
		}

		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			BoundingBoxKey that = (BoundingBoxKey)o;
			return showToolView==that.showToolView&&
					connections==that.connections&&
					availableConnections==that.availableConnections&&
					hasCover==that.hasCover&&
					connectionStyles.equals(that.connectionStyles);
		}

		public int hashCode()
		{
			return Objects.hash(showToolView, connections, availableConnections, hasCover, connectionStyles);
		}

	}

	public int getRenderColour(int tintIndex)
	{
		return 0xffffff;
	}

	public void dropCover(Player player)
	{
		if(level instanceof ServerLevel serverLevel&&hasCover()&&serverLevel.getGameRules().get(GameRules.BLOCK_DROPS))
		{
			ItemEntity entityitem = player.drop(new ItemStack(cover), false);
			if(entityitem!=null)
				entityitem.setNoPickUpDelay();
		}
	}

	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem.isEmpty()&&player.isShiftKeyDown()&&hasCover())
		{
			if(!player.level().isClientSide())
			{
				dropCover(player);
				this.cover = Blocks.AIR;
				this.markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
				markChunkDirty();
			}
			return InteractionResult.SUCCESS;
		}
		else if(!heldItem.isEmpty()&&!player.isShiftKeyDown())
			return setColorOrCoverFrom(heldItem, player);
		return InteractionResult.PASS;
	}

	private InteractionResult setColorOrCoverFrom(ItemStack heldItem, Player player)
	{
		DyeColor heldDye = Utils.getDye(heldItem);
		if(heldDye!=null)
		{
			if(!player.level().isClientSide())
			{
				color = heldDye;
				markChunkDirty();
				markContainingBlockForUpdate(null);
				level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
			}
			return InteractionResult.SUCCESS;
		}
		Block heldBlock = Block.byItem(heldItem.getItem());
		if(heldBlock==Blocks.AIR)
			return InteractionResult.PASS;
		for(Predicate<Block> func : validPipeCovers)
			if(func.test(heldBlock)&&this.cover!=heldBlock)
			{
				if(!player.level().isClientSide())
				{
					dropCover(player);
					this.cover = heldBlock;
					markChunkDirty();
					if(!player.getAbilities().instabuild)
						heldItem.shrink(1);
					this.markContainingBlockForUpdate(null);
					level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
				}
				return InteractionResult.SUCCESS;
			}
		return InteractionResult.PASS;
	}

	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide())
			return true;
		hitVec = hitVec.subtract(Vec3.atLowerCornerOf(worldPosition));
		Direction fd = side;
		List<AABB> boxes = getBoxes(new BoundingBoxKey(true, this));
		for(AABB box : boxes)
			if(box.inflate(.002).contains(hitVec))
			{
				for(Direction d : DirectionUtils.VALUES)
				{
					Vec3 testVec = new Vec3(0.5+0.5*d.getStepX(), 0.5+0.5*d.getStepY(), 0.5+0.5*d.getStepZ());
					if(box.inflate(0.002).contains(testVec))
					{
						fd = d;
						break;
					}
				}
				break;
			}
		if(fd!=null)
		{
			toggleSide(fd);
			this.markContainingBlockForUpdate(null);
			FluidPipeBlockEntity.indirectConnections.clearDimension(level);
			return true;
		}
		return false;
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final Level level = ctx.getLevel();
		if(level.isClientSide())
			return;
		if(ctx.getPlayer()!=null)
		{
			final InteractionHand otherHand = ctx.getHand()==InteractionHand.MAIN_HAND?
					InteractionHand.OFF_HAND:
					InteractionHand.MAIN_HAND;
			setColorOrCoverFrom(ctx.getPlayer().getItemInHand(otherHand), ctx.getPlayer());
		}
		final BlockPos pos = ctx.getClickedPos();
		for(Direction dir : Direction.values())
			if(level.getBlockEntity(pos.relative(dir)) instanceof FluidPipeBlockEntity neighborPipe)
				if(neighborPipe.color!=this.color||!neighborPipe.sideConfig.getOrDefault(dir.getOpposite(), false))
					this.setSide(dir, false);
		boolean changed = updateAllConnectionBytes();
		for(Direction dir : DirectionUtils.VALUES)
		{
			BlockEntity neighborTile = level.getBlockEntity(pos.relative(dir));
			if(neighborTile instanceof FluidPipeBlockEntity neighborPipe&&neighborPipe.updateConnectionByte(dir.getOpposite()))
			{
				neighborPipe.markContainingBlockForUpdate(null);
				changed = true;
			}
		}
		if(changed)
		{
			level.updateNeighborsAt(pos, getBlockState().getBlock());
			markContainingBlockForUpdate(null);
			indirectConnections.clearDimension(level);
		}
	}

	public boolean hasOutputConnection(Direction side)
	{
		return sideConfig.getOrDefault(side, false);
	}

	@SubscribeEvent
	public static void onWorldUnload(LevelEvent.Unload ev)
	{
		if(!ev.getLevel().isClientSide()&&ev.getLevel() instanceof Level level)
			indirectConnections.clearDimension(level);
	}

	@Nullable
	public DyeColor getColor()
	{
		return color;
	}

	public enum ConnectionStyle
	{
		NO_CONNECTION,
		PLAIN,
		FLANGE
	}
}
