/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class RockcutterItem extends SawbladeItem
{
	private static final Set<ItemAbility> TOOL_ACTIONS = ImmutableSet.of(
			ItemAbilities.SHEARS_CARVE
	);
	public static final Identifier TEXTURE = ImmersiveEngineering.rl("item/rockcutter_blade");

	public RockcutterItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(maxDamage, sawbladeSpeed, sawbladeDamage, TEXTURE);
	}

	public boolean canSawbladeFellTree()
	{
		return false;
	}

	public Map<ResourceKey<Enchantment>, Integer> getExtraEnchantments()
	{
		return Map.of(Enchantments.SILK_TOUCH, 1);
	}

	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> s.is(IETags.rockcutterHarvestable);
	}

	public Set<ItemAbility> getItemAbilities()
	{
		return TOOL_ACTIONS;
	}
}
