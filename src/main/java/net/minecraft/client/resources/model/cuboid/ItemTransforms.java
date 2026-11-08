package net.minecraft.client.resources.model.cuboid;

import net.minecraft.world.item.ItemDisplayContext;

public class ItemTransforms
{
	public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();

	public ItemTransforms()
	{
	}

	public ItemTransforms(
			ItemTransform thirdPersonLeftHand,
			ItemTransform thirdPersonRightHand,
			ItemTransform firstPersonLeftHand,
			ItemTransform firstPersonRightHand,
			ItemTransform head,
			ItemTransform gui,
			ItemTransform ground,
			ItemTransform fixed
	)
	{
	}

	public ItemTransform getTransform(ItemDisplayContext context)
	{
		return ItemTransform.NO_TRANSFORM;
	}
}
