/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

public class IEGLShaders
{
	private static Object blockFullbrightShader;
	private static Object vboShader;
	private static Object pointShader;

	public static Object getBlockFullbrightShader()
	{
		return blockFullbrightShader;
	}

	public static Object getVboShader()
	{
		return vboShader;
	}

	public static Object getPointShader()
	{
		return pointShader;
	}
}
