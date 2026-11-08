package net.neoforged.neoforge.client.model;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public interface IQuadTransformer
{
	BakedQuad process(BakedQuad quad);
}
