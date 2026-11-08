/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

public class LanternBlock extends IEBaseBlock implements IBlockOverlayText
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.mapColor(MapColor.METAL)
			.sound(SoundType.METAL)
			.strength(3, 15)
			.lightLevel(b -> b.getValue(IEProperties.INT_16))
			.noOcclusion();

	public static final Property<Direction> FACING = IEProperties.FACING_ALL;

	public LanternBlock(Properties props)
	{
		super(props);
	}

	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(FACING, BlockStateProperties.WATERLOGGED, IEProperties.INT_16);
	}

	protected BlockState getInitDefaultState()
	{
		return super.getInitDefaultState().setValue(IEProperties.INT_16, 14);
	}

	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Level level, BlockPos pos, BlockHitResult hit)
	{
		if(level instanceof ServerLevel)
			level.setBlock(pos, level.getBlockState(pos).cycle(IEProperties.INT_16), 3);
		return InteractionResult.SUCCESS;
	}

	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult mop, boolean hammer)
	{
		if(blockState!=null&&Utils.isScrewdriver(player.getMainHandItem()))
			return new Component[]{Component.translatable(Lib.CHAT_INFO+"light_level", blockState.getValue(IEProperties.INT_16))};
		return null;
	}

	private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.<Direction, VoxelShape>builder()
			.put(Direction.DOWN, Shapes.box(0.25, 0.125, 0.25, 0.75, 1, 0.75))
			.put(Direction.UP, Shapes.box(0.25, 0, 0.25, 0.75, 0.875, 0.75))
			.put(Direction.NORTH, Shapes.box(0.25, 0.0625, 0.25, 0.75, 0.875, 1))
			.put(Direction.EAST, Shapes.box(0, 0.0625, 0.25, 0.75, 0.875, 0.75))
			.put(Direction.SOUTH, Shapes.box(0.25, 0.0625, 0, 0.75, 0.875, 0.75))
			.put(Direction.WEST, Shapes.box(0.25, 0.0625, 0.25, 1, 0.875, 0.75))
			.build();

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPES.get(state.getValue(FACING));
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}
}
