/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class WoodPileBlock extends IEBaseBlock
{
	public static final int MAX_AGE = 7;
	public static final int CHARCOAL_AMOUNT = 8;
	public static final int BONUS_CHARCOAL_AMOUNT = 12;
	private static final int TICK_DELAY = 960;
	private static final int RANDOM_TICK_DELAY = 160;
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);
	private static final Direction[] BONUS_DIRECTIONS = {
			Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
	};
	private static final Set<Block> BONUS_LINING_BLOCKS = Set.of(
			Blocks.CLAY,
			Blocks.TERRACOTTA,
			Blocks.WHITE_TERRACOTTA,
			Blocks.ORANGE_TERRACOTTA,
			Blocks.MAGENTA_TERRACOTTA,
			Blocks.LIGHT_BLUE_TERRACOTTA,
			Blocks.YELLOW_TERRACOTTA,
			Blocks.LIME_TERRACOTTA,
			Blocks.PINK_TERRACOTTA,
			Blocks.GRAY_TERRACOTTA,
			Blocks.LIGHT_GRAY_TERRACOTTA,
			Blocks.CYAN_TERRACOTTA,
			Blocks.PURPLE_TERRACOTTA,
			Blocks.BLUE_TERRACOTTA,
			Blocks.BROWN_TERRACOTTA,
			Blocks.GREEN_TERRACOTTA,
			Blocks.RED_TERRACOTTA,
			Blocks.BLACK_TERRACOTTA
	);

	public static final BooleanProperty ACTIVE = IEProperties.ACTIVE;
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
	public static final Supplier<BlockBehaviour.Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(1.5F, 5)
			.lightLevel(state -> state.getValue(ACTIVE)?10: 0)
			.noOcclusion();

	public WoodPileBlock(BlockBehaviour.Properties props)
	{
		super(props);
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		return super.getInitDefaultState()
				.setValue(ACTIVE, false)
				.setValue(AGE, 0);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE, AGE);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston)
	{
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if(level.isClientSide()||oldState.is(this))
			return;
		if(state.getValue(ACTIVE))
			scheduleNextTick(level, pos, level.getRandom(), false);
		else if(hasAdjacentIgnition(level, pos))
			activate(level, pos, state, true);
	}

	@Override
	protected void neighborChanged(
			BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston
	)
	{
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if(!level.isClientSide()&&!state.getValue(ACTIVE)&&hasAdjacentIgnition(level, pos))
			activate(level, pos, state, true);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		if(!state.getValue(ACTIVE))
			return;

		if(!checkSealedAndSpread(level, pos))
		{
			level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
			return;
		}

		if(random.nextBoolean())
		{
			int age = state.getValue(AGE);
			if(age < MAX_AGE)
				level.setBlock(pos, state.setValue(AGE, age+1), 3);
			else
			{
				level.setBlock(pos, IEBlocks.WoodenDevices.CHARCOAL_LOG_PILE.get().defaultBlockState(), 3);
				return;
			}
		}

		BlockState current = level.getBlockState(pos);
		if(current.is(this)&&current.getValue(ACTIVE))
			scheduleNextTick(level, pos, random, false);
	}

	private boolean checkSealedAndSpread(ServerLevel level, BlockPos pos)
	{
		for(Direction direction : Direction.values())
		{
			BlockPos neighborPos = pos.relative(direction);
			BlockState neighbor = level.getBlockState(neighborPos);
			if(neighbor.is(this))
			{
				if(!neighbor.getValue(ACTIVE)&&neighbor.getValue(AGE) < MAX_AGE)
					activate(level, neighborPos, neighbor, true);
				continue;
			}
			if(neighbor.is(IEBlocks.WoodenDevices.CHARCOAL_LOG_PILE.get()))
				continue;
			if(!isSealedBy(level, neighborPos, neighbor, direction.getOpposite()))
				return false;
		}
		return true;
	}

	private boolean isSealedBy(BlockGetter level, BlockPos pos, BlockState state, Direction face)
	{
		return !state.isAir()&&!state.isFlammable(level, pos, face)&&state.isFaceSturdy(level, pos, face);
	}

	private boolean hasAdjacentIgnition(Level level, BlockPos pos)
	{
		for(Direction direction : Direction.values())
		{
			BlockState neighbor = level.getBlockState(pos.relative(direction));
			if(neighbor.is(Blocks.FIRE)||neighbor.is(Blocks.SOUL_FIRE))
				return true;
			if(neighbor.is(this)&&neighbor.getValue(ACTIVE))
				return true;
		}
		return false;
	}

	private void activate(Level level, BlockPos pos, BlockState state, boolean initial)
	{
		if(level.isClientSide()||!state.is(this)||state.getValue(ACTIVE)||state.getValue(AGE) >= MAX_AGE)
			return;
		level.setBlock(pos, state.setValue(ACTIVE, true), 3);
		scheduleNextTick(level, pos, level.getRandom(), initial);
	}

	private void scheduleNextTick(Level level, BlockPos pos, RandomSource random, boolean initial)
	{
		int delay = initial?TICK_DELAY/4: TICK_DELAY;
		int randomDelay = initial?RANDOM_TICK_DELAY/4: RANDOM_TICK_DELAY;
		level.scheduleTick(pos, this, delay+random.nextInt(randomDelay+1));
	}

	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return state.getValue(ACTIVE)||state.getValue(AGE) >= MAX_AGE?0: 20;
	}

	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return state.getValue(ACTIVE)||state.getValue(AGE) >= MAX_AGE?0: 25;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params)
	{
		if(!state.getValue(ACTIVE)&&state.getValue(AGE) >= MAX_AGE)
		{
			Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
			int charcoalAmount = origin!=null?getCharcoalDropAmount(params.getLevel(), BlockPos.containing(origin)): CHARCOAL_AMOUNT;
			return List.of(new ItemStack(Items.CHARCOAL, charcoalAmount));
		}
		return super.getDrops(state, params);
	}

	static int getCharcoalDropAmount(ServerLevel level, BlockPos pos)
	{
		return hasBonusLining(level, pos)?BONUS_CHARCOAL_AMOUNT: CHARCOAL_AMOUNT;
	}

	private static boolean hasBonusLining(ServerLevel level, BlockPos pos)
	{
		for(Direction direction : BONUS_DIRECTIONS)
			if(BONUS_LINING_BLOCKS.contains(level.getBlockState(pos.relative(direction)).getBlock()))
				return true;
		return false;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if(!state.getValue(ACTIVE))
			return;
		if(random.nextInt(24)==0)
			level.playLocalSound(
					pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D,
					SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
					0.35F+random.nextFloat()*0.25F, 0.6F+random.nextFloat()*0.3F, false
			);
		if(random.nextInt(3)==0)
			level.addParticle(
					random.nextBoolean()?ParticleTypes.SMOKE: ParticleTypes.LARGE_SMOKE,
					pos.getX()+0.25D+random.nextDouble()*0.5D,
					pos.getY()+0.8D,
					pos.getZ()+0.25D+random.nextDouble()*0.5D,
					0, 0.035D, 0
			);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return true;
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type)
	{
		return false;
	}
}
