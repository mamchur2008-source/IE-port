/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.utils.GuiGraphicsPose;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.lib.manual.ManualEntry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public class ManualUnlockToast implements Toast
{
	private static final Identifier BACKGROUND_SPRITE = IEApi.ieLoc("hud/toast_manual");

	private static final Component EUREKA = Component.translatable(Lib.GUI+"toast.eureka");
	private static final Component HEADLINE = Component.translatable(Lib.GUI+"toast.manual_unlocked");

	private final Optional<AdvancementToast> originalToast;
	private final List<ManualEntry> entries;
	private Visibility visibility = Visibility.SHOW;

	public ManualUnlockToast(Optional<AdvancementToast> originalToast, List<ManualEntry> entries)
	{
		this.originalToast = originalToast;
		this.entries = entries;
	}

	public int height()
	{
		return 48+originalToast.map(Toast::height).orElse(0);
	}

	public Visibility getWantedVisibility()
	{
		return visibility;
	}

	public void update(ToastManager toastManager, long timeSinceLastVisible)
	{
		originalToast.ifPresent(toast -> toast.update(toastManager, timeSinceLastVisible));
		if(timeSinceLastVisible >= AdvancementToast.DISPLAY_TIME*toastManager.getNotificationDisplayTimeMultiplier())
			visibility = Visibility.HIDE;
	}

	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long timeSinceLastVisible)
	{
		Object pose = GuiGraphicsPose.pose(graphics);
		GuiGraphicsPose.push(pose);
		originalToast.ifPresent(toast -> {
			toast.extractRenderState(graphics, font, timeSinceLastVisible);
			GuiGraphicsPose.translate(pose, 0, toast.height(), 0);
		});
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, this.width(), 48);
		graphics.renderFakeItem(Tools.MANUAL.asItem().getDefaultInstance(), 7, 8);
		if(timeSinceLastVisible < 1000)
		{
			GuiGraphicsPose.scale(pose, 2, 2, 2);
			graphics.text(font, EUREKA, 16, 4, 0xfff78034, false);
		}
		else
		{
			graphics.text(font, HEADLINE, 32, 6, 0xfff78034, false);
			if(!this.entries.isEmpty())
			{
				int iEntry = (int)((timeSinceLastVisible/1000)%this.entries.size());
				graphics.text(font, entries.get(iEntry).getTitle(), 32, 18, 0xff555555, false);
			}
		}
		GuiGraphicsPose.pop(pose);
	}
}
