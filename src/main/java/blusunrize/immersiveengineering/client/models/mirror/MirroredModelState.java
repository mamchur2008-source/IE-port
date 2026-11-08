/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.mirror;

import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.renderer.block.dispatch.ModelState;

import javax.annotation.Nonnull;

public class MirroredModelState implements ModelState
{
	private static final Transformation MIRRORED_IDENTITY = new Transformation(
			null, null, new Vector3f(-1, 1, 1), null
	);
	private final ModelState inner;
	private final Transformation mirroredMainRotation;

	public MirroredModelState(ModelState inner)
	{
		this.inner = inner;
		this.mirroredMainRotation = mirror(inner.transformation());
	}

	@Nonnull
	public Transformation transformation()
	{
		return mirroredMainRotation;
	}

	public boolean isUvLocked()
	{
		return false;
	}

	private static Transformation mirror(Transformation in)
	{
		return in.compose(MIRRORED_IDENTITY);
	}
}
