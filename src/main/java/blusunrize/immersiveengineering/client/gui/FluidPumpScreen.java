/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.TooltipArea;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity;
import blusunrize.immersiveengineering.common.gui.FluidPumpMenu;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class FluidPumpScreen extends IEContainerScreen<FluidPumpMenu>
{
	private static final Identifier BACKGROUND = makeTextureLocation("fluid_pump");
	private static final Identifier TANK = ieLoc("squeezer/tank_overlay");

	public FluidPumpScreen(FluidPumpMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, BACKGROUND, 176, 216);
	}

	@Nonnull
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(
				new FluidInfoArea(menu.tank, new Rect2i(leftPos+96, topPos+20, 16, 47), 20, 51, TANK),
				new EnergyInfoArea(leftPos+151, topPos+21, menu.energy),
				new TooltipArea(
						new Rect2i(leftPos+18, topPos+20, 34, 14),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.enabled")
				),
				new TooltipArea(
						new Rect2i(leftPos+18, topPos+72, 8, 8),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.placeCobble")
				),
				new TooltipArea(
						new Rect2i(leftPos+54, topPos+72, 8, 8),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.redstoneInverted")
				),
				new TooltipArea(
						new Rect2i(leftPos+32, topPos+62, 52, 8),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.speed")
				),
				new TooltipArea(
						new Rect2i(leftPos+58, topPos+22, 58, 10),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.flow")
				),
				new TooltipArea(
						new Rect2i(leftPos+58, topPos+36, 58, 10),
						Component.translatable(Lib.GUI_CONFIG+"fluidPump.power")
				)
		);
	}

	protected void init()
	{
		super.init();
		addSideButton(Direction.NORTH, 10, 84);
		addSideButton(Direction.SOUTH, 50, 84);
		addSideButton(Direction.WEST, 90, 84);
		addSideButton(Direction.EAST, 130, 84);
		this.addRenderableWidget(new PumpStatusIndicator(
				leftPos+10, topPos+101, "IF",
				() -> shortSideName(menu.getEnergyInputSide()),
				0xff2f6fa3,
				() -> ImmutableList.of(
						Component.literal("Energy input"),
						Component.literal(menu.getEnergyInputSide().getSerializedName()).withStyle(ChatFormatting.GRAY)
				)
		));
		this.addRenderableWidget(new PumpStatusIndicator(
				leftPos+50, topPos+101, "RS",
				() -> "IN",
				0xff7f5aa8,
				() -> ImmutableList.of(Component.literal("Redstone signal input"))
		));
		this.addRenderableWidget(new PumpPowerSwitch(
				leftPos+18, topPos+20,
				() -> menu.pumpEnabled,
				next -> {
					CompoundTag message = new CompoundTag();
					message.putBoolean("pumpEnabled", next);
					sendUpdateToServer(message);
				}
		));
		this.addRenderableWidget(new GuiButtonCheckbox(
				leftPos+18, topPos+72,
				Component.empty(),
				() -> menu.placeCobble,
				btn -> {
					CompoundTag message = new CompoundTag();
					message.putBoolean("togglePlaceCobble", true);
					sendUpdateToServer(message);
				}
		));
		this.addRenderableWidget(new GuiButtonCheckbox(
				leftPos+54, topPos+72,
				Component.empty(),
				() -> menu.redstoneInverted,
				btn -> {
					CompoundTag message = new CompoundTag();
					message.putBoolean("toggleRedstone", true);
					sendUpdateToServer(message);
				}
		));
		this.addRenderableWidget(new PumpSpeedSlider(
				leftPos+32, topPos+62, 52, menu.pumpSpeed,
				value -> {
					int speed = FluidPumpBlockEntity.MIN_PUMP_SPEED+Math.round(
							value*(FluidPumpBlockEntity.MAX_PUMP_SPEED-FluidPumpBlockEntity.MIN_PUMP_SPEED)
					);
					CompoundTag message = new CompoundTag();
					message.putInt("pumpSpeed", speed);
					sendUpdateToServer(message);
				}
		));
	}

	private void addSideButton(Direction side, int x, int y)
	{
		this.addRenderableWidget(new PumpSideButton(
				leftPos+x, topPos+y, side,
				() -> menu.getSideConfig(side),
				() -> menu.isJoinedSide(side),
				next -> {
					CompoundTag message = new CompoundTag();
					message.putInt("side", side.ordinal());
					message.putInt("sideConfig", next.ordinal());
					sendUpdateToServer(message);
				}
		));
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xff1d2326);
		graphics.fill(leftPos+1, topPos+1, leftPos+imageWidth-1, topPos+imageHeight-1, 0xff2b3235);
		graphics.fill(leftPos+7, topPos+16, leftPos+169, topPos+75, 0xff22282b);
		graphics.fill(leftPos+7, topPos+83, leftPos+169, topPos+160, 0xff22282b);
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+1, Lib.COLOUR_I_ImmersiveOrange);
		graphics.fill(leftPos+91, topPos+15, leftPos+117, topPos+72, 0xff111719);
		graphics.fill(leftPos+92, topPos+16, leftPos+116, topPos+71, 0xff384248);
		graphics.fill(leftPos+148, topPos+18, leftPos+161, topPos+70, 0xff111719);
		graphics.fill(leftPos+150, topPos+20, leftPos+159, topPos+68, 0xff384248);
		graphics.fill(leftPos+7, topPos+78, leftPos+169, topPos+120, 0xff1b2023);
		graphics.fill(leftPos+7, topPos+123, leftPos+169, topPos+210, 0xff22282b);
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		super.renderLabels(graphics, mouseX, mouseY);
		graphics.text(this.font, Component.literal("Flow: "+menu.fluidRate+" mB/t"), 58, 22, 0xe0e0e0, false);
		graphics.text(this.font, Component.literal("Power: "+menu.powerRate+" IF/t"), 58, 36, 0xe0e0e0, false);
		graphics.text(this.font, Component.literal("L"+menu.pumpSpeed+": "+menu.fluidRate+" mB/t"), 32, 51, 0xe0e0e0, false);
		graphics.text(this.font, Component.literal("Sides"), 9, 75, 0xe0e0e0, false);
	}

	private static class PumpSideButton extends Button implements ITooltipWidget
	{
		private final Direction side;
		private final Supplier<IOSideConfig> config;
		private final Supplier<Boolean> joined;
		private final Consumer<IOSideConfig> handler;

		private PumpSideButton(
				int x, int y, Direction side, Supplier<IOSideConfig> config, Supplier<Boolean> joined,
				Consumer<IOSideConfig> handler
		)
		{
			super(x, y, 36, 14, Component.empty(), button -> {
			}, DEFAULT_NARRATION);
			this.side = side;
			this.config = config;
			this.joined = joined;
			this.handler = handler;
		}

		public void onPress(InputWithModifiers input)
		{
			if(!joined.get())
				handler.accept(IOSideConfig.next(config.get()));
		}

		public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			this.isHovered = mouseX >= getX()&&mouseY >= getY()&&mouseX < getX()+width&&mouseY < getY()+height;
			IOSideConfig state = config.get();
			boolean isJoined = joined.get();
			int colour = isJoined?0xff33383c: switch(state)
			{
				case INPUT -> 0xff2f6fa3;
				case OUTPUT -> 0xffc07828;
				case NONE -> 0xff53585c;
			};
			if(this.isHovered&&!isJoined)
				colour = brighten(colour);
			graphics.fill(getX(), getY(), getX()+width, getY()+height, 0xff111719);
			graphics.fill(getX()+1, getY()+1, getX()+width-1, getY()+height-1, colour);
			Font font = Minecraft.getInstance().font;
			String text = shortSideName(side)+" "+(isJoined?"--": getConfigLabel(state));
			graphics.centeredText(font, Component.literal(text), getX()+width/2, getY()+3, 0xffffffff);
		}

		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			renderWidget(graphics, mouseX, mouseY, partialTicks);
		}

		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.add(Component.literal("Fluid "+side.getSerializedName()));
			if(joined.get())
				tooltip.add(Component.literal("Joined to the connector block").withStyle(ChatFormatting.GRAY));
			else
				tooltip.add(Component.empty().append(config.get().getTextComponent()).withStyle(ChatFormatting.GRAY));
		}
	}

	private static class PumpStatusIndicator extends Button implements ITooltipWidget
	{
		private final String label;
		private final Supplier<String> state;
		private final int colour;
		private final Supplier<List<Component>> tooltip;

		private PumpStatusIndicator(
				int x, int y, String label, Supplier<String> state, int colour, Supplier<List<Component>> tooltip
		)
		{
			super(x, y, 36, 14, Component.empty(), button -> {
			}, DEFAULT_NARRATION);
			this.label = label;
			this.state = state;
			this.colour = colour;
			this.tooltip = tooltip;
		}

		public void onPress(InputWithModifiers input)
		{
		}

		public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			this.isHovered = mouseX >= getX()&&mouseY >= getY()&&mouseX < getX()+width&&mouseY < getY()+height;
			int renderColour = this.isHovered?brighten(colour): colour;
			graphics.fill(getX(), getY(), getX()+width, getY()+height, 0xff111719);
			graphics.fill(getX()+1, getY()+1, getX()+width-1, getY()+height-1, renderColour);
			Font font = Minecraft.getInstance().font;
			graphics.centeredText(font, Component.literal(label+" "+state.get()), getX()+width/2, getY()+3, 0xffffffff);
		}

		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			renderWidget(graphics, mouseX, mouseY, partialTicks);
		}

		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.addAll(this.tooltip.get());
		}
	}

	private static class PumpPowerSwitch extends Button implements ITooltipWidget
	{
		private final Supplier<Boolean> enabled;
		private final Consumer<Boolean> handler;

		private PumpPowerSwitch(int x, int y, Supplier<Boolean> enabled, Consumer<Boolean> handler)
		{
			super(x, y, 34, 14, Component.empty(), button -> {
			}, DEFAULT_NARRATION);
			this.enabled = enabled;
			this.handler = handler;
		}

		public void onPress(InputWithModifiers input)
		{
			handler.accept(!enabled.get());
		}

		public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			boolean on = enabled.get();
			this.isHovered = mouseX >= getX()&&mouseY >= getY()&&mouseX < getX()+width&&mouseY < getY()+height;
			int green = on?0xff2e9d45: 0xff1f4d2b;
			int red = on?0xff5a2424: 0xffb53737;
			if(this.isHovered)
			{
				green = brighten(green);
				red = brighten(red);
			}
			graphics.fill(getX(), getY(), getX()+width, getY()+height, 0xff111719);
			graphics.fill(getX()+1, getY()+1, getX()+width/2, getY()+height-1, green);
			graphics.fill(getX()+width/2, getY()+1, getX()+width-1, getY()+height-1, red);
			int selectedLeft = on?getX()+1: getX()+width/2;
			int selectedRight = on?getX()+width/2: getX()+width-1;
			graphics.fill(selectedLeft, getY()+1, selectedRight, getY()+2, 0xffffffff);
			graphics.fill(selectedLeft, getY()+height-2, selectedRight, getY()+height-1, 0xffffffff);
			graphics.fill(selectedLeft, getY()+1, selectedLeft+1, getY()+height-1, 0xffffffff);
			graphics.fill(selectedRight-1, getY()+1, selectedRight, getY()+height-1, 0xffffffff);
			Font font = Minecraft.getInstance().font;
			graphics.centeredText(font, Component.literal("1"), getX()+width/4, getY()+3, 0xffffffff);
			graphics.centeredText(font, Component.literal("0"), getX()+width*3/4, getY()+3, 0xffffffff);
		}

		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			renderWidget(graphics, mouseX, mouseY, partialTicks);
		}

		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"fluidPump.enabled"));
			tooltip.add(Component.literal(enabled.get()?"1": "0").withStyle(enabled.get()?ChatFormatting.GREEN: ChatFormatting.RED));
		}

		private static int brighten(int colour)
		{
			int r = Math.min(255, ((colour>>16)&255)+24);
			int g = Math.min(255, ((colour>>8)&255)+24);
			int b = Math.min(255, (colour&255)+24);
			return 0xff000000|(r<<16)|(g<<8)|b;
		}
	}

	private static String getConfigLabel(IOSideConfig config)
	{
		return switch(config)
		{
			case INPUT -> "IN";
			case OUTPUT -> "OUT";
			case NONE -> "OFF";
		};
	}

	private static int brighten(int colour)
	{
		int r = Math.min(255, ((colour>>16)&255)+24);
		int g = Math.min(255, ((colour>>8)&255)+24);
		int b = Math.min(255, (colour&255)+24);
		return 0xff000000|(r<<16)|(g<<8)|b;
	}

	private static String shortSideName(Direction side)
	{
		return switch(side)
		{
			case DOWN -> "DN";
			case UP -> "UP";
			case NORTH -> "N";
			case SOUTH -> "S";
			case WEST -> "W";
			case EAST -> "E";
		};
	}

	private static class PumpSpeedSlider extends GuiSliderIE
	{
		public PumpSpeedSlider(int x, int y, int width, int value, FloatConsumer handler)
		{
			super(
					x, y, width, Component.empty(),
					FluidPumpBlockEntity.MIN_PUMP_SPEED, FluidPumpBlockEntity.MAX_PUMP_SPEED, value, handler
			);
		}

		protected void updateMessage()
		{
			this.setMessage(Component.empty());
		}
	}
}
