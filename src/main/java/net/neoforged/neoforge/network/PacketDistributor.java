package net.neoforged.neoforge.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class PacketDistributor
{
	public static void sendToAllPlayers(CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunk, CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload message, CustomPacketPayload... others)
	{
	}

	public static void sendToPlayersNear(
			ServerLevel level,
			ServerPlayer excluded,
			double x,
			double y,
			double z,
			double radius,
			CustomPacketPayload message,
			CustomPacketPayload... others
	)
	{
	}
}
