package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class EditBox extends AbstractWidget
{
	private String value = "";
	private Consumer<String> responder = s -> {};

	public EditBox(Font font, int x, int y, int width, int height, Component message)
	{
		super(x, y, width, height, message);
	}

	public void setTextColor(int color)
	{
	}

	public void setTextColorUneditable(int color)
	{
	}

	public void setBordered(boolean bordered)
	{
	}

	public void setCanLoseFocus(boolean canLoseFocus)
	{
	}

	public void setMaxLength(int maxLength)
	{
	}

	public void setResponder(Consumer<String> responder)
	{
		this.responder = responder;
	}

	public void setValue(String value)
	{
		this.value = value;
		this.responder.accept(value);
	}

	public String getValue()
	{
		return value;
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	public boolean keyPressed(int key, int scancode, int modifiers)
	{
		return false;
	}

	public boolean charTyped(char codePoint, int modifiers)
	{
		return false;
	}

	public boolean canConsumeInput()
	{
		return false;
	}
}
