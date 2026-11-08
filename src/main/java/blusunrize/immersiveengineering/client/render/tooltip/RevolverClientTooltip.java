/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.render.tooltip;

import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import blusunrize.immersiveengineering.client.utils.GuiGraphicsPose;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record RevolverClientTooltip(RevolverServerTooltip data) implements ClientTooltipComponent
{
	public int getHeight(Font font)
	{
		return 40;
	}

	public int getWidth(Font pFont)
	{
		return 40;
	}

	public void renderImage(Font font, int mouseX, int mouseY, GuiGraphicsExtractor graphics)
	{
		Object pose = GuiGraphicsPose.pose(graphics);
		GuiGraphicsPose.push(pose);
		GuiGraphicsPose.translate(pose, mouseX, mouseY, 0);
		GuiGraphicsPose.scale(pose, .5f, .5f, 1);
		RevolverScreen.drawExternalGUI(data.bullets(), data.bulletCount(), graphics);
		GuiGraphicsPose.pop(pose);
	}
}
