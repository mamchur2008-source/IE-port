/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;


import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract class ImmersiveConnectableBlockEntity extends IEBaseBlockEntity implements IImmersiveConnectable
{
	protected GlobalWireNetwork globalNet;

	public ImmersiveConnectableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public boolean canConnect()
	{
		return true;
	}

	public BlockPos getConnectionMaster(WireType cableType, TargetingInfo target)
	{
		return getBlockPos();
	}

	public void connectCable(WireType cableType, ConnectionPoint target, IImmersiveConnectable other, ConnectionPoint otherTarget)
	{
	}

	public void removeCable(Connection connection, ConnectionPoint attachedPoint)
	{
		this.setChanged();
	}

	public void setLevel(Level worldIn)
	{
		super.setLevel(worldIn);
		globalNet = GlobalWireNetwork.getNetwork(worldIn);
	}

	@Nullable
	public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset)
	{
		return new ConnectionPoint(worldPosition, 0);
	}

	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
	}

	public void writeCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
	}

	public void onChunkUnloaded()
	{
		super.onChunkUnloaded();
		ConnectorBlockEntityHelper.onChunkUnload(globalNet, this);
	}

	public void onLoad()
	{
		super.onLoad();
		ConnectorBlockEntityHelper.onChunkLoad(this, level);
	}

	public void setRemovedIE()
	{
		super.setRemovedIE();
		ConnectorBlockEntityHelper.remove(level, this);
	}

	public Collection<ConnectionPoint> getConnectionPoints()
	{
		return ImmutableList.of(new ConnectionPoint(worldPosition, 0));
	}

	private final Int2ObjectMap<LocalWireNetwork> cachedLocalNets = new Int2ObjectArrayMap<>();

	protected LocalWireNetwork getLocalNet(int cpIndex)
	{
		return ConnectorBlockEntityHelper.getLocalNetWithCache(globalNet, getBlockPos(), cpIndex, cachedLocalNets);
	}

	public BlockPos getPosition()
	{
		return worldPosition;
	}
}
