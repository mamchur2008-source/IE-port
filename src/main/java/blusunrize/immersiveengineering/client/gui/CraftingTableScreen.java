/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.CraftingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingTableScreen extends IEContainerScreen<CraftingTableMenu>
{
	public CraftingTableScreen(CraftingTableMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("craftingtable"), 176, 210);
		this.inventoryLabelY = this.imageHeight-91;
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		super.renderLabels(graphics, mouseX, mouseY);
		graphics.text(this.font, title, 8, 6, Lib.COLOUR_I_ImmersiveOrange, true);
	}
}
