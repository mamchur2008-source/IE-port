package com.mojang.blaze3d.systems;

import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class RenderSystem
{
	public static boolean isOnRenderThread()
	{
		return true;
	}

	public static void setShaderTexture(int slot, Identifier texture)
	{
	}

	public static void setShader(Supplier<?> shader)
	{
	}

	public static void enableDepthTest()
	{
	}

	public static void disableDepthTest()
	{
	}

	public static void enableBlend()
	{
	}

	public static void disableBlend()
	{
	}

	public static void defaultBlendFunc()
	{
	}

	public static void blendFunc(int sourceFactor, int destFactor)
	{
	}

	public static void blendFuncSeparate(int sourceFactor, int destFactor, int sourceAlpha, int destAlpha)
	{
	}

	public static void texParameter(int target, int parameterName, int parameter)
	{
	}
}
