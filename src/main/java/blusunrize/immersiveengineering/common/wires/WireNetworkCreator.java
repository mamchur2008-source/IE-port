/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.proxy.DefaultProxyProvider;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.stream.Stream;

public class WireNetworkCreator
{
	public static final IAttachmentSerializer<GlobalWireNetwork> SERIALIZER = new Serializer();
	public static final Function<IAttachmentHolder, GlobalWireNetwork> CREATOR = holder -> {
		if(holder instanceof Level level)
			return new GlobalWireNetwork(
					level.isClientSide(), new DefaultProxyProvider(level), new WireSyncManager(level)
			);
		else
			throw new RuntimeException("Wire networks should only ever be attached to levels, got "+holder);
	};
	public static GlobalWireNetwork getOrCreateNetwork(Level level)
	{
		return level.getData(IEDataAttachments.WIRE_NETWORK.get());
	}

	private static class Serializer implements IAttachmentSerializer<GlobalWireNetwork>
	{
		private static final String NETWORK_KEY = "network";

		public boolean write(GlobalWireNetwork attachment, ValueOutput output)
		{
			CompoundTag savedNBT = attachment.save(new CompoundTag(), Provider.create(Stream.empty()));
			if(savedNBT.isEmpty())
				return false;
			output.store(NETWORK_KEY, CompoundTag.CODEC, savedNBT);
			return true;
		}

		@Nonnull
		public GlobalWireNetwork read(@Nonnull IAttachmentHolder holder, @Nonnull ValueInput input)
		{
			GlobalWireNetwork network = CREATOR.apply(holder);
			input.read(NETWORK_KEY, CompoundTag.CODEC).ifPresent(network::readFromNBT);
			return network;
		}
	}
}
