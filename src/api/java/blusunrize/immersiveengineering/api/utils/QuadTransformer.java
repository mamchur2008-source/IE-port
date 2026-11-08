/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class QuadTransformer
{
	public static QuadTransform color(Int2IntFunction colorTransform)
	{
		return quad -> {
		};
	}

	@FunctionalInterface
	public interface QuadTransform
	{
		void process(BakedQuad quad);
	}

	private static int modifyColor(int oldColor, int offsetBits, int packedMultiplier)
	{
		final int mask = 255<<offsetBits;
		final int oldSubColor = mask&oldColor;
		final float subMultiplier = ((packedMultiplier>>offsetBits)&255)/255f;
		final int newSubColor = ((int)(oldSubColor*subMultiplier))&mask;
		return (oldColor&~mask)|newSubColor;
	}
}
