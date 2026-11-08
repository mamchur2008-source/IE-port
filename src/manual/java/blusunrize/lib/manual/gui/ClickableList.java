/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.Tree;
import blusunrize.lib.manual.Tree.AbstractNode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClickableList extends Button
{
	private String[] headers;
	private boolean[] isCategory;
	@Nonnull
	private List<Tree.AbstractNode<Identifier, ManualEntry>> nodes = new ArrayList<>();
	private float textScale;
	private final Consumer<AbstractNode<Identifier, ManualEntry>> handler;
	private int offset;
	private int maxOffset;
	private final int perPage;
	private ManualScreen gui;

	ClickableList(ManualScreen gui, int x, int y, int w, int h, float textScale,
				  @Nonnull List<Tree.AbstractNode<Identifier, ManualEntry>> nodes,
				  Consumer<Tree.AbstractNode<Identifier, ManualEntry>> handler)
	{
		super(x, y, w, h, Component.empty(), btn -> {
		}, DEFAULT_NARRATION);
		this.gui = gui;
		this.textScale = textScale;
		this.handler = handler;
		this.perPage = (h-8)/getFontHeight();
		setEntries(nodes);
	}

	int getFontHeight()
	{
		return (int)(gui.manual.fontRenderer().lineHeight*textScale);
	}

	@Override
	protected void extractContents(@NotNull GuiGraphicsExtractor graphics, int mx, int my, float partialTicks)
	{
		Matrix3x2fStack transform = graphics.pose();
		if(!visible)
			return;
		Font fr = gui.manual.fontRenderer();

		int mmY = my-this.getY();
		transform.pushMatrix();
		transform.scale(textScale, textScale);
		transform.translate(getX()/textScale, getY()/textScale);
		isHovered = mx >= getX()&&mx < getX()+width&&my >= getY()&&my < getY()+height;
		for(int i = 0; i < Math.min(perPage, headers.length); i++)
		{
			int col = gui.manual.getTextColour();
			boolean currEntryHovered = isHovered&&mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight();
			if(currEntryHovered)
				col = gui.manual.getHighlightColour();
			if(i!=0)
				transform.translate(0, getFontHeight());
			int j = offset+i;
			if(j > headers.length-1)
				j = headers.length-1;
			String s = headers[j];
			if(isCategory[j])
				graphics.blit(RenderPipelines.GUI_TEXTURED, gui.texture, 0, 0, 11, 226+(currEntryHovered?20: 0), 5, 10, 256, 256);
			graphics.text(fr, s, isCategory[j]?7: 0, 0, col, false);
		}
		transform.popMatrix();
		if(maxOffset > 0)
		{
			final int minVisibleBlack = 0x1B<<24;
			final int mainBarBlack = 0x28<<24;
			final float totalHeight = maxOffset*getFontHeight()+height;
			final float heightTopRel = (offset*getFontHeight())/totalHeight;
			final float heightBottomRel = (offset*getFontHeight()+height)/totalHeight;
			final int heightTopAbs = (int)(heightTopRel*height);
			final int heightBottomAbs = (int)(heightBottomRel*height);
			graphics.fill(getX()+width, getY(), getX()+width+8, getY()+height, minVisibleBlack);
			graphics.fill(getX()+width+1, getY()+heightTopAbs, getX()+width+7, getY()+heightBottomAbs, mainBarBlack);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		if(scrollY < 0&&offset < maxOffset)
		{
			offset++;
			return true;
		}
		if(scrollY > 0&&offset > 0)
		{
			offset--;
			return true;
		}
		return false;
	}


	@Nullable
	public AbstractNode<Identifier, ManualEntry> getSelected(double mx, double my)
	{
		if(!isMouseOver(mx, my))
			return null;
		double mmY = my-this.getY();
		for(int i = 0; i < Math.min(perPage, headers.length); i++)
			if(mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight())
				return nodes.get(offset+i);
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
	{
		AbstractNode<Identifier, ManualEntry> selected = getSelected(event.x(), event.y());
		if(selected==null)
			return false;
		handler.accept(selected);
		return true;
	}

	public void setEntries(List<AbstractNode<Identifier, ManualEntry>> nodes)
	{
		this.nodes = nodes;
		headers = new String[nodes.size()];
		isCategory = new boolean[nodes.size()];
		for(int i = 0; i < nodes.size(); i++)
		{
			headers[i] = ManualUtils.getTitleForNode(nodes.get(i), gui.manual);
			isCategory[i] = !nodes.get(i).isLeaf();
		}

		if(perPage < headers.length)
			maxOffset = headers.length-perPage;
		else
			maxOffset = 0;
		height = getFontHeight()*Math.min(perPage, headers.length);
	}

	public int getHeight()
	{
		return height;
	}
}
