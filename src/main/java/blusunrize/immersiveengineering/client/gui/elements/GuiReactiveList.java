/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.utils.GuiGraphicsPose;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class GuiReactiveList<E> extends Button
{
	private static final Identifier SCROLL_TOP = ieLoc("slider_vertical/top");
	private static final Identifier SCROLL_BOTTOM = ieLoc("slider_vertical/bottom");
	private static final Identifier SCROLL_CENTER = ieLoc("slider_vertical/center");
	private static final Identifier SCROLL_BUTTON_TOP = ieLoc("slider_vertical/button_top");
	private static final Identifier SCROLL_BUTTON_BOTTOM = ieLoc("slider_vertical/button_bottom");
	private static final Identifier SCROLL_BUTTON_CENTER = ieLoc("slider_vertical/button_center");

	protected Supplier<List<E>> entries;
	protected Function<E, String> toStringFunction;
	private final int[] padding = {0, 0, 0, 0};
	private boolean needsSlider = false;
	protected int perPage;
	private final float textScale = 1;
	private int textColor = 0xE0E0E0;
	private int textColorHovered = Lib.COLOUR_I_ImmersiveOrange;
	private boolean textShadow = true;

	protected int offset;
	private int maxOffset;

	private int targetEntry = -1;
	private int hoverTimer = 0;

	public GuiReactiveList(int x, int y, int w, int h, IIEPressable<? extends GuiReactiveList> handler, Supplier<List<E>> entries, Function<E, String> toStringFunction)
	{
		super(x, y, w, h, Component.empty(), handler, DEFAULT_NARRATION);
		this.entries = entries;
		this.toStringFunction = toStringFunction;
	}

	public static GuiReactiveList<String> build(int x, int y, int w, int h, IIEPressable<? extends GuiReactiveList<String>> handler, Supplier<List<String>> stringEntries)
	{
		return new GuiReactiveList<>(x, y, w, h, handler, stringEntries, s -> s);
	}

	private void recalculateEntries()
	{
		final int length = this.entries.get().size();
		perPage = (int)((this.height-padding[0]-padding[1])/(ClientUtils.mc().font.lineHeight*textScale));
		if(perPage < length)
		{
			needsSlider = true;
			maxOffset = length-perPage;
			this.offset = Math.min(this.offset, maxOffset);
		}
		else
		{
			needsSlider = false;
			this.maxOffset = this.offset = 0;
		}
	}

	public GuiReactiveList<E> setTextStyling(int textColor, int textColorHovered, boolean textShadow)
	{
		this.textColor = textColor;
		this.textColorHovered = textColorHovered;
		this.textShadow = textShadow;
		return this;
	}

	public GuiReactiveList<E> setPadding(int up, int down, int left, int right)
	{
		this.padding[0] = up;
		this.padding[1] = down;
		this.padding[2] = left;
		this.padding[3] = right;
		return this;
	}

	public int getOffset()
	{
		return this.offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getMaxOffset()
	{
		return this.maxOffset;
	}

	public void renderWidget(GuiGraphicsExtractor graphics, int mx, int my, float partialTicks)
	{
		recalculateEntries();
		final List<E> entries = this.entries.get();
		Font fr = ClientUtils.mc().font;

		int mmY = my-this.getY();
		int strWidth = width-padding[2]-padding[3]-(needsSlider?6: 0);
		if(needsSlider)
		{
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_TOP, getX()+width-6, getY(), 6, 4);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BOTTOM, getX()+width-6, getY()+height-4, 6, 4);
			for(int i = 0; i < height-8; i += 2)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_CENTER, getX()+width-6, getY()+4+i, 6, 2);

			int sliderSize = Math.max(6, height-maxOffset*fr.lineHeight);
			float silderShift = (height-sliderSize)/(float)maxOffset*offset;

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BUTTON_TOP, getX()+width-5, (int)(getY()+silderShift+1), 4, 2);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BUTTON_BOTTOM, getX()+width-5, (int)(getY()+silderShift+sliderSize-4), 4, 3);
			for(int i = 0; i < sliderSize-7; i++)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_BUTTON_CENTER, getX()+width-5, (int)(getY()+silderShift+3+i), 4, 1);
		}

		Object pose = GuiGraphicsPose.pose(graphics);
		GuiGraphicsPose.push(pose);
		GuiGraphicsPose.scale(pose, textScale, textScale, 1);
		this.isHovered = active&&mx >= getX()&&mx < getX()+width&&my >= getY()&&my < getY()+height;
		boolean hasTarget = false;
		for(int i = 0; i < Math.min(perPage, entries.size()); i++)
		{
			int j = offset+i;
			int col = this.textColor;
			boolean selectionHover = isHovered&&mmY >= i*fr.lineHeight&&mmY < (i+1)*fr.lineHeight;
			if(selectionHover)
			{
				hasTarget = true;
				if(targetEntry!=j)
				{
					targetEntry = j;
					hoverTimer = 0;
				}
				else
					hoverTimer++;
				col = this.textColorHovered;
			}
			if(j > entries.size()-1)
				j = entries.size()-1;
			String s = this.toStringFunction.apply(entries.get(j));
			int overLength = s.length()-fr.plainSubstrByWidth(s, strWidth).length();
			if(overLength > 0)//String is too long
			{
				if(selectionHover&&hoverTimer > 20)
				{
					int textOffset = (hoverTimer/10)%(s.length());
					s = s.substring(textOffset)+" "+s.substring(0, textOffset);
				}
				s = fr.plainSubstrByWidth(s, strWidth);
			}
			float tx = ((getX()+padding[2])/textScale);
			float ty = ((getY()+padding[0]+(fr.lineHeight*i))/textScale);
			GuiGraphicsPose.translate(pose, tx, ty, 0);
			graphics.text(fr, s, 0, 0, col, textShadow);
			GuiGraphicsPose.translate(pose, -tx, -ty, 0);
		}
		GuiGraphicsPose.pop(pose);
		if(!hasTarget)
		{
			targetEntry = -1;
			hoverTimer = 0;
		}
	}

	protected void extractContents(GuiGraphicsExtractor graphics, int mx, int my, float partialTicks)
	{
		renderWidget(graphics, mx, my, partialTicks);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY)
	{
		if(deltaY!=0&&maxOffset > 0)
		{
			if(deltaY < 0&&offset < maxOffset)
				offset++;
			if(deltaY > 0&&offset > 0)
				offset--;
			return true;
		}
		else
			return false;
	}

	public int selectedOption = -1;

	public boolean mouseClicked(double mx, double my, int key)
	{
		selectedOption = -1;
		if(this.active&&this.visible)
			if(this.isValidClickButton(key)&&this.clicked(mx, my))
			{
				Font fr = ClientUtils.mc().font;
				double mmY = my-this.getY();
				for(int i = 0; i < Math.min(perPage, entries.get().size()); i++)
					if(mmY >= i*fr.lineHeight&&mmY < (i+1)*fr.lineHeight)
						selectedOption = offset+i;
			}
		return selectedOption!=-1;
	}
}
