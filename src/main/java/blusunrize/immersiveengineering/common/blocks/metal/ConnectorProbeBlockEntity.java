/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ConnectorProbeBlockEntity extends ConnectorRedstoneBlockEntity
{
	public DyeColor redstoneChannelSending = DyeColor.WHITE;
	public int outputThreshold = 0;
	private int lastOutput = 0;

	public ConnectorProbeBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CONNECTOR_PROBE.get(), pos, state);
	}

	public void tickServer()
	{
		super.tickServer();
		if(level.getGameTime()%8==((getBlockPos().getX()^getBlockPos().getZ())&7))
		{
			int out = getComparatorSignal();
			out = out >= outputThreshold?out: 0;
			if(out!=lastOutput)
			{
				this.lastOutput = out;
				this.rsDirty = true;
			}
		}
	}

	public boolean isRSInput()
	{
		return true;
	}

	public boolean isRSOutput()
	{
		return true;
	}

	private int getComparatorSignal()
	{
		BlockPos pos = this.getBlockPos().relative(getFacing());
		BlockState state = level.getBlockState(pos);
		if(state.hasAnalogOutputSignal())
			return state.getAnalogOutputSignal(level, pos, getFacing());
		else if(state.isRedstoneConductor(level, pos))
		{
			pos = pos.relative(getFacing());
			state = level.getBlockState(pos);
			if(state.hasAnalogOutputSignal())
				return state.getAnalogOutputSignal(level, pos, getFacing());
			else if(state.isAir())
			{
				ItemFrame entityitemframe = this.findItemFrame(level, getFacing(), pos);
				if(entityitemframe!=null)
					return entityitemframe.getAnalogOutput();
			}
		}
		return 0;
	}

	private ItemFrame findItemFrame(Level world, final Direction facing, BlockPos pos)
	{
		List<ItemFrame> list = world.getEntitiesOfClass(ItemFrame.class, new AABB(pos), entity -> entity!=null&&entity.getDirection()==facing);
		return list.size()==1?list.get(0): null;
	}

	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		signals[redstoneChannelSending.ordinal()] = (byte)lastOutput;
		rsDirty = false;
	}

	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide())
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneProbe, this);
		return InteractionResult.SUCCESS;
	}

	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getIntOr("redstoneChannel", 0));
		if(message.contains("redstoneChannelSending"))
			redstoneChannelSending = DyeColor.byId(message.getIntOr("redstoneChannelSending", 0));
		if(message.contains("outputThreshold"))
			outputThreshold = message.getIntOr("outputThreshold", 0);
		updateAfterConfigure();
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("redstoneChannelSending", redstoneChannelSending.getId());
		nbt.putInt("outputThreshold", outputThreshold);
	}

	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		redstoneChannelSending = DyeColor.byId(nbt.getIntOr("redstoneChannelSending", 0));
		outputThreshold = nbt.getIntOr("outputThreshold", 0);
	}

	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = type.getRenderDiameter()/2;
		return new Vec3(.5+side.getStepX()*(.375-conRadius), .5+side.getStepY()*(.375-conRadius), .5+side.getStepZ()*(.375-conRadius));
	}

	private static final Map<Direction.Axis, VoxelShape> SHAPES = Util.make(
			new EnumMap<>(Direction.Axis.class), map -> {
				final float wMin = .28125f;
				final float wMax = .71875f;
				map.put(Axis.X, Shapes.box(0, wMin, wMin, 1, wMax, wMax));
				map.put(Axis.Y, Shapes.box(wMin, 0, wMin, wMax, 1, wMax));
				map.put(Axis.Z, Shapes.box(wMin, wMin, 0, wMax, wMax, 1));
			}
	);

	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(getFacing().getAxis());
	}

	public Pair<DyeColor, Byte>[] overrideVoltmeterRead()
	{
		return new Pair[]{
				Pair.of(this.redstoneChannel, (byte)this.output),
				Pair.of(this.redstoneChannelSending, (byte)this.lastOutput),
		};
	}

	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult mop, boolean hammer)
	{
		if(Utils.isScrewdriver(player.getMainHandItem()))
			return new Component[]{
					getChannelComponent("_receiving", redstoneChannel),
					getChannelComponent("_sending", redstoneChannelSending),
			};
		return null;
	}
}
