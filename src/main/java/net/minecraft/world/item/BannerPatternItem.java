package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item
{
	public BannerPatternItem(TagKey<BannerPattern> tag, Properties properties)
	{
		super(properties);
	}
}
