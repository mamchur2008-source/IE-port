/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class VerticalFacingBlock extends IEBaseBlock
{
	public VerticalFacingBlock(Properties blockProps)
	{
		super(blockProps);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_TOP_DOWN);
	}

	protected Direction getDefaultFacing()
	{
		return Direction.UP;
	}

	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		return ret.setValue(IEProperties.FACING_TOP_DOWN, getDefaultFacing());
	}

	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		if(pContext.getClickedFace().getAxis()==Axis.Y)
			return this.defaultBlockState().setValue(IEProperties.FACING_TOP_DOWN,pContext.getClickedFace());
		Direction d = pContext.getClickLocation().y<.5?Direction.UP:Direction.DOWN;
		return this.defaultBlockState().setValue(IEProperties.FACING_TOP_DOWN, d);
	}
}
