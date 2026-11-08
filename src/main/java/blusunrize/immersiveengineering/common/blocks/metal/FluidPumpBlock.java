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
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FluidPumpBlock extends IEEntityBlock<FluidPumpBlockEntity>
{
	public FluidPumpBlock(Properties props)
	{
		super(IEBlockEntities.FLUID_PUMP, props);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, BlockStateProperties.WATERLOGGED);
	}

	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context).setValue(IEProperties.FACING_ALL, Direction.UP);
	}

	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		BlockPos start = context.getClickedPos();
		return areAllReplaceable(start, start.above(), context);
	}
}
