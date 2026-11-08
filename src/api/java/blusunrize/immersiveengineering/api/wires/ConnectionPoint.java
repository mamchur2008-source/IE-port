/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires;

import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;

public record ConnectionPoint(@Nonnull BlockPos position, int index) implements Comparable<ConnectionPoint>
{
	public static final DualCodec<ByteBuf, ConnectionPoint> CODECS = DualCompositeCodecs.composite(
			DualCodecs.BLOCK_POS.fieldOf("position"), ConnectionPoint::position,
			DualCodecs.INT.fieldOf("index"), ConnectionPoint::index,
			ConnectionPoint::new
	);

	public ConnectionPoint(CompoundTag nbt)
	{
		this(readPosition(nbt), nbt.getIntOr("index", 0));
	}

	public CompoundTag createTag()
	{
		CompoundTag ret = new CompoundTag();
		CompoundTag posTag = new CompoundTag();
		posTag.putInt("x", position.getX());
		posTag.putInt("y", position.getY());
		posTag.putInt("z", position.getZ());
		ret.put("position", posTag);
		ret.putInt("index", index);
		return ret;
	}

	private static BlockPos readPosition(CompoundTag nbt)
	{
		CompoundTag pos = nbt.getCompoundOrEmpty("position");
		return new BlockPos(pos.getIntOr("x", 0), pos.getIntOr("y", 0), pos.getIntOr("z", 0));
	}

	@Override
	public int compareTo(ConnectionPoint o)
	{
		int blockCmp = position.compareTo(o.position);
		if(blockCmp!=0)
			return blockCmp;
		return Integer.compare(index, o.index);
	}

	public int getX()
	{
		return position.getX();
	}

	public int getY()
	{
		return position.getY();
	}

	public int getZ()
	{
		return position.getZ();
	}
}
