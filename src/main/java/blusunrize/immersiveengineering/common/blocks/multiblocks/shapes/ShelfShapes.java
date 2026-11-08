/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class ShelfShapes implements Function<BlockPos, VoxelShape>
{
	public static final Function<BlockPos, VoxelShape> SHAPE_GETTER = new ShelfShapes();

	public VoxelShape apply(BlockPos posInMultiblock)
	{
		VoxelShape floor = posInMultiblock.getY()==0?Shapes.empty(): Shapes.box(
				posInMultiblock.getX()==0?.0625:0, -.1875, posInMultiblock.getZ()==0?.0625:0,
				posInMultiblock.getX()==3?.9375:1, -.0625, posInMultiblock.getZ()==1?.9375:1
		);
		double legY = posInMultiblock.getY()==0?0: -.0625;
		VoxelShape leg = posInMultiblock.getY()==3?Shapes.empty():
				posInMultiblock.getX()==0?Shapes.box(
						0, legY, posInMultiblock.getZ()==0?0: .75,
						.25, legY+1, posInMultiblock.getZ()==0?.25: 1
				): posInMultiblock.getX()==3?Shapes.box(
						.75, legY, posInMultiblock.getZ()==0?0: .75,
						1, legY+1, posInMultiblock.getZ()==0?.25: 1
				): Shapes.empty();
		double[] crateX = {.3125, .1875, .0625, -.0625};
		VoxelShape crate = posInMultiblock.getY()==0?Shapes.empty(): Shapes.box(
				crateX[posInMultiblock.getX()], -.0625, posInMultiblock.getZ()==0?.25: 0,
				crateX[posInMultiblock.getX()]+.75, .6875, posInMultiblock.getZ()==0?1: .75
		);
		return Shapes.or(floor, leg, crate);
	}
}
