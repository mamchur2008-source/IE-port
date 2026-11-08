/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.common.gui.DieselGeneratorMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class DieselGeneratorScreen extends IEContainerScreen<DieselGeneratorMenu>
{
	private static final Identifier BACKGROUND = makeTextureLocation("diesel_generator");
	private static final Identifier TANK = ieLoc("squeezer/tank_overlay");

	public DieselGeneratorScreen(DieselGeneratorMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, BACKGROUND);
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+80, topPos+20, 16, 47), 20, 51, TANK),
				new TooltipArea(
						new Rect2i(leftPos+117, topPos+36, 10, 10),
						() -> Component.translatable(Lib.GUI_CONFIG+"dieselGenerator."+(menu.active?"active": "idle"))
				)
		);
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xff1d2326);
		graphics.fill(leftPos+1, topPos+1, leftPos+imageWidth-1, topPos+imageHeight-1, 0xff2b3235);
		graphics.fill(leftPos+7, topPos+16, leftPos+169, topPos+75, 0xff22282b);
		graphics.fill(leftPos+7, topPos+83, leftPos+169, topPos+160, 0xff22282b);
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+1, Lib.COLOUR_I_ImmersiveOrange);
		graphics.fill(leftPos+75, topPos+15, leftPos+101, topPos+72, 0xff111719);
		graphics.fill(leftPos+76, topPos+16, leftPos+100, topPos+71, 0xff384248);
		graphics.fill(leftPos+114, topPos+33, leftPos+130, topPos+49, 0xff111719);
		graphics.fill(leftPos+117, topPos+36, leftPos+127, topPos+46, menu.active?0xff2fa363: 0xff8a4b2b);
	}
}
