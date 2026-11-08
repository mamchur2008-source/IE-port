/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.ChunkLoaderLogic.State;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class ChunkLoaderCallbacks implements BlockCallback<Boolean>
{
	public static final ChunkLoaderCallbacks INSTANCE = new ChunkLoaderCallbacks();

	public Boolean extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState blockState, BlockEntity blockEntity)
	{
		if(blockEntity instanceof IMultiblockBE<?> multiblockBE&&
				multiblockBE.getHelper().getState() instanceof State state)
			return state.renderAsActive;
		return getDefaultKey();
	}

	public Boolean getDefaultKey()
	{
		return false;
	}

	public boolean dependsOnLayer()
	{
		return true;
	}

	public boolean shouldRenderGroup(Boolean paper, String group, RenderType layer)
	{
		if(layer==null)
			return !"glass".equals(group);
		if("glass".equals(group))
			return layer==blusunrize.immersiveengineering.client.utils.RenderTypeCompat.translucent();
		if("amethyst".equals(group))
			return layer==blusunrize.immersiveengineering.client.utils.RenderTypeCompat.cutout();
		if("paper".equals(group))
			return paper&&layer==blusunrize.immersiveengineering.client.utils.RenderTypeCompat.cutout();
		return layer==blusunrize.immersiveengineering.client.utils.RenderTypeCompat.solid();
	}
}
