/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.Collectors;

public class IESaveData extends SavedData
{
	private static IESaveData INSTANCE;
	public static final String dataName = "ImmersiveEngineering-SaveData";
	public static final SavedDataType<IESaveData> TYPE = new SavedDataType<>(
			IEApi.ieLoc("save_data"), ignored -> new IESaveData(), IESaveData::codec
	);

	public IESaveData()
	{
		super();
	}

	public IESaveData(CompoundTag nbt, Provider provider)
	{
		this();
		ListTag dimensionList = nbt.getListOrEmpty("mineralVeins");
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			ExcavatorHandler.getMineralVeinList().clear();
			for(int i = 0; i < dimensionList.size(); i++)
			{
				CompoundTag dimTag = dimensionList.getCompoundOrEmpty(i);
				Identifier rl = Identifier.parse(dimTag.getStringOr("dimension", "minecraft:overworld"));
				ResourceKey<Level> dimensionType = ResourceKey.create(Registries.DIMENSION, rl);
				ListTag mineralList = dimTag.getListOrEmpty("veins");

				ExcavatorHandler.getMineralVeinList().
						putAll(dimensionType, mineralList.stream()
								.map(inbt -> MineralVein.readFromNBT((CompoundTag)inbt))
								.collect(Collectors.toList()));
			}
			ExcavatorHandler.resetCache();
		}

		ListTag receivedShaderList = nbt.getListOrEmpty("receivedShaderList");
		for(int i = 0; i < receivedShaderList.size(); i++)
		{
			CompoundTag tag = receivedShaderList.getCompoundOrEmpty(i);
			UUID player = tag.read("player", UUIDUtil.CODEC).orElse(null);
			if(player==null)
				continue;
			ShaderRegistry.receivedShaders.get(player).clear();

			ListTag playerReceived = tag.getListOrEmpty("received");
			for(int j = 0; j < playerReceived.size(); j++)
			{
				String s = playerReceived.getString(j).orElse("");
				if(!s.isEmpty())
					ShaderRegistry.receivedShaders.put(player, Identifier.parse(s));
			}
		}
	}

	public static Codec<IESaveData> codec(@Nullable ServerLevel level)
	{
		Provider provider = level!=null?level.registryAccess(): RegistryAccess.EMPTY;
		return CompoundTag.CODEC.xmap(
				tag -> new IESaveData(tag, provider),
				data -> data.save(new CompoundTag(), provider)
		);
	}

	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag nbt, Provider provider)
	{
		ListTag dimensionList = new ListTag();
		synchronized(ExcavatorHandler.getMineralVeinList())
		{
			for(ResourceKey<Level> dimension : ExcavatorHandler.getMineralVeinList().keySet())
			{
				CompoundTag dimTag = new CompoundTag();
				dimTag.putString("dimension", dimension.identifier().toString());
				ListTag mineralList = new ListTag();
				for(MineralVein mineralVein : ExcavatorHandler.getMineralVeinList().get(dimension))
					mineralList.add(mineralVein.writeToNBT());
				dimTag.put("veins", mineralList);
				dimensionList.add(dimTag);
			}
		}
		nbt.put("mineralVeins", dimensionList);


		ListTag receivedShaderList = new ListTag();
		for(UUID player : ShaderRegistry.receivedShaders.keySet())
		{
			CompoundTag tag = new CompoundTag();
			tag.store("player", UUIDUtil.CODEC, player);
			ListTag playerReceived = new ListTag();
			for(Identifier shader : ShaderRegistry.receivedShaders.get(player))
				if(shader!=null)
					playerReceived.add(StringTag.valueOf(shader.toString()));
			tag.put("received", playerReceived);
			receivedShaderList.add(tag);
		}
		nbt.put("receivedShaderList", receivedShaderList);

		return nbt;
	}


	public static void markInstanceDirty()
	{
		if(INSTANCE!=null)
			INSTANCE.setDirty();
	}

	public static void setInstance(IESaveData in)
	{
		INSTANCE = in;
	}

}
