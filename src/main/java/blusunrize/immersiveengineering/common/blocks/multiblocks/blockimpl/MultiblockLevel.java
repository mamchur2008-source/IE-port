/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public record MultiblockLevel(
		Supplier<Level> getLevel, MultiblockOrientation orientation, Supplier<BlockPos> origin
) implements IMultiblockLevel
{
	public MultiblockLevel(Supplier<Level> getLevel, MultiblockOrientation orientation, BlockPos origin)
	{
		this(getLevel, orientation, () -> origin);
	}

	public BlockState getBlockState(BlockPos relativePosition)
	{
		return SafeChunkUtils.getBlockState(level(), toAbsolute(relativePosition));
	}

	public void setBlock(BlockPos relativePosition, BlockState state)
	{
		level().setBlock(toAbsolute(relativePosition), state, Block.UPDATE_ALL);
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos relativePosition)
	{
		return SafeChunkUtils.getSafeBE(level(), toAbsolute(relativePosition));
	}

	@Nullable
	public BlockEntity forciblyGetBlockEntity(BlockPos relativePosition)
	{
		return level().getBlockEntity(toAbsolute(relativePosition));
	}

	public boolean shouldTickModulo(int interval)
	{
		final int posRandom = 0x7f_ff_ff_ff&origin.hashCode();
		return posRandom%interval==level().getGameTime()%interval;
	}

	public BlockPos getAbsoluteOrigin()
	{
		return origin.get();
	}

	public MultiblockOrientation getOrientation()
	{
		return orientation;
	}

	public BlockPos toAbsolute(BlockPos relative)
	{
		return getAbsoluteOrigin().offset(orientation.getAbsoluteOffset(relative));
	}

	public @Nullable Direction toAbsolute(@Nullable RelativeBlockFace relative)
	{
		if(relative!=null)
			return relative.forFront(orientation);
		else
			return null;
	}

	public AABB toAbsolute(AABB relative)
	{
		final Vec3 minPos = new Vec3(relative.minX, relative.minY, relative.minZ);
		final Vec3 maxPos = new Vec3(relative.maxX, relative.maxY, relative.maxZ);
		return new AABB(toAbsolute(minPos), toAbsolute(maxPos));
	}

	public Vec3 toAbsolute(Vec3 relative)
	{
		return Vec3.atLowerCornerOf(getAbsoluteOrigin()).add(orientation.getAbsoluteOffset(relative));
	}

	public BlockPos toRelative(BlockPos absolute)
	{
		final BlockPos absoluteOffset = absolute.subtract(getAbsoluteOrigin());
		return orientation.getPosInMB(absoluteOffset);
	}

	public RelativeBlockFace toRelative(Direction absolute)
	{
		return RelativeBlockFace.from(orientation, absolute);
	}

	public boolean isThundering()
	{
		return level().isThundering();
	}

	public boolean isRaining()
	{
		return level().isRaining();
	}

	public int getMaxBuildHeight()
	{
		return level().getMaxY();
	}

	public Level getRawLevel()
	{
		return level();
	}

	public void updateNeighbourForOutputSignal(BlockPos posInMultiblock)
	{
		final BlockPos absolutePos = toAbsolute(posInMultiblock);
		if(!SafeChunkUtils.isChunkSafe(level(), absolutePos))
			return;
		final BlockState stateAt = level().getBlockState(absolutePos);
		level().updateNeighbourForOutputSignal(absolutePos, stateAt.getBlock());
	}

	private Level level()
	{
		return Objects.requireNonNull(getLevel.get());
	}
}
