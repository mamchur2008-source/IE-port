package net.minecraft.world.item;

public class ElytraItem extends Item
{
	public ElytraItem(Properties properties)
	{
		super(properties);
	}

	public static boolean isFlyEnabled(ItemStack stack)
	{
		return true;
	}
}
