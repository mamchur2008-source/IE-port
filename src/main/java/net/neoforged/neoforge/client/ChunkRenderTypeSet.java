package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChunkRenderTypeSet implements Iterable<RenderType>
{
	private final Set<RenderType> renderTypes;

	private ChunkRenderTypeSet(Set<RenderType> renderTypes)
	{
		this.renderTypes = renderTypes;
	}

	public static ChunkRenderTypeSet of(RenderType... renderTypes)
	{
		return new ChunkRenderTypeSet(new LinkedHashSet<>(Arrays.asList(renderTypes)));
	}

	public static ChunkRenderTypeSet all()
	{
		return of(
				RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS),
				RenderTypes.itemCutout(InventoryMenu.BLOCK_ATLAS),
				RenderTypes.itemTranslucent(InventoryMenu.BLOCK_ATLAS)
		);
	}

	public static ChunkRenderTypeSet none()
	{
		return new ChunkRenderTypeSet(Collections.emptySet());
	}

	public boolean isEmpty()
	{
		return renderTypes.isEmpty();
	}

	public Iterator<RenderType> iterator()
	{
		return renderTypes.iterator();
	}
}
