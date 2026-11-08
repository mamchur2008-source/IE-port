/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;

import java.util.List;
import java.util.Set;

// Currently only used to register our custom injection point (CaptureOwner)
public class IEMixinConfig implements IMixinConfigPlugin
{
	public void onLoad(String mixinPackage)
	{
		InjectionInfo.register(CaptureOwnerInjectionInfo.class);
	}

	public String getRefMapperConfig()
	{
		return null;
	}

	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		return true;
	}

	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	public List<String> getMixins()
	{
		return null;
	}

	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
