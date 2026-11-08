package net.neoforged.neoforge.client.model;

import com.mojang.math.Transformation;

public class QuadTransformers
{
	public static IQuadTransformer applying(Transformation transformation)
	{
		return quad -> quad;
	}
}
