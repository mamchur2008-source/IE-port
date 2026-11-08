/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.items.RevolverItem.Perks;
import blusunrize.immersiveengineering.common.items.RevolverItem.RevolverPerk;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;

public class RevolverAssemblyRecipe extends TurnAndCopyRecipe
{
	public RevolverAssemblyRecipe(ShapedRecipe vanilla, List<Integer> copyNBT)
	{
		super(vanilla, copyNBT);
	}

	@Nonnull
	public RecipeSerializer<? extends CraftingRecipe> getSerializer()
	{
		return RecipeSerializers.REVOLVER_ASSEMBLY_SERIALIZER.get();
	}

	@Nonnull
	public ItemStack assemble(@Nonnull CraftingInput matrix)
	{
		if(nbtCopyTargetSlot!=null)
		{
			ItemStack out = getResultItem(null).copy();
			EnumMap<RevolverPerk, Double> mergedPerks = new EnumMap<>(RevolverPerk.class);
			for(int targetSlot : nbtCopyTargetSlot)
			{
				ItemStack s = matrix.getItem(targetSlot);
				var perks = s.get(IEDataComponents.REVOLVER_PERKS);
				if(perks!=null)
					for(var entry : perks.perks().entrySet())
						mergedPerks.merge(entry.getKey(), entry.getValue(), entry.getKey()::concat);
			}
			out.set(IEDataComponents.REVOLVER_PERKS, new Perks(mergedPerks));
			return out;
		}
		else
			return super.assemble(matrix);
	}
}
