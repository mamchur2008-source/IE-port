/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;

import java.util.Map;

import static net.minecraft.world.item.equipment.ArmorType.*;

public class IEArmorMaterials
{
	private static final ResourceKey<EquipmentAsset> FARADAY_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, IEApi.ieLoc("faraday")
	);
	private static final ResourceKey<EquipmentAsset> STEEL_ASSET = ResourceKey.create(
			EquipmentAssets.ROOT_ID, IEApi.ieLoc("steel")
	);

	public static final ArmorMaterial FARADAY = new ArmorMaterial(
			1,
			Map.of(BOOTS, 1, HELMET, 1, LEGGINGS, 2, CHESTPLATE, 3, BODY, 3),
			1,
			SoundEvents.ARMOR_EQUIP_CHAIN,
			0,
			0,
			IETags.getTagsFor(EnumMetals.ALUMINUM).plate,
			FARADAY_ASSET
	);
	public static final ArmorMaterial STEEL = new ArmorMaterial(
			21,
			Map.of(BOOTS, 2, HELMET, 2, LEGGINGS, 6, CHESTPLATE, 7, BODY, 7),
			10,
			SoundEvents.ARMOR_EQUIP_IRON,
			0,
			0,
			IETags.getTagsFor(EnumMetals.STEEL).ingot,
			STEEL_ASSET
	);

	public static void init(IEventBus modBus)
	{
	}

	public static Item.Properties getProperties(ArmorMaterial material, ArmorType type)
	{
		return IEItems.defaultProperties().humanoidArmor(material, type);
	}

	public static int getDurability(ArmorMaterial material, ArmorType type)
	{
		if(material==STEEL)
		{
			return switch(type)
			{
				case BOOTS -> 273;
				case LEGGINGS -> 315;
				case CHESTPLATE -> 336;
				case HELMET -> 231;
				case BODY -> throw new UnsupportedOperationException("Steel body armor not implemented");
			};
		}
		else if(material==FARADAY)
		{
			return switch(type)
			{
				case BOOTS -> 13;
				case LEGGINGS -> 15;
				case CHESTPLATE -> 16;
				case HELMET -> 11;
				case BODY -> throw new UnsupportedOperationException("Faraday body armor not implemented");
			};
		}
		else
			throw new UnsupportedOperationException("Unknown armor material "+material);
	}
}
