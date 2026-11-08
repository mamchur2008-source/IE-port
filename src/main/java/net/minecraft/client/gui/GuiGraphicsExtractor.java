package net.minecraft.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class GuiGraphicsExtractor
{
	private final PoseStack pose = new PoseStack();

	public GuiGraphicsExtractor()
	{
	}

	public GuiGraphicsExtractor(Minecraft minecraft, GuiRenderState state, int width, int height)
	{
	}

	public PoseStack pose()
	{
		return pose;
	}

	public MultiBufferSource.BufferSource bufferSource()
	{
		return Minecraft.getInstance().renderBuffers().bufferSource();
	}

	public void flush()
	{
		bufferSource().endBatch();
	}

	public void setColor(float red, float green, float blue, float alpha)
	{
	}

	public void fill(int x1, int y1, int x2, int y2, int color)
	{
	}

	public void fill(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color)
	{
	}

	public void fill(RenderType renderType, int x1, int y1, int x2, int y2, int color)
	{
	}

	public void fillGradient(int x1, int y1, int x2, int y2, int colorFrom, int colorTo)
	{
	}

	public void blit(Identifier texture, int x, int y, int u, int v, int width, int height)
	{
	}

	public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight)
	{
	}

	public void blit(Identifier texture, int x, int y, int u, int v, int width, int height, int texWidth, int texHeight)
	{
	}

	public void blit(Identifier texture, int x, int y, int width, int height, float u0, float v0, float u1, float v1)
	{
	}

	public void blit(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight)
	{
	}

	public void blit(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color)
	{
	}

	public void blit(GpuTextureView texture, GpuSampler sampler, int x, int y, int width, int height, float u0, float v0, float u1, float v1)
	{
	}

	public void blit(int x, int y, int z, int width, int height, TextureAtlasSprite sprite, float red, float green, float blue, int alpha)
	{
	}

	public void blitSprite(Identifier texture, int x, int y, int width, int height)
	{
		blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height);
	}

	public void blitSprite(Identifier texture, int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height)
	{
		blitSprite(RenderPipelines.GUI_TEXTURED, texture, spriteWidth, spriteHeight, u, v, x, y, width, height);
	}

	public void blitSprite(RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height)
	{
	}

	public void blitSprite(RenderPipeline pipeline, Identifier texture, int spriteWidth, int spriteHeight, int u, int v, int x, int y, int width, int height)
	{
	}

	public void blitSprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height)
	{
	}

	public void blitSprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height, int color)
	{
	}

	public void renderItem(ItemStack stack, int x, int y)
	{
		item(stack, x, y);
	}

	public void renderFakeItem(ItemStack stack, int x, int y)
	{
		fakeItem(stack, x, y);
	}

	public void item(ItemStack stack, int x, int y)
	{
	}

	public void fakeItem(ItemStack stack, int x, int y)
	{
	}

	public void renderItemDecorations(Font font, ItemStack stack, int x, int y)
	{
		itemDecorations(font, stack, x, y);
	}

	public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text)
	{
		itemDecorations(font, stack, x, y, text);
	}

	public void itemDecorations(Font font, ItemStack stack, int x, int y)
	{
	}

	public void itemDecorations(Font font, ItemStack stack, int x, int y, String text)
	{
	}

	public void text(Font font, String text, int x, int y, int color)
	{
	}

	public void text(Font font, String text, int x, int y, int color, boolean shadow)
	{
	}

	public void text(Font font, Component text, int x, int y, int color)
	{
	}

	public void text(Font font, Component text, int x, int y, int color, boolean shadow)
	{
	}

	public void text(Font font, FormattedCharSequence text, int x, int y, int color)
	{
	}

	public void text(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow)
	{
	}

	public void drawCenteredString(Font font, String text, int x, int y, int color)
	{
		centeredText(font, text, x, y, color);
	}

	public void drawCenteredString(Font font, Component text, int x, int y, int color)
	{
		centeredText(font, text, x, y, color);
	}

	public void centeredText(Font font, String text, int x, int y, int color)
	{
	}

	public void centeredText(Font font, Component text, int x, int y, int color)
	{
	}

	public void centeredText(Font font, FormattedCharSequence text, int x, int y, int color)
	{
	}

	public void renderTooltip(Font font, List<Component> lines, Optional<?> image, int x, int y)
	{
	}

	public void renderTooltip(Font font, List<FormattedCharSequence> lines, int x, int y)
	{
	}

	public void renderTooltip(Font font, List<Component> lines, Optional<?> image, ItemStack stack, int x, int y)
	{
	}

	public void renderTooltip(Font font, List<FormattedCharSequence> lines, ClientTooltipPositioner positioner, int x, int y)
	{
	}

	public void setTooltipForNextFrame(Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y)
	{
	}

	public void setTooltipForNextFrame(Font font, List<Component> lines, Optional<TooltipComponent> image, int x, int y, Identifier style)
	{
	}

	public void setTooltipForNextFrame(Font font, List<? extends FormattedCharSequence> lines, int x, int y)
	{
	}

	public void submitPictureInPictureRenderState(PictureInPictureRenderState renderState)
	{
	}

	public ScreenRectangle peekScissorStack()
	{
		return null;
	}
}
