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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

public class GuiButtonState<E> extends Button implements ITooltipWidget
{
	public E[] states;
	private final IntSupplier state;
	private final BiConsumer<List<Component>, E> tooltip;
	private final Map<E, ButtonTexture> texture;
	public int[] textOffset;

	protected static <E> Map<E, ButtonTexture> allSame(E[] keys, ButtonTexture texture)
	{
		Map<E, ButtonTexture> map = new HashMap<>();
		for(E key : keys)
			map.put(key, texture);
		return map;
	}

	public GuiButtonState(
			int x, int y, int w, int h,
			Component name,
			E[] states, IntSupplier state, Map<E, ButtonTexture> texture,
			IIEPressable<GuiButtonState<E>> handler
	)
	{
		this(x, y, w, h, name, states, state, texture, handler, (a, b) -> {
		});
	}

	public GuiButtonState(
			int x, int y, int w, int h,
			Component name,
			E[] states, IntSupplier state, Map<E, ButtonTexture> texture,
			IIEPressable<GuiButtonState<E>> handler,
			BiConsumer<List<Component>, E> tooltip)
	{
		super(x, y, w, h, name, handler, DEFAULT_NARRATION);
		this.states = states;
		this.state = state;
		this.tooltip = tooltip;
		this.textOffset = new int[]{width+1, height/2-3};
		this.texture = texture;
	}

	protected int getNextStateInt()
	{
		return (state.getAsInt()+1)%states.length;
	}

	public E getNextState()
	{
		return this.states[getNextStateInt()];
	}

	public E getState()
	{
		return this.states[this.state.getAsInt()];
	}

	protected int getStateAsInt()
	{
		return this.state.getAsInt();
	}

	public int[] getTextOffset(Font fontrenderer)
	{
		return this.textOffset;
	}

	protected int getTextColor(boolean highlighted)
	{
		if(!this.active)
			return 0xA0A0A0;
		if(highlighted)
			return Lib.COLOUR_I_ImmersiveOrange;
		return 0xE0E0E0;
	}

	public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		Font fontrenderer = mc.font;
		this.isHovered = mouseX >= this.getX()&&mouseY >= this.getY()&&mouseX < this.getX()+this.width&&mouseY < this.getY()+this.height;
		graphics.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				texture.get(this.states[state.getAsInt()]).get(this.isHovered), getX(), getY(), width, height
		);
		if(!getMessage().getString().isEmpty())
		{
			int[] offset = getTextOffset(fontrenderer);
			graphics.text(fontrenderer, getMessage(), getX()+offset[0], getY()+offset[1], getTextColor(this.isHovered), false);
		}
	}

	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		renderWidget(graphics, mouseX, mouseY, partialTicks);
	}

	public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
	{
		this.tooltip.accept(tooltip, getState());
	}
}
