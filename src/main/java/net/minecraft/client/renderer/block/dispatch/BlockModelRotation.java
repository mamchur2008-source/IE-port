package net.minecraft.client.renderer.block.dispatch;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;

public class BlockModelRotation implements ModelState
{
	public static final BlockModelRotation IDENTITY = new BlockModelRotation();
	public static final BlockModelRotation X0_Y0 = IDENTITY;
	public static final BlockModelRotation X0_Y90 = IDENTITY;
	public static final BlockModelRotation X0_Y180 = IDENTITY;
	public static final BlockModelRotation X0_Y270 = IDENTITY;
	public static final BlockModelRotation X90_Y0 = IDENTITY;
	public static final BlockModelRotation X270_Y0 = IDENTITY;

	public static BlockModelRotation by(int x, int y)
	{
		return IDENTITY;
	}

	public static BlockModelRotation get(OctahedralGroup group)
	{
		return IDENTITY;
	}

	public Transformation transformation()
	{
		return Transformation.IDENTITY;
	}
}
