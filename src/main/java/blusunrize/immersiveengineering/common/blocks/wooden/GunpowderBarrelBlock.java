/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.entities.GunpowderBarrelEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GunpowderBarrelBlock extends TntBlock
{
	public static final Supplier<Properties> PROPERTIES = () -> Block.Properties.of()
			.mapColor(MapColor.WOOD)
			.ignitedByLava()
			.instrument(NoteBlockInstrument.BASS)
			.sound(SoundType.WOOD)
			.strength(2, 0);

	public GunpowderBarrelBlock(BlockBehaviour.Properties props)
	{
		super(props);
	}

	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 100;
	}

	public boolean onCaughtFire(BlockState state, Level world, BlockPos pos, @org.jetbrains.annotations.Nullable Direction face, @org.jetbrains.annotations.Nullable LivingEntity igniter)
	{
		if(world instanceof ServerLevel)
		{
			GunpowderBarrelEntity explosive = spawnExplosive(world, pos, state, igniter);
			world.playSound(null, explosive.getX(), explosive.getY(), explosive.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
			world.removeBlock(pos, false);
		}
		return true;
	}

	public void wasExploded(ServerLevel world, BlockPos pos, Explosion explosion)
	{
		super.wasExploded(world, pos, explosion);
	}

	private GunpowderBarrelEntity spawnExplosive(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity igniter)
	{
		GunpowderBarrelEntity explosive = new GunpowderBarrelEntity(world, pos, igniter, state, 8);
		world.addFreshEntity(explosive);
		return explosive;
	}
}
