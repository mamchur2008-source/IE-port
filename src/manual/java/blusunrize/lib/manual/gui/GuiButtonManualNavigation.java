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
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class GuiButtonManualNavigation extends Button
{
	public int type;
	public ManualScreen gui;

	public GuiButtonManualNavigation(ManualScreen gui, int x, int y, int w, int h, int type, OnPress handler)
	{
		super(
                x, y,
                type >= 4?10: Math.min(type < 2?16: 10, w), type >= 4?10: Math.min(type < 2?10: 16, h),
                Component.empty(), handler, DEFAULT_NARRATION
                );
		this.gui = gui;
		this.type = type;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mx, int my, float partial)
	{
		isHovered = mx >= this.getX()&&mx < (this.getX()+this.width)&&my >= this.getY()&&my < (this.getY()+this.height);
		int u = type==5?46: type==4||type==6?36: (type < 2?0: type < 3?16: 26)+(type > 1?(10-width): type==1?(16-width): 0);
		int v = 216+(type==0?0: type==1?10: type==2?(16-height): type==3?0: type==4||type==5?10: 0);
		if(isHovered)
			v += 20;
		graphics.blit(RenderPipelines.GUI_TEXTURED, gui.texture, this.getX(), this.getY(), u, v, width, height, 256, 256);
	}
}
