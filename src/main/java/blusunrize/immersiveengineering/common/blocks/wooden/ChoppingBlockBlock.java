/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class ChoppingBlockBlock extends IEEntityBlock<ChoppingBlockBlockEntity>
{
	public static final BooleanProperty HAS_LOG = BooleanProperty.create("has_log");
	public static final int BASE_HEIGHT = 6;
	public static final int LOG_MIN = 4;
	public static final int LOG_MAX = 12;
	public static final float LOG_RENDER_SCALE = (LOG_MAX-LOG_MIN)/16F;
	public static final float LOG_RENDER_ITEM_SCALE = LOG_RENDER_SCALE*2;
	public static final float LOG_RENDER_Y = (BASE_HEIGHT+(LOG_MAX-LOG_MIN)/2F)/16F;
	public static final float LOG_TOP_Y = (BASE_HEIGHT+LOG_MAX-LOG_MIN)/16F;
	public static final Supplier<BlockBehaviour.Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(1.5F, 5)
			.noOcclusion();

	private static final VoxelShape EMPTY_SHAPE = Block.box(0, 0, 0, 16, BASE_HEIGHT, 16);
	private static final VoxelShape LOADED_SHAPE = Shapes.or(
			EMPTY_SHAPE, Block.box(LOG_MIN, BASE_HEIGHT, LOG_MIN, LOG_MAX, BASE_HEIGHT+LOG_MAX-LOG_MIN, LOG_MAX)
	);

	public ChoppingBlockBlock(BlockBehaviour.Properties props)
	{
		super(IEBlockEntities.CHOPPING_BLOCK, props);
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		return super.getInitDefaultState().setValue(HAS_LOG, false);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(HAS_LOG);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return state.getValue(HAS_LOG)?LOADED_SHAPE: EMPTY_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return state.getValue(HAS_LOG)?LOADED_SHAPE: EMPTY_SHAPE;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}
}
