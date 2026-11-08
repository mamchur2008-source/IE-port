/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GiveGiftToHero.class)
public interface HeroGiftsTaskAccess
{
	@Accessor("GIFTS")
	static Map<VillagerProfession, Identifier> getGifts()
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
