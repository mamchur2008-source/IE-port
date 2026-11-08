/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyInfoArea extends InfoArea
{
	private final IEnergyStorage energy;

	public EnergyInfoArea(int xMin, int yMin, IEnergyStorage energy)
	{
		super(new Rect2i(xMin, yMin, 7, 46));
		this.energy = energy;
	}

	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		tooltip.add(Component.literal(energy.getEnergyStored()+"/"+energy.getMaxEnergyStored()+" IF"));
	}

	public void draw(GuiGraphicsExtractor graphics)
	{
		final int height = area.getHeight();
		int capacity = energy.getMaxEnergyStored();
		int stored = capacity > 0?(int)(height*(energy.getEnergyStored()/(float)capacity)): 0;
		graphics.fillGradient(
				area.getX(), area.getY()+(height-stored),
				area.getX()+area.getWidth(), area.getY()+area.getHeight(),
				0xffb51500, 0xff600b00
		);
	}
}
