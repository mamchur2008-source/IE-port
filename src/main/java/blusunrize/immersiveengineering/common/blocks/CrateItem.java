/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.network.MessageIncognitoSync;
import blusunrize.immersiveengineering.common.register.IEPotions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingVisibilityEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

import static net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent.LivingTargetType.MOB_TARGET;

@EventBusSubscriber(modid = Lib.MODID)
public class CrateItem extends BlockItemIE
{
	public static Set<Integer> incognitoPlayers = new HashSet<>();

	public CrateItem(Block b, Properties props)
	{
		super(b, props);
	}

	@Nullable
	public EquipmentSlot getEquipmentSlot(ItemStack stack)
	{
		// Only equip empty crates
		final ItemContainerContents items = stack.get(DataComponents.CONTAINER);
		if(items!=null&&items.nonEmptyItems().iterator().hasNext())
			return null;
		return EquipmentSlot.CHEST;
	}

	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		if(!world.isClientSide()&&entity instanceof Player player&&player.isCrouching()&&itemSlot==PowerpackItem.CHEST_SLOT)
		{
			MobEffectInstance effect = player.getEffect(IEPotions.INCOGNITO);
			if(effect!=null&&effect.getAmplifier() < 1&&effect.getDuration() < 20)
			{
				// If the effect has less than 20 ticks remaining, that means we've stood still long enough to go to tier 2
				player.addEffect(new MobEffectInstance(IEPotions.INCOGNITO, -1, 1, false, false, true));
				PacketDistributor.sendToAllPlayers(new MessageIncognitoSync(player.getId(), true));
			}
			else if(effect==null&&(player.getDeltaMovement().x==0&&player.getDeltaMovement().z==0))
				// If no effect is applied, and the player is stood still, put in a 3 second effect!
				player.addEffect(new MobEffectInstance(IEPotions.INCOGNITO, 60, 0, false, false, true));
		}
	}

	@SubscribeEvent
	public static void adjustVisibility(LivingVisibilityEvent event)
	{
		MobEffectInstance effect = event.getEntity().getEffect(IEPotions.INCOGNITO);
		if(effect==null)
			return;
		if(effect.getAmplifier() > 0)
			event.modifyVisibility(0);
		else
			event.modifyVisibility(0.125);
	}

	@SubscribeEvent
	public static void preventTargeting(LivingChangeTargetEvent event)
	{
		if(event.getTargetType()==MOB_TARGET&&event.getOriginalAboutToBeSetTarget() instanceof Player player
				&&player.getEffect(IEPotions.INCOGNITO) instanceof MobEffectInstance effect&&effect.getAmplifier() > 0)
		{
			event.setNewAboutToBeSetTarget(null);
		}
	}
}
