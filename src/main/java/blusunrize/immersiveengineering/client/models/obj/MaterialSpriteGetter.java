/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MaterialSpriteGetter<T> implements BiFunction<String, Material, TextureAtlasSprite>
{
	private final Function<Material, TextureAtlasSprite> getter;
	private final String groupName;
	private final IEOBJCallback<T> callback;
	private final T key;
	private final ShaderCase shaderCase;

	private int renderPass = 0;

	public MaterialSpriteGetter(Function<Material, TextureAtlasSprite> getter, String groupName,
								IEOBJCallback<T> callback, T key, ShaderCase shaderCase)
	{
		this.getter = getter;
		this.groupName = groupName;
		this.callback = callback;
		this.key = key;
		this.shaderCase = shaderCase;
	}

	/**
	 * Set the renderpass for use by the shader case
	 *
	 * @param pass
	 */
	public void setRenderPass(int pass)
	{
		this.renderPass = pass;
	}

	public TextureAtlasSprite apply(String material, Material Identifier)
	{
		TextureAtlasSprite sprite = null;
		if(callback!=null)
			sprite = callback.getTextureReplacement(key, groupName, material);
		if(shaderCase!=null)
		{
			Identifier rl = shaderCase.getTextureReplacement(groupName, renderPass);
			if(rl!=null)
				sprite = getter.apply(new Material(rl));
		}
		if(sprite==null)
			sprite = getter.apply(Identifier);
		return sprite;
	}
}
