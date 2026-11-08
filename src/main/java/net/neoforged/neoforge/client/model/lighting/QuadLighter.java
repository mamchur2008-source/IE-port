package net.neoforged.neoforge.client.model.lighting;

public class QuadLighter
{
	public static float calculateShade(float x, float y, float z, boolean shade)
	{
		return shade?0.8f: 1;
	}
}
