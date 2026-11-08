/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class GuiButtonIE extends Button
{
	protected final ButtonTexture texture;

	public GuiButtonIE(int x, int y, int w, int h, Component name, ButtonTexture texture, IIEPressable handler)
	{
		super(x, y, w, h, name, handler, DEFAULT_NARRATION);
		this.texture = texture;
	}

	private boolean isPressable(double mouseX, double mouseY)
	{
		return this.active&&this.visible&&mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
	}

	public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		Font fontrenderer = mc.font;
		this.isHovered = isPressable(mouseX, mouseY);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture.get(this.isHovered), getX(), getY(), width, height);
		if(!getMessage().getString().isEmpty())
		{
			int txtCol = 0xE0E0E0;
			if(!this.active)
				txtCol = 0xA0A0A0;
			else if(this.isHovered)
				txtCol = Lib.COLOUR_I_ImmersiveOrange;
		graphics.centeredText(fontrenderer, getMessage(), this.getX()+this.width/2, this.getY()+(this.height-8)/2, txtCol);
		}
	}

	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		renderWidget(graphics, mouseX, mouseY, partialTicks);
	}

	public void onPress()
	{
		this.onPress.onPress(this);
	}

	public interface IIEPressable<B extends Button> extends OnPress
	{
		void onIEPress(B var1);

		default void onPress(Button var1)
		{
			this.onIEPress((B)var1);
		}
	}

	public record ButtonTexture(Identifier texture, Identifier hovered)
	{
		public ButtonTexture
		{
			Preconditions.checkArgument(texture!=null);
			Preconditions.checkArgument(hovered!=null);
		}

		public ButtonTexture(Identifier texture)
		{
			this(texture, texture);
		}

		public Identifier get(boolean hovered)
		{
			return hovered?this.hovered: texture;
		}
	}
}
