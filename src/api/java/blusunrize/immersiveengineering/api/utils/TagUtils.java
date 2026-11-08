/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// TODO move a lot of RLs to TagKey's
// TODO make less alloc-y...
public class TagUtils
{
	private static List<Identifier> getTags(Reference<?> ref) {
		return ref.tags().map(TagKey::location).toList();
	}

	public static Collection<Identifier> getMatchingTagNames(RegistryAccess tags, ItemStack stack)
	{
		// TODO ideally get rid of the block part...
		Collection<Identifier> ret = new HashSet<>(getTags(stack.getItem().builtInRegistryHolder()));
		Block b = Block.byItem(stack.getItem());
		if(b!=Blocks.AIR)
			ret.addAll(getTags(b.builtInRegistryHolder()));
		return ret;
	}

	public static String[] getMatchingPrefixAndRemaining(RegistryAccess tags, ItemStack stack, String... componentTypes)
	{
		for(Identifier name : getMatchingTagNames(tags, stack))
		{
			for(String componentType : componentTypes)
				if(name.getPath().startsWith(componentType))
				{
					String material = name.getPath().substring(componentType.length());
					if(material.startsWith("/"))
						material = material.substring(1);
					if(material.length() > 0)
						return new String[]{componentType, material};
				}
		}
		return null;
	}

	// TODO do we actually need RegAccess here when using during arc recycling
	public static boolean isIngot(RegistryAccess tags, ItemStack stack)
	{
		var registry = tags.lookupOrThrow(Registries.ITEM);
		var tag = registry.get(Tags.Items.INGOTS);
		if (tag.isPresent())
			return tag.get().contains(Holder.direct(stack.getItem()));
		return false;
	}

	public static <T> Stream<T> elementStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, Identifier tag) {
		return holderStream(tags, registry, tag).map(Holder::value);
	}

	public static <T> Stream<T> elementStream(RegistryAccess tags, TagKey<T> key) {
		return holderStream(tags.lookupOrThrow(key.registry()), key).map(Holder::value);
	}

	public static <T> Stream<T> elementStream(Registry<T> registry, TagKey<T> tag) {
		return holderStream(registry, tag).map(Holder::value);
	}

	public static <T> Stream<Holder<T>> holderStream(RegistryAccess tags, ResourceKey<Registry<T>> registry, Identifier tag) {
		return holderStream(tags.lookupOrThrow(registry), TagKey.create(registry, tag));
	}

	public static <T> Stream<Holder<T>> holderStream(Registry<T> registry, TagKey<T> tag) {
		return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false);
	}

	public static TagKey<Item> createItemWrapper(Identifier name)
	{
		return TagKey.create(Registries.ITEM, name);
	}

	public static TagKey<Block> createBlockWrapper(Identifier name)
	{
		return TagKey.create(Registries.BLOCK, name);
	}

	public static TagKey<Fluid> createFluidWrapper(Identifier name)
	{
		return TagKey.create(Registries.FLUID, name);
	}

	public static TagKey<EntityType<?>> createEntityWrapper(Identifier name)
	{
		return TagKey.create(Registries.ENTITY_TYPE, name);
	}

	public static TagKey<Biome> createBiomeWrapper(Identifier name)
	{
		return TagKey.create(Registries.BIOME, name);
	}
}
