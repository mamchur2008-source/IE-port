/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.gui.CokeOvenMenu;
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

public class CokeOvenScreen extends IEContainerScreen<CokeOvenMenu>
{
	private static final Identifier TEXTURE = makeTextureLocation("coke_oven");
	private static final Identifier TANK = ieLoc("coke_oven/tank_overlay");
	private static final Identifier FLAME = ieLoc("coke_oven/flame");

	public CokeOvenScreen(CokeOvenMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+129, topPos+20, 16, 47), 20, 51, TANK)
		);
	}

	protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float f, int mx, int my)
	{
		int processMax = menu.data.get(CokeOvenLogic.State.MAX_BURN_TIME);
		int process = menu.data.get(CokeOvenLogic.State.BURN_TIME);
		if(processMax > 0&&process > 0)
		{
			int h = (int)(12*(process/(float)processMax));
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FLAME, 9, 12, 0, 12-h, leftPos+59, topPos+37+12-h, 9, h);
		}
	}
}
