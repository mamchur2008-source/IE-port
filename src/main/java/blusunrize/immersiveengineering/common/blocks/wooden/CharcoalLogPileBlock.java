/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.Supplier;

public class CharcoalLogPileBlock extends IEBaseBlock
{
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

	public static final Supplier<BlockBehaviour.Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.COLOR_BLACK)
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(1.5F, 5)
			.noOcclusion();

	public CharcoalLogPileBlock(BlockBehaviour.Properties props)
	{
		super(props);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params)
	{
		Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
		int charcoalAmount = origin!=null?
				WoodPileBlock.getCharcoalDropAmount(params.getLevel(), BlockPos.containing(origin)):
				WoodPileBlock.CHARCOAL_AMOUNT;
		return List.of(new ItemStack(Items.CHARCOAL, charcoalAmount));
	}

	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 0;
	}

	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 0;
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
