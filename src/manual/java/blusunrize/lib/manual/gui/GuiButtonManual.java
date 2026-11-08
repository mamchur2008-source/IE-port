/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiButtonManual extends Button
{
	public ManualScreen gui;
	public int[] colour = {0x33000000, 0x33cb7f32};
	public int[] textColour = {0xffe0e0e0, 0xffffffa0};

	public GuiButtonManual(ManualScreen gui, int x, int y, int w, int h, Component text, OnPress handler)
	{
		super(x, y, w, h, text, handler, DEFAULT_NARRATION);
		this.gui = gui;
	}

	public GuiButtonManual setColour(int normal, int hovered)
	{
		colour = new int[]{normal, hovered};
		return this;
	}

	public GuiButtonManual setTextColour(int normal, int hovered)
	{
		textColour = new int[]{normal, hovered};
		return this;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mx, int my, float partialTicks)
	{
		if(this.visible)
		{
			ManualUtils.bindTexture(gui.texture);
			this.isHovered = mx >= this.getX()&&mx < (this.getX()+this.width)&&my >= this.getY()&&my < (this.getY()+this.height);

			int col = colour[isHovered?1: 0];
			graphics.fill(getX(), getY(), getX()+width, getY()+height, col);
			int txtCol = textColour[isHovered?1: 0];
			int sw = gui.manual.fontRenderer().width(getMessage().getString());
			graphics.text(
					gui.manual.fontRenderer(), getMessage().getString(), getX()+width/2-sw/2, getY()+height/2-gui.manual.fontRenderer().lineHeight/2, txtCol
			);
		}
	}
}
