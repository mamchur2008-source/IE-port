/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.FurnaceHandler.StateView;
import blusunrize.immersiveengineering.common.gui.BlastFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

import javax.annotation.Nonnull;

public class BlastFurnaceScreen extends IEContainerScreen<BlastFurnaceMenu>
{
	private static final Identifier TEXTURE = makeTextureLocation("blast_furnace");

	public BlastFurnaceScreen(BlastFurnaceMenu container, Inventory inventoryPlayer, Component title)
	{
		this(container, inventoryPlayer, title, 176);
	}

	protected BlastFurnaceScreen(BlastFurnaceMenu container, Inventory inventoryPlayer, Component title, int imageWidth)
	{
		super(container, inventoryPlayer, title, TEXTURE, imageWidth, 166);
	}

	public static void drawFlameAndArrow(
			ContainerData state, GuiGraphicsExtractor graphics, int leftPos, int topPos, int arrowXOffset
	)
	{
		if(StateView.getLastBurnTime(state) > 0)
		{
			int h = (int)(12*(StateView.getBurnTime(state)/(float)StateView.getLastBurnTime(state)));
			GuiHelper.blit(graphics, TEXTURE, leftPos+56, topPos+37+12-h, 179, 1+12-h, 9, h);
		}
		if(StateView.getMaxProcess(state) > 0)
		{
			int w = (int)(22*(1-StateView.getProcess(state)/(float)StateView.getMaxProcess(state)));
			GuiHelper.blit(graphics, TEXTURE, leftPos+arrowXOffset, topPos+35, 177, 14, w, 16);
		}
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		GuiHelper.blit(graphics, TEXTURE, leftPos, topPos, 0, 0, 176, imageHeight);
	}

	protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float f, int mx, int my)
	{
		drawFlameAndArrow(menu.state, graphics, leftPos, topPos, 76);
	}

	public static class Advanced extends BlastFurnaceScreen
	{
		public Advanced(BlastFurnaceMenu container, Inventory inventoryPlayer, Component title)
		{
			super(container, inventoryPlayer, title, 210);
		}

		protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float f, int mx, int my)
		{
			GuiHelper.blit(graphics, TEXTURE, leftPos+140, topPos+11, 176, 32, 70, 46);
			if(menu.leftHeater.get())
				GuiHelper.blit(graphics, TEXTURE, leftPos+182, topPos+27, 200, 22, 10, 10);
			if(menu.rightHeater.get())
				GuiHelper.blit(graphics, TEXTURE, leftPos+182, topPos+39, 200, 22, 10, 10);
			super.drawContainerBackgroundPre(graphics, f, mx, my);
		}

		protected void renderLabels(GuiGraphicsExtractor graphics, int x, int y)
		{
			String title = I18n.get(Lib.GUI+"blast_furnace.preheaters");
			int w = this.font.width(title)/2;
			graphics.text(font, title, 175-w, 18, 0xAEAEAE);
			graphics.text(font, I18n.get(Lib.GUI+"left"), 154, 28, 0xAEAEAE);
			graphics.text(font, I18n.get(Lib.GUI+"right"), 154, 40, 0xAEAEAE);
		}
	}
}
