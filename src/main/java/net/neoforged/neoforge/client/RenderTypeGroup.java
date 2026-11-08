package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.inventory.InventoryMenu;

public record RenderTypeGroup(RenderType block, RenderType item, RenderType fabulous)
{
	public static final RenderTypeGroup EMPTY = new RenderTypeGroup(
			RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS),
			RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS),
			RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS)
	);

	public RenderType entity()
	{
		return item;
	}

	public RenderType entityFabulous()
	{
		return fabulous;
	}
}
