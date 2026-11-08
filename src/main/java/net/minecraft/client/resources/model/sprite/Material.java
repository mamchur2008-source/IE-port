package net.minecraft.client.resources.model.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public record Material(Identifier sprite, boolean forceTranslucent)
{
	public Material(Identifier sprite)
	{
		this(sprite, false);
	}

	public Material withForceTranslucent(boolean forceTranslucent)
	{
		return new Material(this.sprite, forceTranslucent);
	}

	public record Baked(TextureAtlasSprite sprite, boolean forceTranslucent)
	{
	}
}
