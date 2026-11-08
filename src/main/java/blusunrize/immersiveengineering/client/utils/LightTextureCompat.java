/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

public final class LightTextureCompat
{
	public static final int FULL_BRIGHT = pack(15, 15);

	private LightTextureCompat()
	{
	}

	public static int pack(int block, int sky)
	{
		return block<<4|sky<<20;
	}

	public static int block(int packedLight)
	{
		return packedLight>>4&15;
	}

	public static int sky(int packedLight)
	{
		return packedLight>>20&15;
	}
}
