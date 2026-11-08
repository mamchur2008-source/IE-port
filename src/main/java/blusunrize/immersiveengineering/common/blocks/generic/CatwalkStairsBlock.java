/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.api.utils.shapes.ShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CatwalkStairsBlock extends CatwalkBlock
{
	public static BooleanProperty RAILING_LEFT = BooleanProperty.create("railing_left");
	public static BooleanProperty RAILING_RIGHT = BooleanProperty.create("railing_right");

	public CatwalkStairsBlock(Properties blockProps, boolean isDyeable)
	{
		super(blockProps, isDyeable);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(DYE_PROPERTY, BlockStateProperties.WATERLOGGED, IEProperties.FACING_HORIZONTAL, RAILING_LEFT, RAILING_RIGHT);
	}

	protected BlockState getInitDefaultState()
	{
		BlockState ret = this.stateDefinition.any()
				.setValue(BlockStateProperties.WATERLOGGED, false)
				.setValue(IEProperties.FACING_HORIZONTAL, Direction.NORTH)
				.setValue(RAILING_LEFT, false)
				.setValue(RAILING_RIGHT, false);
		if(this.isDyeable)
			ret = ret.setValue(DYE_PROPERTY, DyeColor.WHITE);
		return ret;
	}

	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return super.getStateForPlacement(context)
				.setValue(IEProperties.FACING_HORIZONTAL, context.getHorizontalDirection());
	}

	public InteractionResult useHammer(BlockState state, Level world, BlockPos pos, @Nullable Player player, UseOnContext context)
	{
		Direction currentDirection = state.getValue(IEProperties.FACING_HORIZONTAL);
		if(player!=null && player.isShiftKeyDown())
		{
			Vec3 hitVec = context.getClickLocation().subtract(Vec3.atCenterOf(pos));
			BooleanProperty railing = switch(currentDirection)
			{
				case DOWN, UP, NORTH -> hitVec.x < 0?RAILING_LEFT: RAILING_RIGHT;
				case SOUTH -> hitVec.x < 0?RAILING_RIGHT: RAILING_LEFT;
				case WEST -> hitVec.z < 0?RAILING_RIGHT: RAILING_LEFT;
				case EAST -> hitVec.z < 0?RAILING_LEFT: RAILING_RIGHT;
			};
			world.setBlock(pos, state.setValue(railing, !state.getValue(railing)), 3);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public BlockState rotate(BlockState state, Rotation rotation)
	{
		Direction currentDirection = state.getValue(IEProperties.FACING_HORIZONTAL);
		Direction newDirection = rotation.rotate(currentDirection);
		return state.setValue(IEProperties.FACING_HORIZONTAL, newDirection);
	}

	private static final CachedVoxelShapes<RailingsKey> SHAPES = new CachedVoxelShapes<>(railingsKey -> {
		ArrayList<AABB> list = new ArrayList<>();
		// STEPS
		list.add(ShapeUtils.transformAABB(
				new AABB(0, 0, .5, 1, .125, 1),
				railingsKey.direction
		));
		list.add(ShapeUtils.transformAABB(
				new AABB(0, .5, 0, 1, .625, .5),
				railingsKey.direction
		));
		// RAILING
		final double height = railingsKey.collision?1.5: 1;
		if(railingsKey.left())
		{
			list.add(ShapeUtils.transformAABB(
					new AABB(0, 0, .5, .0625, height, 1),
					railingsKey.direction
			));
			list.add(ShapeUtils.transformAABB(
					new AABB(0, .5, 0, .0625, height+.5, .5),
					railingsKey.direction
			));
		}
		if(railingsKey.right())
		{
			list.add(ShapeUtils.transformAABB(
					new AABB(.9375, 0, .5, 1, height, 1),
					railingsKey.direction
			));
			list.add(ShapeUtils.transformAABB(
					new AABB(.9375, .5, 0, 1, height+.5, .5),
					railingsKey.direction
			));
		}
		return list;
	});

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(new RailingsKey(state, false));
	}

	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(new RailingsKey(state, true));
	}

	private record RailingsKey(Direction direction, boolean left, boolean right, boolean collision)
	{
		public RailingsKey(BlockState blockState, boolean collision)
		{
			this(blockState.getValue(IEProperties.FACING_HORIZONTAL),
					blockState.getValue(RAILING_LEFT),
					blockState.getValue(RAILING_RIGHT),
					collision);
		}
	}

}
