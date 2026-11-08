package net.minecraft.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class Screen
{
	protected final Component title;
	protected Minecraft minecraft = Minecraft.getInstance();
	protected Font font = Minecraft.getInstance().font;
	public int width;
	public int height;
	protected float alpha = 1;
	private GuiEventListener focused;
	private boolean dragging;
	private final List<GuiEventListener> children = new ArrayList<>();
	protected final List<Renderable> renderables = new ArrayList<>();

	protected Screen(Component title)
	{
		this.title = title;
	}

	protected void init()
	{
	}

	public void init(Minecraft minecraft, int width, int height)
	{
		this.minecraft = minecraft;
		this.font = minecraft.font;
		this.width = width;
		this.height = height;
		init();
	}

	public void init(int width, int height)
	{
		init(Minecraft.getInstance(), width, height);
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	public void renderBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
	{
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		return false;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
	{
		return focused!=null&&focused.isMouseOver(mouseX, mouseY);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
	{
		return false;
	}

	public boolean charTyped(char codePoint, int modifiers)
	{
		return false;
	}

	public boolean isPauseScreen()
	{
		return true;
	}

	public void onClose()
	{
	}

	public void setFocused(GuiEventListener listener)
	{
		this.focused = listener;
	}

	public GuiEventListener getFocused()
	{
		return focused;
	}

	public boolean isDragging()
	{
		return dragging;
	}

	public <T extends GuiEventListener> T addWidget(T widget)
	{
		children.add(widget);
		return widget;
	}

	public <T extends GuiEventListener> T addRenderableWidget(T widget)
	{
		children.add(widget);
		if(widget instanceof Renderable renderable)
			renderables.add(renderable);
		return widget;
	}

	public void removeWidget(GuiEventListener widget)
	{
		children.remove(widget);
		if(widget instanceof Renderable renderable)
			renderables.remove(renderable);
	}

	public void clearWidgets()
	{
		children.clear();
	}

	public List<? extends GuiEventListener> children()
	{
		return children;
	}

	public Minecraft getMinecraft()
	{
		return minecraft;
	}

	public static boolean hasControlDown()
	{
		return false;
	}

	public static boolean hasShiftDown()
	{
		return Minecraft.getInstance().options.keyShift.isDown();
	}
}
