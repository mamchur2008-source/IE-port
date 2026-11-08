/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.CrateItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageIncognitoSync(int entityID, boolean isIncognito) implements IMessage
{
	public static final Type<MessageIncognitoSync> ID = IMessage.createType("incognito_sync");
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageIncognitoSync> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MessageIncognitoSync::entityID,
			ByteBufCodecs.BOOL, MessageIncognitoSync::isIncognito,
			MessageIncognitoSync::new
	);

	public void process(IPayloadContext context)
	{
		if(context.flow().getReceptionSide()==LogicalSide.CLIENT)
			context.enqueueWork(() -> {
				Level world = ImmersiveEngineering.proxy.getClientWorld();
				if(world!=null) // This can happen if the task is scheduled right before leaving the world
				{
					if(isIncognito)
						CrateItem.incognitoPlayers.add(entityID);
					else
						CrateItem.incognitoPlayers.remove(entityID);
				}
			});
	}

	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
