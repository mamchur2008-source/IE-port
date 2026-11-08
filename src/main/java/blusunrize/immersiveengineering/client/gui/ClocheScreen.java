/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FertilizerInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.ClocheMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class ClocheScreen extends IEContainerScreen<ClocheMenu>
{
	private static final Identifier TEXTURE = makeTextureLocation("cloche");
	private static final Identifier TANK_OVERLAY = ieLoc("cloche/tank_overlay");
	private static final Identifier PROGRESS = ieLoc("cloche/progress");

	public ClocheScreen(ClocheMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+8, topPos+8, 16, 47), 20, 51, TANK_OVERLAY),
				new EnergyInfoArea(leftPos+158, topPos+22, menu.energyStorage),
				new FertilizerInfoArea(leftPos+30, topPos+22, menu.fertilizerAmount, menu.fertilizerMod)
		);
	}

	protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float f, int mx, int my)
	{
		float process = menu.guiProgress.get();
		if(process > 0)
		{
			int w = (int)Math.max(1, process*12);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS, 12, 12, 0, 0, leftPos+101, topPos+36, w, 12);
		}
	}
}
