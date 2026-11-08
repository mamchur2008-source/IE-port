/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.info;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import java.util.List;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class FluidInfoArea extends InfoArea
{
	private final IFluidTank tank;
	private final Rect2i area;
	private final int overlayWidth;
	private final int overlayHeight;
	private final Identifier overlayTexture;

	public FluidInfoArea(IFluidTank tank, Rect2i area, int overlayWidth, int overlayHeight, Identifier overlayTexture)
	{
		super(area);
		this.tank = tank;
		this.area = area;
		this.overlayWidth = overlayWidth;
		this.overlayHeight = overlayHeight;
		this.overlayTexture = overlayTexture;
	}


	public void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip)
	{
		fillTooltip(tank.getFluid(), tank.getCapacity(), tooltip::add);
	}

	public static void fillTooltip(FluidStack fluid, int tankCapacity, Consumer<Component> tooltip)
	{

		if(!fluid.isEmpty())
			tooltip.accept(applyFormat(
					fluid.getHoverName(),
					fluid.getFluid().getFluidType().getRarity(fluid).color()
			));
		else
			tooltip.accept(Component.translatable("gui.immersiveengineering.empty"));
		if(fluid.getFluid() instanceof PotionFluid potion)
			potion.addInformation(fluid, tooltip);

		if(mc().options.advancedItemTooltips&&!fluid.isEmpty())
		{
			if(!net.minecraft.client.Minecraft.getInstance().options.keyShift.isDown())
				tooltip.accept(Component.translatable(Lib.DESC_INFO+"holdShiftForInfo"));
			else
			{
				//TODO translation keys
				tooltip.accept(applyFormat(Component.literal("Fluid Registry: "+BuiltInRegistries.FLUID.getKey(fluid.getFluid())), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Density: "+fluid.getFluid().getFluidType().getDensity(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Temperature: "+fluid.getFluid().getFluidType().getTemperature(fluid)), ChatFormatting.DARK_GRAY));
				tooltip.accept(applyFormat(Component.literal("Viscosity: "+fluid.getFluid().getFluidType().getViscosity(fluid)), ChatFormatting.DARK_GRAY));
			}
		}

		if(tankCapacity > 0)
			tooltip.accept(applyFormat(Component.literal(fluid.getAmount()+"/"+tankCapacity+"mB"), ChatFormatting.GRAY));
		else if(tankCapacity==0)
			tooltip.accept(applyFormat(Component.literal(fluid.getAmount()+"mB"), ChatFormatting.GRAY));
		//don't display amount for tankCapacity < 0, i.e. for ghost fluid stacks
	}

	public void draw(GuiGraphicsExtractor graphics)
	{
		FluidStack fluid = tank.getFluid();
		float capacity = tank.getCapacity();
		if(!fluid.isEmpty())
		{
			int fluidHeight = (int)(area.getHeight()*(fluid.getAmount()/capacity));
			GuiHelper.drawFluidSpriteGui(
					graphics, fluid,
					area.getX(), area.getY()+area.getHeight()-fluidHeight,
					area.getWidth(), fluidHeight
			);
		}
		int xOff = (area.getWidth()-overlayWidth)/2;
		int yOff = (area.getHeight()-overlayHeight)/2;
		graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED, overlayTexture,
				area.getX()+xOff, area.getY()+yOff,
				overlayWidth, overlayHeight
		);
	}
}
