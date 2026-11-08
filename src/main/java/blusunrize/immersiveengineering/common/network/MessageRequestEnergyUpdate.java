/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.api.utils.FastEither;
import blusunrize.immersiveengineering.common.items.VoltmeterItem.RemoteEnergyData;
import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public record MessageRequestEnergyUpdate(Either<BlockPos, Integer> pos) implements IMessage
{
	public static final Type<MessageRequestEnergyUpdate> ID = IMessage.createType("request_energy_update");
	public static final StreamCodec<ByteBuf, MessageRequestEnergyUpdate> CODEC = ByteBufCodecs.either(
			BlockPos.STREAM_CODEC, ByteBufCodecs.INT
	).map(MessageRequestEnergyUpdate::new, MessageRequestEnergyUpdate::pos);

	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
			Level level = context.player().level();
			EnergyHandler storage = null;
			RemoteEnergyData data = null;
			if(pos.left().isPresent())
				storage = getBlockEnergy(level, pos.left().get());
			else if(pos.right().isPresent())
			{
				Entity entity = level.getEntity(pos.right().get());
				if(entity!=null)
					storage = entity.getCapability(Energy.ENTITY, null);
			}
			if(storage!=null&&storage.getCapacityAsInt() > 0)
				data = new RemoteEnergyData(
						pos, level.getGameTime(), true, storage.getAmountAsInt(), storage.getCapacityAsInt()
				);
			if(data==null)
				data = new RemoteEnergyData(pos, level.getGameTime(), false, 0, 0);
			PacketDistributor.sendToPlayer(IMessage.serverPlayer(context), new MessageStoredEnergy(data));
		});
	}

	private static EnergyHandler getBlockEnergy(Level level, BlockPos pos)
	{
		EnergyHandler storage = level.getCapability(Energy.BLOCK, pos, null);
		if(storage!=null&&storage.getCapacityAsInt() > 0)
			return storage;
		for(Direction direction : Direction.values())
		{
			storage = level.getCapability(Energy.BLOCK, pos, direction);
			if(storage!=null&&storage.getCapacityAsInt() > 0)
				return storage;
		}
		return null;
	}

	public static FastEither<BlockPos, Integer> readPos(FriendlyByteBuf buf)
	{
		if(buf.readBoolean())
			return FastEither.right(buf.readInt());
		else
			return FastEither.left(buf.readBlockPos());
	}

	public static void writePos(FriendlyByteBuf out, FastEither<BlockPos, Integer> pos)
	{
		out.writeBoolean(pos.isRight());
		if(pos.isRight())
			out.writeInt(pos.rightNonnull());
		else
			out.writeBlockPos(pos.leftNonnull());
	}

	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
