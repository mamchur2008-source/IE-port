/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class TurretCallbacks implements BlockCallback<Unit>
{
	public static final TurretCallbacks INSTANCE = new TurretCallbacks();

	public Unit extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		return getDefaultKey();
	}

	public Unit getDefaultKey()
	{
		return Unit.INSTANCE;
	}

	private static final IEObjState STATE = new IEObjState(VisibilityList.show("base"));

	public IEObjState getIEOBJState(Unit unit)
	{
		return STATE;
	}
}
