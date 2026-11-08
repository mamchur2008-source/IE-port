package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;

public class Button extends AbstractWidget
{
	protected static final CreateNarration DEFAULT_NARRATION = button -> button.getMessage();
	protected final OnPress onPress;
	protected final CreateNarration createNarration;

	protected Button(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration)
	{
		super(x, y, width, height, message);
		this.onPress = onPress;
		this.createNarration = createNarration;
	}

	public void onPress()
	{
		onPress.onPress(this);
	}

	public void onClick(double mouseX, double mouseY)
	{
		onPress();
	}

	public interface OnPress
	{
		void onPress(Button button);
	}

	public interface CreateNarration
	{
		Component createNarrationMessage(Button button);
	}
}
