package net.minecraft.client.renderer.block.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemOverrides
{
	public static final ItemOverrides EMPTY = new ItemOverrides();

	public BakedModel resolve(
			@Nonnull BakedModel originalModel,
			@Nonnull ItemStack stack,
			@Nullable ClientLevel world,
			@Nullable LivingEntity entity,
			int seed
	)
	{
		return originalModel;
	}
}
