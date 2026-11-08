/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.metal.FloodlightBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.util.SpawnInterdictionHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class FakeLightBlock extends IEEntityBlock<FakeLightBlockEntity>
{
	public static final Supplier<Properties> PROPERTIES = () -> Properties.of()
			.replaceable()
			.air()
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
			.lightLevel(b -> 15);

	public FakeLightBlock(Properties props)
	{
		super(IEBlockEntities.FAKE_LIGHT, props);
	}

	public boolean isAir(BlockState state)
	{
		return true;
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	public boolean isPathfindable(BlockState state, PathComputationType type)
	{
		return true;
	}

	public static class FakeLightBlockEntity extends IEBaseBlockEntity implements ISpawnInterdiction
	{
		public BlockPos floodlightCoords = null;
		public boolean firstTick = true;

		public FakeLightBlockEntity(BlockPos pos, BlockState state)
		{
			super(IEBlockEntities.FAKE_LIGHT.get(), pos, state);
		}

		public double getInterdictionRangeSquared()
		{
			return 1024;
		}

		public void setRemovedIE()
		{
			SpawnInterdictionHandler.removeFromInterdictionTiles(this);
			super.setRemovedIE();
		}

		public void onChunkUnloaded()
		{
			SpawnInterdictionHandler.removeFromInterdictionTiles(this);
			super.onChunkUnloaded();
		}

		public void onLoad()
		{
			super.onLoad();
			if(!firstTick&&(floodlightCoords==null||!(Utils.getExistingTileEntity(level, floodlightCoords) instanceof FloodlightBlockEntity floodlight)||!floodlight.getIsActive()))
				level.removeBlock(getBlockPos(), false);
			else
				SpawnInterdictionHandler.addInterdictionTile(this);
		}

		public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
		{
			if(nbt.contains("floodlightCoords"))
				floodlightCoords = BlockPos.of(nbt.getLongOr("floodlightCoords", BlockPos.ZERO.asLong()));
			else
				floodlightCoords = null;
		}

		public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
		{
			if(floodlightCoords!=null)
				nbt.putLong("floodlightCoords", floodlightCoords.asLong());
		}

		public void setFloodlightCoords(BlockPos pos)
		{
			floodlightCoords = pos;
			firstTick = false;
		}
	}
}
