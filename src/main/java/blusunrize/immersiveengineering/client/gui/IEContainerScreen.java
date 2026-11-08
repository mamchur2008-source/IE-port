/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.network.MessageContainerUpdate;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 05.07.2017
 */
public abstract class IEContainerScreen<C extends AbstractContainerMenu> extends AbstractContainerScreen<C>
{
	private final ResettableLazy<List<InfoArea>> infoAreas;
	protected final Identifier background;

	public IEContainerScreen(C inventorySlotsIn, Inventory inv, Component title, Identifier background)
	{
		this(inventorySlotsIn, inv, title, background, 176, 166);
	}

	public IEContainerScreen(C inventorySlotsIn, Inventory inv, Component title, Identifier background, int imageWidth, int imageHeight)
	{
		super(inventorySlotsIn, inv, title, imageWidth, imageHeight);
		this.background = background;
		this.infoAreas = new ResettableLazy<>(this::makeInfoAreas);
	}

	protected void init()
	{
		super.init();
		this.infoAreas.reset();
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas() {
		return ImmutableList.of();
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		// Only difference to super version is the text color
		graphics.text(this.font, title, titleLabelX, titleLabelY, Lib.COLOUR_I_ImmersiveOrange, true);
		graphics.text(this.font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, Lib.COLOUR_I_ImmersiveOrange, true);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		drawBackgroundTexture(graphics);
		drawContainerBackgroundPre(graphics, partialTicks, mouseX, mouseY);
		for(InfoArea area : infoAreas.get())
			area.draw(graphics);
		super.extractContents(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		renderLabels(graphics, mouseX, mouseY);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		List<Component> tooltip = getAdditionalTooltip(mouseX, mouseY);
		if(!tooltip.isEmpty())
			graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
		else
			renderTooltip(graphics, mouseX, mouseY);
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		extractContents(graphics, mouseX, mouseY, partialTicks);
		extractTooltip(graphics, mouseX, mouseY);
	}

	private List<Component> getAdditionalTooltip(int mouseX, int mouseY)
	{
		List<Component> tooltip = new ArrayList<>();
		for(InfoArea area : infoAreas.get())
			area.fillTooltip(mouseX, mouseY, tooltip);
		for(GuiEventListener w : children())
			if(w.isMouseOver(mouseX, mouseY)&&w instanceof ITooltipWidget ttw)
				ttw.gatherTooltip(mouseX, mouseY, tooltip);
		gatherAdditionalTooltips(
				mouseX, mouseY, tooltip::add, t -> tooltip.add(TextUtils.applyFormat(t, ChatFormatting.GRAY))
		);
		return tooltip;
	}

	protected void renderTooltip(GuiGraphicsExtractor graphics, int x, int y)
	{
		super.extractTooltip(graphics, x, y);
	}

	protected boolean isMouseIn(int mouseX, int mouseY, int x, int y, int w, int h)
	{
		return mouseX >= leftPos+x&&mouseY >= topPos+y
				&&mouseX < leftPos+x+w&&mouseY < topPos+y+h;
	}

	public void fullInit()
	{
		this.init(width, height);
	}

	protected final void renderBg(@Nonnull GuiGraphicsExtractor graphics, float partialTicks, int x, int y)
	{
		drawBackgroundTexture(graphics);
		drawContainerBackgroundPre(graphics, partialTicks, x, y);
		for(InfoArea area : infoAreas.get())
			area.draw(graphics);
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		graphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float partialTicks, int x, int y)
	{
	}

	protected void gatherAdditionalTooltips(
			int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray
	)
	{
	}

	public static Identifier makeTextureLocation(String name)
	{
		return ImmersiveEngineering.rl("textures/gui/"+name+".png");
	}

	protected void sendUpdateToServer(CompoundTag message)
	{
		ClientPacketDistributor.sendToServer(new MessageContainerUpdate(menu.containerId, message));
	}
}
