package blusunrize.immersiveengineering.common.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.fluids.FluidUtil;

public class CapabilityCompat
{
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> T getItemCapability(ItemStack stack, ItemCapability capability)
	{
		if(capability==Capabilities.Fluid.ITEM)
			return (T)FluidUtil.getFluidHandler(stack).orElse(null);
		return (T)stack.getCapability(capability, null);
	}
}
