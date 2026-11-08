package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class AbstractWidget implements GuiEventListener, Renderable
{
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected Component message;
	protected boolean isHovered;
	private boolean focused;
	protected float alpha = 1;
	public boolean active = true;
	public boolean visible = true;

	public AbstractWidget(int x, int y, int width, int height, Component message)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.message = message;
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		renderWidget(graphics, mouseX, mouseY, partialTicks);
	}

	protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
		renderWidget(graphics, mouseX, mouseY, partialTicks);
	}

	protected boolean isValidClickButton(int button)
	{
		return button==0;
	}

	public boolean clicked(double mouseX, double mouseY)
	{
		return isMouseOver(mouseX, mouseY);
	}

	public void onClick(double mouseX, double mouseY)
	{
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(active&&visible&&isValidClickButton(button)&&clicked(mouseX, mouseY))
		{
			onClick(mouseX, mouseY);
			return true;
		}
		return false;
	}

	public boolean isMouseOver(double mouseX, double mouseY)
	{
		return visible&&mouseX >= x&&mouseY >= y&&mouseX < x+width&&mouseY < y+height;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Component getMessage()
	{
		return message;
	}

	public void setMessage(Component message)
	{
		this.message = message;
	}

	public boolean isHovered()
	{
		return isHovered;
	}

	public boolean isHoveredOrFocused()
	{
		return isHovered||focused;
	}

	public void setFocused(boolean focused)
	{
		this.focused = focused;
	}

	public boolean isFocused()
	{
		return focused;
	}

	public boolean charTyped(char codePoint, int modifiers)
	{
		return false;
	}
}
