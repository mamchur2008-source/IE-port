/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.register.IEPotions.IEPotion;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID)
public class ClientExtensions
{
	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent ev)
	{
		ev.registerItem(
				ItemCallback.USE_IEOBJ_RENDER,
				Tools.BUZZSAW.asItem(),
				Tools.DRILL.asItem(),
				Weapons.CHEMTHROWER.asItem(),
				Weapons.RAILGUN.asItem(),
				Weapons.REVOLVER.asItem(),
				Misc.FLUORESCENT_TUBE.asItem(),
				Misc.SHIELD.asItem()
		);
		for(var potion : IEPotions.REGISTER.getEntries())
		{
			var iePotion = (IEPotion)potion.get();
			ev.registerMobEffect(
					new IClientMobEffectExtensions()
					{
						public boolean isVisibleInGui(MobEffectInstance instance)
						{
							return iePotion.showInHud;
						}

						public boolean isVisibleInInventory(MobEffectInstance instance)
						{
							return iePotion.showInInventory;
						}
					},
					iePotion
			);
		}
	}
}
