/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 03.10.2016
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID)
public class IEDefaultColourHandlers implements BlockTintSource
{
	public static IEDefaultColourHandlers INSTANCE = new IEDefaultColourHandlers();

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources ev)
	{
		for(BlockEntry<?> blockEntry : BlockEntry.ALL_ENTRIES)
		{
			Block block = blockEntry.get();
			if(block instanceof IColouredBlock colouredBlock&&colouredBlock.hasCustomBlockColours())
				ev.register(java.util.List.of(INSTANCE), block);
		}
	}

	public int color(BlockState state)
	{
		return colorInWorld(state, null, null);
	}

	public int colorInWorld(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable BlockPos pos)
	{
		if(state.getBlock() instanceof IColouredBlock colouredBlock)
			return colouredBlock.getRenderColour(state, worldIn, pos, 0)|0xff000000;
		return 0xffffff;
	}
}
