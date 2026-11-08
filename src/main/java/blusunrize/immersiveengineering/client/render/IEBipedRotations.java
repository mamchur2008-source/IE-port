/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.items.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

public class IEBipedRotations
{
	public static void handleBipedRotations(HumanoidModel<?> model, HumanoidRenderState renderState)
	{
		if(!IEClientConfig.fancyItemHolding.get())
			return;

		handleHeldItem(model, renderState.rightHandItemStack, true);
		handleHeldItem(model, renderState.leftHandItemStack, false);
	}

	private static void handleHeldItem(HumanoidModel<?> model, ItemStack heldItem, boolean right)
	{
		if(heldItem==null||heldItem.isEmpty())
			return;

		if(heldItem.getItem() instanceof RevolverItem)
		{
			if(right)
			{
				model.rightArm.xRot = -1.39626f+model.head.xRot;
				model.rightArm.yRot = -.08726f+model.head.yRot;
			}
			else
			{
				model.leftArm.xRot = -1.39626f+model.head.xRot;
				model.leftArm.yRot = .08726f+model.head.yRot;
			}
		}
		else if(heldItem.getItem() instanceof DrillItem||heldItem.getItem() instanceof ChemthrowerItem)
		{
			if(right)
			{
				model.leftArm.xRot = -.87266f;
				model.leftArm.yRot = .52360f;
			}
			else
			{
				model.rightArm.xRot = -.87266f;
				model.rightArm.yRot = -.52360f;
			}
		}
		else if(heldItem.getItem() instanceof BuzzsawItem)
		{
			if(right)
			{
				model.leftArm.xRot = -.87266f;
				model.leftArm.yRot = .78539f;
			}
			else
			{
				model.rightArm.xRot = -.87266f;
				model.rightArm.yRot = -.78539f;
			}
		}
		else if(heldItem.getItem() instanceof RailgunItem)
		{
			if(right)
				model.rightArm.xRot = -.87266f;
			else
				model.leftArm.xRot = -.87266f;
		}
	}
}
