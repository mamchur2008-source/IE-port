/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.gui.RevolverContainer;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IBulletContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class RevolverScreen extends IEContainerScreen<RevolverContainer>
{
	private static final Identifier TEXTURE = makeTextureLocation("revolver");
	private static final Identifier DRUM_MAIN = IEApi.ieLoc("revolver/drum_single");
	private static final Identifier EXTENDED_MAG = IEApi.ieLoc("revolver/drum_extend");
	private static final Identifier SECOND_DRUM = IEApi.ieLoc("revolver/drum_second");

	private final int[] bullets = new int[2];
	private final boolean otherRevolver;
	private final int offset;

	public RevolverScreen(RevolverContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE, getImageWidth(container, inventoryPlayer), 166);
		ItemStack revolver = inventoryPlayer.player.getItemBySlot(container.entityEquipmentSlot);
		if(!revolver.isEmpty()&&revolver.getItem() instanceof IBulletContainer)
			this.bullets[0] = ((IBulletContainer)revolver.getItem()).getBulletCount(revolver);
		this.otherRevolver = !this.menu.secondRevolver.isEmpty();
		if(this.otherRevolver)
		{
			this.bullets[1] = ((IBulletContainer)this.menu.secondRevolver.getItem()).getBulletCount(this.menu.secondRevolver);
			this.offset = (getDrumWidth(bullets[0])+getDrumWidth(bullets[1])+4-176)/2;
		}
		else
			this.offset = (getDrumWidth(bullets[0])-176)/2;
	}

	private static int getImageWidth(RevolverContainer container, Inventory inventoryPlayer)
	{
		int bullets = getBulletCount(inventoryPlayer.player.getItemBySlot(container.entityEquipmentSlot));
		if(!container.secondRevolver.isEmpty())
		{
			int secondBullets = getBulletCount(container.secondRevolver);
			int offset = (getDrumWidth(bullets)+getDrumWidth(secondBullets)+4-176)/2;
			return offset > 0?176+offset*2: 176;
		}
		return 176;
	}

	private static int getBulletCount(ItemStack stack)
	{
		if(!stack.isEmpty()&&stack.getItem() instanceof IBulletContainer bulletContainer)
			return bulletContainer.getBulletCount(stack);
		return 0;
	}

	private static int getDrumWidth(int bulletCount)
	{
		return bulletCount >= 18?150: bulletCount > 8?136: 74;
	}

	protected void drawContainerBackgroundPre(@Nonnull GuiGraphicsExtractor graphics, float par1, int par2, int par3)
	{
		int off = (offset < 0?-offset: 0);
		for(int hand = 0; hand < (otherRevolver?2: 1); hand++)
		{
			int side = !otherRevolver?0: (hand==0)==(ImmersiveEngineering.proxy.getClientPlayer().getMainArm()==HumanoidArm.RIGHT)?1: 0;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRUM_MAIN, leftPos+off, topPos+1, 74, 74);
			if(bullets[side] >= 18)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SECOND_DRUM, leftPos+off+47, topPos+1, 74, 103);
			else if(bullets[side] > 8)
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXTENDED_MAG, leftPos+off+57, topPos+1, 79, 39);
			off += getDrumWidth(bullets[side])+4;
		}
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos+Math.max(offset, 0), topPos+77, 0, 125, 176, 89, 256, 256);
	}

	public static void drawExternalGUI(NonNullList<ItemStack> bullets, int bulletAmount, GuiGraphicsExtractor graphics)
	{
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRUM_MAIN, 0, 1, 74, 74);
		if(bulletAmount >= 18)
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SECOND_DRUM, 47, 1, 74, 103);
		else if(bulletAmount > 8)
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EXTENDED_MAG, 57, 1, 79, 39);

		int[][] slots = RevolverContainer.slotPositions[bulletAmount >= 18?2: bulletAmount > 8?1: 0];
		for(int i = 0; i < bulletAmount; i++)
		{
			ItemStack b = bullets.get(i);
			if(!b.isEmpty())
			{
				int x;
				int y;
				if(i==0)
				{
					x = 29;
					y = 3;
				}
				else if(i-1 < slots.length)
				{
					x = slots[i-1][0];
					y = slots[i-1][1];
				}
				else
				{
					int ii = i-(slots.length+1);
					x = ii==0?48: ii==1?29: ii==3?2: 10;
					y = ii==1?57: ii==3?30: ii==4?11: 49;
				}
				graphics.item(b, x, y);
			}
		}
	}
}
