/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import com.mojang.datafixers.util.Unit;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class BottlingMachineCallbacks implements BlockCallback<Unit>
{
	public static final BottlingMachineCallbacks INSTANCE = new BottlingMachineCallbacks();

	public Unit extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return getDefaultKey();
	}

	public Unit getDefaultKey()
	{
		return Unit.INSTANCE;
	}

	public boolean dependsOnLayer()
	{
		return true;
	}

	public boolean shouldRenderGroup(Unit object, String group, RenderType layer)
	{
		return "glass".equals(group)==(blusunrize.immersiveengineering.client.utils.RenderTypeCompat.translucent()==layer);
	}
}
