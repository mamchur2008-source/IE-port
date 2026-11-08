/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.utils.GuiGraphicsPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class GuiSelectBox<E> extends GuiButtonState<E>
{
	private static final Identifier TEXTURE = ieLoc("machine_interface/select_box");
	private static final Identifier BUTTON = ieLoc("machine_interface/select_button");

	private static final int WIDTH_LEFT = 8;
	private static final int WIDTH_MIDDLE = 4;
	private static final int WIDTH_RIGHT = 12;
	private static final int WIDTH_STATIC = WIDTH_LEFT+WIDTH_RIGHT;
	private static final int WIDTH_BUTTON = 10;
	private static final int HEIGHT_BASE = 16;
	private static final int OPEN_OFFSET = 8;
	private static final int TEXT_INDENT = 4;

	private final Supplier<E[]> optionGetter;
	private final Function<E, Component> messageGetter;
	private final int minWidth;
	private boolean opened = false;
	private int openedHeight;

	private int selectedState = -1;

	public GuiSelectBox(
			int x, int y, int minWidth, Supplier<E[]> optionGetter, IntSupplier selectedOption,
			Function<E, Component> messageGetter, IIEPressable<GuiSelectBox<E>> handler
	)
	{
		super(x, y, 64, 16, Component.empty(), optionGetter.get(), selectedOption,
				allSame(optionGetter.get(), new ButtonTexture(TEXTURE)), btn -> handler.onIEPress((GuiSelectBox<E>)btn));
		this.optionGetter = optionGetter;
		this.messageGetter = messageGetter;
		this.minWidth = minWidth;
		this.recalculateOptionsAndSize();
	}

	public void recalculateOptionsAndSize()
	{
		this.states = optionGetter.get();
		// set width based on widest text
		this.width = Math.max(
				WIDTH_STATIC+minWidth,
				WIDTH_STATIC+Arrays.stream(this.states).mapToInt(value -> mc().font.width(messageGetter.apply(value))).max().orElse(this.width)
		);
		this.openedHeight = mc().font.lineHeight*this.states.length;

	}

	public int getClickedState()
	{
		return selectedState!=-1?selectedState: this.getStateAsInt();
	}

	public Component getMessage()
	{
		return this.messageGetter.apply(this.getState());
	}

	protected int getTextColor(boolean highlighted)
	{
		if(highlighted)
			return Lib.COLOUR_I_ImmersiveOrange;
		return 0x555555;
	}

	public void onClick(double mouseX, double mouseY)
	{
		if(!opened)
		{
			this.opened = true;
			this.height = HEIGHT_BASE+openedHeight;
		}
		else
		{
			int sel = getHighlightedIndex(mouseX, mouseY);
			if(sel!=-1)
			{
				this.selectedState = sel;
				this.onPress.onPress(this);
			}
			this.opened = false;
			this.height = HEIGHT_BASE;
		}
	}

	private int getHighlightedIndex(double mouseX, double mouseY)
	{
		if(mouseX > getX()+(width-WIDTH_BUTTON))
			return -1;
		int calc = (int)((mouseY-getY()-8)/mc().font.lineHeight);
		return calc >= 0&&calc < states.length?calc: -1;
	}


	public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		if(this.visible)
		{
			Font fontrenderer = mc.font;
			this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BUTTON, getX()+width-WIDTH_BUTTON, getY(), WIDTH_BUTTON, HEIGHT_BASE);
			if(!this.opened)
			{
				// basic field
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), width-WIDTH_BUTTON, height);

				// text
				Component text = getMessage();
				int textX = getX()+TEXT_INDENT;
				int textY = getY()+HEIGHT_BASE/2-fontrenderer.lineHeight/2;
				graphics.text(fontrenderer, text, textX, textY, getTextColor(this.isHovered), false);
			}
			else
			{
				Object pose = GuiGraphicsPose.pose(graphics);
				GuiGraphicsPose.push(pose);
				GuiGraphicsPose.translate(pose, 0, 0, 2);
				// background

				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), width-WIDTH_BUTTON, openedHeight+OPEN_OFFSET+2);

				// text
				for(int j = 0; j < states.length; j++)
				{
					Component text = messageGetter.apply(states[j]);
					int textX = getX()+TEXT_INDENT;
					int textY = getY()+OPEN_OFFSET+j*fontrenderer.lineHeight;
					boolean highlighted = isHovered&&getHighlightedIndex(mouseX, mouseY)==j;
					graphics.text(fontrenderer, text, textX, textY, getTextColor(highlighted), false);
				}
				GuiGraphicsPose.pop(pose);
			}

		}
	}
}
