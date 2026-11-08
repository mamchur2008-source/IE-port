package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T>
{
	protected int imageWidth;
	protected int imageHeight;
	protected int titleLabelX;
	protected int titleLabelY;
	protected int inventoryLabelX;
	protected int inventoryLabelY;
	protected final T menu;
	protected final Component playerInventoryTitle;
	protected Slot hoveredSlot;
	protected int leftPos;
	protected int topPos;

	public AbstractContainerScreen(T menu, Inventory inventory, Component title)
	{
		this(menu, inventory, title, 176, 166);
	}

	public AbstractContainerScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight)
	{
		super(title);
		this.menu = menu;
		this.playerInventoryTitle = inventory.getDisplayName();
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	public void init(Minecraft minecraft, int width, int height)
	{
		this.minecraft = minecraft;
		this.width = width;
		this.height = height;
		init();
	}

	protected void init()
	{
		this.leftPos = (this.width-this.imageWidth)/2;
		this.topPos = (this.height-this.imageHeight)/2;
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		renderBg(graphics, partialTicks, mouseX, mouseY);
		renderLabels(graphics, mouseX, mouseY);
	}

	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
	}

	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
	}

	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY)
	{
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
	}

	protected void renderTooltip(GuiGraphicsExtractor graphics, int x, int y)
	{
	}

	protected List<Component> getTooltipFromContainerItem(ItemStack stack)
	{
		return List.of(stack.getHoverName());
	}

	public boolean keyPressed(int key, int scancode, int modifiers)
	{
		return false;
	}

	public boolean charTyped(char codePoint, int modifiers)
	{
		return false;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		return false;
	}

	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY)
	{
		return mouseX >= leftPos+x&&mouseY >= topPos+y&&mouseX < leftPos+x+width&&mouseY < topPos+y+height;
	}

	public T getMenu()
	{
		return menu;
	}

	public Slot getSlotUnderMouse()
	{
		return hoveredSlot;
	}

	protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput type)
	{
	}

	public int getLeftPos()
	{
		return leftPos;
	}

	public int getTopPos()
	{
		return topPos;
	}

	public int getGuiLeft()
	{
		return leftPos;
	}

	public int getGuiTop()
	{
		return topPos;
	}

	public int getXSize()
	{
		return imageWidth;
	}

	public int getYSize()
	{
		return imageHeight;
	}
}
