/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.conveyor.BasicConveyorType;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler.IConveyorBlockEntity;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.client.render.conveyor.SplitConveyorRender;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author BluSunrize - 20.08.2016
 */
public class SplitConveyor extends ConveyorBase
{
	public static final Identifier NAME = ImmersiveEngineering.rl("splitter");
	public static final Identifier texture_on = ImmersiveEngineering.rl("block/conveyor/split");
	public static final Identifier texture_off = ImmersiveEngineering.rl("block/conveyor/split_off");
	public static final IConveyorType<SplitConveyor> TYPE = new BasicConveyorType<>(
			NAME, false, true, SplitConveyor::new, () -> new SplitConveyorRender(texture_on, texture_off)
	);

	boolean nextOutputLeft = true;

	public SplitConveyor(BlockEntity tile)
	{
		super(tile);
	}

	public IConveyorType<SplitConveyor> getType()
	{
		return TYPE;
	}

	public ConveyorDirection getConveyorDirection()
	{
		return ConveyorDirection.HORIZONTAL;
	}

	public boolean changeConveyorDirection()
	{
		return false;
	}

	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	public void handleInsertion(ItemEntity entity, ConveyorDirection conDir, double distX, double distZ)
	{
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
		if(entity.getPersistentData().contains(nbtKey))
		{
			Direction redirect = Direction.values()[entity.getPersistentData().getIntOr(nbtKey, 0)];
			BlockPos nextPos = getBlockEntity().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
			BlockEntity inventoryTile = getBlockEntity().getLevel().getBlockEntity(nextPos);
			if(distNext < .7&&inventoryTile!=null&&!(inventoryTile instanceof IConveyorBlockEntity))
				ItemUtils.tryInsertEntity(getBlockEntity().getLevel(), getBlockEntity().getBlockPos().relative(redirect), redirect.getOpposite(), entity);
		}
	}

	public void onEntityCollision(@Nonnull Entity entity)
	{
		if(!isActive())
			return;
		Direction redirect = null;
		if(entity.isAlive())
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
			if(entity.getPersistentData().contains(nbtKey))
				redirect = Direction.values()[entity.getPersistentData().getIntOr(nbtKey, 0)];
			else
			{
				redirect = getOutputFace();
				entity.getPersistentData().putInt(nbtKey, redirect.ordinal());
				BlockPos nextPos = getBlockEntity().getBlockPos().relative(this.getOutputFace().getOpposite());
				if(getBlockEntity().getLevel().hasChunkAt(nextPos))
				{
					BlockEntity nextTile = getBlockEntity().getLevel().getBlockEntity(nextPos);
					if(!(nextTile instanceof IConveyorBlockEntity))
						nextOutputLeft = !nextOutputLeft;
					else if(((IConveyorBlockEntity<?>)nextTile).getFacing()!=this.getOutputFace())
						nextOutputLeft = !nextOutputLeft;
				}
			}
		}
		super.onEntityCollision(entity);
		if(redirect!=null)
		{
			String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
			BlockPos nextPos = getBlockEntity().getBlockPos().relative(redirect);
			double distNext = Math.abs((redirect.getAxis()==Axis.Z?nextPos.getZ(): nextPos.getX())+.5-(redirect.getAxis()==Axis.Z?entity.getZ(): entity.getX()));
			double treshold = .4;
			boolean contact = distNext < treshold;
			if(contact)
				entity.getPersistentData().remove(nbtKey);
		}
	}

	public Direction[] sigTransportDirections()
	{
		return new Direction[]{getFacing().getClockWise(), getFacing().getCounterClockWise()};
	}

	public Vec3 getDirection(Entity entity, boolean outputBlocked)
	{
		Vec3 vec = super.getDirection(entity, outputBlocked);
		String nbtKey = "immersiveengineering:conveyorDir"+Integer.toHexString(getBlockEntity().getBlockPos().hashCode());
		if(!entity.getPersistentData().contains(nbtKey))
			return vec;
		Direction redirect = Direction.from3DDataValue(entity.getPersistentData().getIntOr(nbtKey, 0));
		BlockPos wallPos = getBlockEntity().getBlockPos().relative(getFacing());
		double distNext = Math.abs((getFacing().getAxis()==Axis.Z?wallPos.getZ(): wallPos.getX())+.5-(getFacing().getAxis()==Axis.Z?entity.getZ(): entity.getX()));
		if(distNext < 1.33)
		{
			double sideMove = Math.pow(1+distNext, 0.1)*.2;
			if(distNext < .8)
				vec = new Vec3(getFacing().getAxis()==Axis.X?0: vec.x, vec.y, getFacing().getAxis()==Axis.Z?0: vec.z);
			vec = vec.add(redirect.getStepX()*sideMove, 0, redirect.getStepZ()*sideMove);
		}
		return vec;
	}

	public CompoundTag writeConveyorNBT()
	{
		CompoundTag nbt = super.writeConveyorNBT();
		nbt.putBoolean("nextLeft", nextOutputLeft);
		return nbt;
	}

	public void readConveyorNBT(CompoundTag nbt)
	{
		super.readConveyorNBT(nbt);
		nextOutputLeft = nbt.getBooleanOr("nextLeft", false);
	}

	public List<BlockPos> getNextConveyorCandidates()
	{
		BlockPos baseOutput = getBlockEntity().getBlockPos().relative(getOutputFace());
		return ImmutableList.of(
				baseOutput,
				baseOutput.below()
		);
	}

	public boolean isOutputBlocked()
	{
		// Consider the belt blocked if at least one of the possible outputs is blocked
		Direction outputFace = getOutputFace();
		BlockPos here = getBlockEntity().getBlockPos();
		for(BlockPos outputPos : new BlockPos[]{
				here.relative(outputFace, 1),
				here.relative(outputFace, -1),
		})
		{
			BlockEntity tile = SafeChunkUtils.getSafeBE(getBlockEntity().getLevel(), outputPos);
			if(tile instanceof IConveyorBlockEntity&&((IConveyorBlockEntity<?>)tile).getConveyorInstance().isBlocked())
				return true;
		}
		return false;
	}

	private Direction getOutputFace()
	{
		if(nextOutputLeft)
			return getFacing().getCounterClockWise();
		else
			return getFacing().getClockWise();
	}
}
