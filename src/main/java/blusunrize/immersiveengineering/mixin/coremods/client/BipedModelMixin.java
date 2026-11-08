/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.client.render.IEBipedRotations;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class BipedModelMixin<T extends HumanoidRenderState>
{
	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("RETURN"))
	public void rotationAngleCallback(
			T renderState, CallbackInfo info
	)
	{
		//noinspection ConstantConditions (IntelliJ warns about the (admittedly crazy-looking) cast)
		IEBipedRotations.handleBipedRotations((HumanoidModel<?>)(Object)this, renderState);
	}
}
