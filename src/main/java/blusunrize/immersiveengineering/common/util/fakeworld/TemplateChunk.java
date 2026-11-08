/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TemplateChunk extends LevelChunk
{
	private final Predicate<BlockPos> shouldShow;
	private final Holder<Biome> biome;

	public TemplateChunk(Level worldIn, ChunkPos chunkPos, List<StructureBlockInfo> blocksInChunk, Predicate<BlockPos> shouldShow)
	{
		super(worldIn, chunkPos);
		worldIn.registryAccess().lookupOrThrow(Registries.BIOME);
		for(int i = 0; i < getSections().length; ++i)
			getSections()[i] = new TemplateChunkSection(i, worldIn.registryAccess(), shouldShow, chunkPos);
		this.shouldShow = shouldShow;
		this.biome = worldIn.getUncachedNoiseBiome(0, 0, 0);
		for(StructureBlockInfo info : blocksInChunk)
		{
			actuallSetBlockState(info.pos(), info.state());
			if(info.nbt()!=null)
			{
				BlockEntity tile = BlockEntity.loadStatic(
						info.pos(), info.state(), info.nbt(), worldIn.registryAccess()
				);
				if(tile!=null)
				{
					tile.setLevel(worldIn);
					getBlockEntities().put(info.pos(), tile);
				}
			}
		}
	}

	@Nonnull
	public FluidState getFluidState(@Nonnull BlockPos pos)
	{
		return getBlockState(pos).getFluidState();
	}


	@Nullable
	public BlockEntity getBlockEntity(@Nonnull BlockPos pos, @Nonnull EntityCreationType creationMode)
	{
		if(!shouldShow.test(pos))
			return null;
		return getBlockEntities().get(pos);
	}

	@Nullable
	public BlockState setBlockState(@Nonnull BlockPos pos, @Nonnull BlockState state, boolean isMoving)
	{
		return null;
	}

	public void actuallSetBlockState(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		final int sectionIndex = getSectionIndex(pos.getY());
		final int sectionX = pos.getX()&15;
		final int sectionY = pos.getY()&15;
		final int sectionZ = pos.getZ()&15;
		final TemplateChunkSection section = (TemplateChunkSection)getSection(sectionIndex);
		section.actuallySetBlockState(sectionX, sectionY, sectionZ, state);
	}

	public int getLightEmission(@Nonnull BlockPos pos)
	{
		return 0;
	}

	public void addAndRegisterBlockEntity(@Nonnull BlockEntity blockEntity)
	{
	}

	public void setBlockEntity(@Nonnull BlockEntity blockEntity)
	{
	}

	public void removeBlockEntity(@Nonnull BlockPos pos)
	{
	}

	// Not always correct, but hopefully "good enough"
	public boolean isEmpty()
	{
		return false;
	}

	public boolean isYSpaceEmpty(int startY, int endY)
	{
		return false;
	}

	@Nonnull
	public FullChunkStatus getFullStatus()
	{
		return FullChunkStatus.INACCESSIBLE;
	}

	@Nonnull
	public Holder<Biome> getNoiseBiome(int p_204426_, int p_204427_, int p_204428_)
	{
		return this.biome;
	}
}
