package com.mojang.blaze3d.platform;

public class InputConstants
{
	public enum Type
	{
		KEYSYM,
		MOUSE
	}

	public static class Key
	{
	}

	public static Key getKey(int keyCode, int scanCode)
	{
		return new Key();
	}
}
