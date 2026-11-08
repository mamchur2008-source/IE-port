/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

public class WhiteTexture implements AutoCloseable
{
	//TODO does this work across resource reloads?
	public static final Supplier<WhiteTexture> INSTANCE = Suppliers.memoize(WhiteTexture::new);

	private final DynamicTexture whiteTexture;
	private final Identifier whiteTextureLocation;

	private WhiteTexture()
	{
		this.whiteTexture = new DynamicTexture("ie_light_map", 16, 16, false);
		this.whiteTextureLocation = ImmersiveEngineering.rl("ie_light_map");
		Minecraft.getInstance().getTextureManager().register(this.whiteTextureLocation, this.whiteTexture);
		NativeImage lightPixels = Objects.requireNonNull(this.whiteTexture.getPixels());

		for(int i = 0; i < 16; ++i)
		{
			for(int j = 0; j < 16; ++j)
			{
				lightPixels.setPixel(j, i, -1);
			}
		}

		this.whiteTexture.upload();
	}

	public void bind()
	{
		RenderSystem.setShaderTexture(2, this.whiteTextureLocation);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	}

	public Identifier getTextureLocation()
	{
		return whiteTextureLocation;
	}

	public void close() throws Exception
	{
		whiteTexture.close();
	}
}
