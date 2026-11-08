/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WatermillBlock extends IEEntityBlock<WatermillBlockEntity>
{
	public WatermillBlock(Properties props)
	{
		super(IEBlockEntities.WATERMILL, props);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.getBlock()!=newState.getBlock()&&!world.isClientSide())
		{
			if(!state.getValue(IEProperties.MULTIBLOCKSLAVE))
				WatermillBlockEntity.clearWatermillBlocks(
						world, pos, state.getValue(IEProperties.FACING_HORIZONTAL), state.getBlock(), false
				);
			else if(!(world.getBlockEntity(pos) instanceof WatermillBlockEntity))
			{
				BlockPos center = getWatermillCenter(world, pos, state);
				if(center!=null)
					WatermillBlockEntity.clearWatermillBlocks(
							world, center, state.getValue(IEProperties.FACING_HORIZONTAL), state.getBlock()
					);
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		BlockPos center = context.getClickedPos();
		Level world = context.getLevel();
		Direction facing = context.getHorizontalDirection();
		Player player = context.getPlayer();
		CollisionContext selectionCtx = player==null?CollisionContext.empty(): CollisionContext.of(player);
		BlockState stateToPlace = defaultBlockState();
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = center.offset(facing.getAxis()==Axis.Z?ww: 0, hh, facing.getAxis()==Axis.Z?0: ww);
					BlockState state = world.getBlockState(pos2);
					if(!state.canBeReplaced(BlockPlaceContext.at(context, pos2, facing))||
							!world.isUnobstructed(stateToPlace, pos2, selectionCtx))
						return false;
				}
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if(isOrphanedWatermillSlave(world, pos, state))
		{
			if(world instanceof Level level)
				removeOrphanedWatermillSlaves(level, pos);
			return Shapes.empty();
		}
		return super.getShape(state, world, pos, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if(isOrphanedWatermillSlave(world, pos, state))
		{
			if(world instanceof Level level)
				removeOrphanedWatermillSlaves(level, pos);
			return Shapes.empty();
		}
		return super.getCollisionShape(state, world, pos, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos)
	{
		if(isOrphanedWatermillSlave(world, pos, state))
		{
			if(world instanceof Level level)
				removeOrphanedWatermillSlaves(level, pos);
			return Shapes.empty();
		}
		return super.getInteractionShape(state, world, pos);
	}

	private boolean isOrphanedWatermillSlave(BlockGetter world, BlockPos pos, BlockState state)
	{
		return state.getBlock()==this
				&&state.getValue(IEProperties.MULTIBLOCKSLAVE)
				&&getWatermillCenter(world, pos, state)==null;
	}

	private void removeOrphanedWatermillSlaves(Level world, BlockPos origin)
	{
		if(world.isClientSide())
			return;
		for(int xx = -4; xx <= 4; xx++)
			for(int yy = -4; yy <= 4; yy++)
				for(int zz = -4; zz <= 4; zz++)
				{
					BlockPos current = origin.offset(xx, yy, zz);
					BlockState state = world.getBlockState(current);
					if(state.getBlock()==this
							&&state.getValue(IEProperties.MULTIBLOCKSLAVE)
							&&getWatermillCenter(world, current, state)==null)
						world.removeBlock(current, false);
				}
	}

	private static BlockPos getWatermillCenter(BlockGetter world, BlockPos pos, BlockState state)
	{
		if(!state.getValue(IEProperties.MULTIBLOCKSLAVE))
			return pos;
		Direction facing = state.getValue(IEProperties.FACING_HORIZONTAL);
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if((hh > -2&&hh < 2)||(ww > -2&&ww < 2))
				{
					BlockPos center = pos.offset(facing.getAxis()==Axis.Z?-ww: 0, -hh, facing.getAxis()==Axis.Z?0: -ww);
					BlockState centerState = world.getBlockState(center);
					if(centerState.getBlock()==state.getBlock()
							&&!centerState.getValue(IEProperties.MULTIBLOCKSLAVE)
							&&centerState.getValue(IEProperties.FACING_HORIZONTAL)==facing)
						return center;
				}
		return null;
	}
}
