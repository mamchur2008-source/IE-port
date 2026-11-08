/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class RedstoneStateCellBlockEntity extends ConnectorRedstoneBlockEntity
{
	public DyeColor redstoneChannelSet = DyeColor.WHITE;
	public DyeColor redstoneChannelReset = DyeColor.WHITE;

	boolean wasToggled = false;

	public RedstoneStateCellBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.REDSTONE_STATE_CELL.get(), pos, state);
	}

	public void tickServer()
	{
		super.tickServer();

		if(level instanceof ServerLevel serverLevel&&this.output > 0&&level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
		{
			Direction dir = getFacing();
			Vec3 particlePos = Vec3.atCenterOf(getPosition()).add(dir.getStepX()*.25, dir.getStepY()*.25, dir.getStepZ()*.25);
			serverLevel.sendParticles(
					new DustParticleOptions(0xff0000, .5f),
					particlePos.x, particlePos.y, particlePos.z, 4,
					0.05, 0.05, 0.05, 0
			);
		}
	}

	public boolean isRSInput()
	{
		return false;
	}

	public boolean isRSOutput()
	{
		return false;
	}

	public boolean canConnectRedstone(@Nonnull Direction side)
	{
		return false;
	}

	public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler)
	{
		if(!level.isClientSide()&&SafeChunkUtils.isChunkSafe(level, worldPosition))
		{
			int setVal = handler.getValue(redstoneChannelSet.getId());
			int resetVal = handler.getValue(redstoneChannelReset.getId());
			final int oldOutput = this.output;

			if(redstoneChannelReset==redstoneChannelSet)
			{
				// toggle mode
				if(setVal <= 0)
					this.wasToggled = false;
				else if(!wasToggled)
				{
					this.output = this.output > 0?0: setVal;
					this.wasToggled = true;
				}
			}
			else if(setVal > 0) // set value
				this.output = setVal;
			else if(resetVal > 0) // reset value
				this.output = 0;
			if(!isRemoved()&&this.output!=oldOutput)
				this.rsDirty = true;
		}
	}


	public void updateInput(byte[] signals, ConnectionPoint cp)
	{
		signals[redstoneChannel.ordinal()] = (byte)this.output;
		rsDirty = false;
	}

	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(level.isClientSide())
			ImmersiveEngineering.proxy.openTileScreen(Lib.GUIID_RedstoneStateCell, this);
		return InteractionResult.SUCCESS;
	}

	public void receiveMessageFromClient(CompoundTag message)
	{
		if(message.contains("redstoneChannelSet"))
			redstoneChannelSet = DyeColor.byId(message.getIntOr("redstoneChannelSet", 0));
		if(message.contains("redstoneChannelReset"))
			redstoneChannelReset = DyeColor.byId(message.getIntOr("redstoneChannelReset", 0));
		if(message.contains("redstoneChannel"))
			redstoneChannel = DyeColor.byId(message.getIntOr("redstoneChannel", 0));
		updateAfterConfigure();
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("redstoneChannelSet", redstoneChannelSet.getId());
		nbt.putInt("redstoneChannelReset", redstoneChannelReset.getId());
		nbt.putBoolean("wasToggled", wasToggled);
	}

	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		redstoneChannelSet = DyeColor.byId(nbt.getIntOr("redstoneChannelSet", 0));
		redstoneChannelReset = DyeColor.byId(nbt.getIntOr("redstoneChannelReset", 0));
		wasToggled = nbt.getBooleanOr("wasToggled", false);
	}

	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing().getOpposite();
		double conRadius = type.getRenderDiameter()/2;
		return new Vec3(.5+side.getStepX()*(.25-conRadius), .5+side.getStepY()*(.25-conRadius), .5+side.getStepZ()*(.25-conRadius));
	}

	private static final Map<Direction, VoxelShape> SHAPES = Util.make(
			new EnumMap<>(Direction.class), map -> {
				final float wMin = .3125f;
				final float wMax = .6875f;
				map.put(Direction.NORTH, Shapes.box(wMin, wMin, 0, wMax, wMax, .875));
				map.put(Direction.SOUTH, Shapes.box(wMin, wMin, .125, wMax, wMax, 1));
				map.put(Direction.WEST, Shapes.box(0, wMin, wMin, .875, wMax, wMax));
				map.put(Direction.EAST, Shapes.box(.125, wMin, wMin, 1, wMax, wMax));
				map.put(Direction.DOWN, Shapes.box(wMin, 0, wMin, wMax, .875, wMax));
				map.put(Direction.UP, Shapes.box(wMin, .125, wMin, wMax, 1, wMax));
			}
	);

	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return SHAPES.get(getFacing());
	}

	public Pair<DyeColor, Byte>[] overrideVoltmeterRead()
	{
		return new Pair[]{
				Pair.of(this.redstoneChannel, (byte)this.output),
		};
	}


	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult mop, boolean hammer)
	{
		if(Utils.isScrewdriver(player.getMainHandItem()))
			return new Component[]{
					getChannelComponent("_set", redstoneChannelSet),
					getChannelComponent("_reset", redstoneChannelReset),
					getChannelComponent("_output", redstoneChannel),
			};
		return null;
	}
}
