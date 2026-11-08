/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.wires.WireApi.INFOS;

public class FeedthroughBlockEntity extends ImmersiveConnectableBlockEntity implements IBlockEntityDrop,
		IBlockBounds, IStateBasedDirectional
{
	public static final String WIRE = "wire";
	private static final String OFFSET = "offset";
	public static final String MIDDLE_STATE = "middle";

	@Nonnull
	public WireType reference = WireType.COPPER;
	@Nonnull
	public BlockState stateForMiddle = Blocks.DIRT.defaultBlockState();
	public int offset = 0;
	public boolean currentlyDisassembling = false;

	public FeedthroughBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FEEDTHROUGH.get(), pos, state);
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putString(WIRE, reference.getUniqueName());
		nbt.putInt(OFFSET, offset);
		CompoundTag stateNbt = NbtUtils.writeBlockState(stateForMiddle);
		nbt.put(MIDDLE_STATE, stateNbt);
	}

	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		reference = WireType.getValue(nbt.getStringOr(WIRE, ""));
		offset = nbt.getIntOr(OFFSET, 0);
		HolderGetter<Block> lookup = this.level != null ?
				this.level.holderLookup(Registries.BLOCK) :
				BuiltInRegistries.BLOCK;
		stateForMiddle = NbtUtils.readBlockState(lookup, nbt.getCompoundOrEmpty(MIDDLE_STATE));
	}

	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		double l = INFOS.get(reference).connOffset();
		int factor;
		if(here.equals(getPositivePoint()))
			factor = 1;
		else
			factor = -1;
		return new Vec3(.5+(.5+l)*getFacing().getStepX()*factor, .5+(.5+l)*getFacing().getStepY()*factor,
				.5+(.5+l)*getFacing().getStepZ()*factor);
	}

	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(!WireApi.canMix(reference, cableType))
			return false;
		Collection<Connection> existing = globalNet.getLocalNet(target).getConnections(target);
		for(Connection c : existing)
			if(!c.isInternal())
				return false;
		return true;
	}

	public Set<BlockPos> getIgnored(IImmersiveConnectable other)
	{
		return ImmutableSet.of(worldPosition.relative(getFacing(), 1), worldPosition.relative(getFacing(), -1));
	}

	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return worldPosition.relative(getFacing(), -offset);
	}

	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		WireApi.FeedthroughModelInfo info = INFOS.get(reference);
		if(offset==0)
			Utils.getDrops(stateForMiddle, context, drop);
		else
			drop.accept(new ItemStack(info.connector().getBlock()));
	}

	public ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult rayRes)
	{
		if(offset==0)
			return Utils.getPickBlock(stateForMiddle, rayRes, player);
		return IBlockEntityDrop.super.getPickBlock(player, state, rayRes);
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		ItemData data = stack.get(IEDataComponents.FEEDTHROUGH_DATA);
		if(data!=null)
		{
			reference = data.type();
			stateForMiddle = data.middleState();
		}
	}

	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.PISTON_LIKE;
	}

	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	private VoxelShape aabb;

	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(offset==0)
			return Shapes.block();
		if(aabb==null)
			aabb = EnergyConnectorBlockEntity.getConnectorBounds(
					offset < 0?getFacing(): getFacing().getOpposite(), (float)INFOS.get(reference).connLength()
			);
		return aabb;
	}

	public boolean triggerEvent(int id, int arg)
	{
		if(id==253)
		{
			checkLight();
			return true;
		}
		return super.triggerEvent(id, arg);
	}

	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(getNegativePoint(), getPositivePoint());
	}

	//pos.add(facing.getOpposite().getDirectionVec())
	public ConnectionPoint getNegativePoint()
	{
		return new ConnectionPoint(worldPosition, getIndexForOffset(-1));
	}

	//pos.add(facing.getDirectionVec())
	public ConnectionPoint getPositivePoint()
	{
		return new ConnectionPoint(worldPosition, getIndexForOffset(1));
	}

	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	public Iterable<? extends Connection> getInternalConnections()
	{
		return ImmutableList.of(new Connection(worldPosition, 0, 1));
	}

	@Nullable
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		if(offset.equals(getFacing().getUnitVec3i()))
			return getPositivePoint();
		else if(offset.equals(getFacing().getOpposite().getUnitVec3i()))
			return getNegativePoint();
		else
			return null;
	}

	public static int getIndexForOffset(int offset)
	{
		if(offset==-1)
			return 1;
		else if(offset==1)
			return 0;
		else
			return -1;
	}

	public record FeedthroughData(
			BlockState baseState,
			WireType wire,
			Direction facing,
			int offset,
			int colorMultiplier
	)
	{
	}

	public record ItemData(
			WireType type,
			BlockState middleState
	)
	{
		public static final DualCodec<ByteBuf, ItemData> CODECS = DualCompositeCodecs.composite(
				WireType.CODECS.fieldOf("type"), ItemData::type,
				DualCodecs.BLOCK_STATE.fieldOf("middleState"), ItemData::middleState,
				ItemData::new
		);
		public static final ItemData FALLBACK = new ItemData(IEWireTypes.COPPER, Blocks.STONE.defaultBlockState());
	}
}
