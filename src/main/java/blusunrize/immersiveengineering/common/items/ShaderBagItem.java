/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class ShaderBagItem extends IEBaseItem implements IColouredItem
{
	@Nonnull
	private final Rarity rarity;

	public ShaderBagItem(Rarity rarity)
	{
		super(itemProperties().component(DataComponents.RARITY, rarity));
		this.rarity = rarity;
	}

	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return rarity.color().getColor();
	}

	public InteractionResult use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(!world.isClientSide())
			if(ShaderRegistry.totalWeight.containsKey(rarity))
			{
				Identifier shader = ShaderRegistry.getRandomShader(player.getUUID(), player.getRandom(), rarity, true);
				if(shader==null)
					return InteractionResult.FAIL;
				ItemStack shaderItem = ShaderRegistry.makeShaderStack(shader);
				Rarity shaderRarity = shaderItem.getRarity();
				if(ShaderRegistry.sortedRarityMap.indexOf(shaderRarity) <= ShaderRegistry.sortedRarityMap.indexOf(Rarity.EPIC)&&
						ShaderRegistry.sortedRarityMap.indexOf(rarity) >= ShaderRegistry.sortedRarityMap.indexOf(Rarity.COMMON))
					Utils.unlockIEAdvancement(player, "main/secret_luckofthedraw");
				stack.shrink(1);
				if(stack.getCount() <= 0)
				{
					player.setItemInHand(hand, shaderItem);
					return InteractionResult.SUCCESS;
				}
				if(!player.getInventory().add(shaderItem))
					player.drop(shaderItem, false, true);
			}
		return InteractionResult.PASS;
	}
}
