/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.ButtonTexture;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ShelfLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ShelfLogic.CrateVariant;
import blusunrize.immersiveengineering.common.gui.ShelfMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static blusunrize.immersiveengineering.common.gui.ShelfMenu.*;

public class ShelfScreen extends IEContainerScreen<ShelfMenu>
{
	private static final Identifier TEXTURE = makeTextureLocation("shelf");
	private static final ButtonTexture BUTTON = new ButtonTexture(ieLoc("shelf/swap"));
	private static final Component TEXT_SWAP = Component.translatable(Lib.GUI_CONFIG+"shelf.swap");
	private int playerInvX = 0;
	private int playerInvY = 0;
	private GuiButtonIE swapButton;

	public ShelfScreen(ShelfMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE, COLUMN_WIDTH*2, 2*CRATE_SEGMENT+INV_SEGMENT);
		this.titleLabelY = 3;
	}

	protected void init()
	{
		this.playerInvX = COLUMN_WIDTH/2;
		this.playerInvY = 2*CRATE_SEGMENT;
		super.init();

		this.inventoryLabelY = this.playerInvY+3;
		this.clearWidgets();
		this.swapButton = this.addRenderableWidget(new GuiButtonIE(
				leftPos+playerInvX+COLUMN_WIDTH+2, topPos+playerInvY, 16, 16,
				Component.empty(), BUTTON, button -> {
			boolean b = !menu.backside.get();
			menu.backside.set(b);
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("backside", b);
			sendUpdateToServer(nbt);
		}));
	}

	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		if(swapButton.isHovered()&&menu.getCarried().isEmpty())
			addLine.accept(TEXT_SWAP);
	}


	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		// Crates
		List<ItemStack> crates = this.menu.backside.get()?this.menu.cratesBack.get(): this.menu.cratesFront.get();
		Map<Item, CrateVariant> variants = ShelfLogic.CRATE_VARIANTS.get();
		for(int i = 0; i < crates.size(); i++)
			if(!crates.get(i).isEmpty())
			{
				CrateVariant variant = variants.get(crates.get(i).getItem());
				int x = i%2*COLUMN_WIDTH;
				int y = i/2*CRATE_SEGMENT;
				if(variant.color()!=-1)
				{
					Color4 color = Color4.fromRGB(variant.color());
					GuiHelper.colouredBlit(
							graphics, background, leftPos+x, topPos+y, 0,
							COLUMN_WIDTH, CRATE_SEGMENT, 0, INV_SEGMENT+variant.screenVOffset(),
							color.r(), color.g(), color.b(), color.a()
					);
				}
				else
					GuiHelper.blit(
							graphics, background, leftPos+x, topPos+y,
							0, INV_SEGMENT+variant.screenVOffset(), COLUMN_WIDTH, CRATE_SEGMENT
					);
			}
		// Player Inventory
		GuiHelper.blit(graphics, background, leftPos+playerInvX, topPos+playerInvY, 0, 0, COLUMN_WIDTH, INV_SEGMENT);
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		List<ItemStack> crates = this.menu.backside.get()?this.menu.cratesBack.get(): this.menu.cratesFront.get();
		for(int i = 0; i < crates.size(); i++)
			if(!crates.get(i).isEmpty())
			{
				int x = i%2*COLUMN_WIDTH;
				int y = i/2*CRATE_SEGMENT;
				graphics.text(
						this.font, crates.get(i).getHoverName(),
						x+titleLabelX, y+titleLabelY,
						Lib.COLOUR_I_ImmersiveOrange, false
				);
			}
		graphics.text(
				this.font, playerInventoryTitle,
				playerInvX+inventoryLabelX, inventoryLabelY,
				Lib.COLOUR_I_ImmersiveOrange, false
		);
	}
}
