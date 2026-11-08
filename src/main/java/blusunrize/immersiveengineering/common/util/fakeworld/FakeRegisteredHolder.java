/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record FakeRegisteredHolder<T>(
		T value, ResourceKey<T> key
) implements Holder<T>
{
	public boolean isBound()
	{
		return true;
	}

	public boolean areComponentsBound()
	{
		return true;
	}

	public boolean is(Identifier p_205713_)
	{
		return this.key.identifier().equals(p_205713_);
	}

	public boolean is(ResourceKey<T> p_205712_)
	{
		return this.key==p_205712_;
	}

	public boolean is(Predicate<ResourceKey<T>> p_205711_)
	{
		return p_205711_.test(this.key);
	}

	public boolean is(TagKey<T> p_205705_)
	{
		return false;
	}

	public boolean is(Holder<T> p_316447_)
	{
		// TODO is this right? Is this even critical?
		return p_316447_==this;
	}

	public Stream<TagKey<T>> tags()
	{
		return Stream.empty();
	}

	public DataComponentMap components()
	{
		return DataComponentMap.EMPTY;
	}

	public Either<ResourceKey<T>, T> unwrap()
	{
		return Either.right(value);
	}

	public Optional<ResourceKey<T>> unwrapKey()
	{
		return Optional.of(key);
	}

	public Kind kind()
	{
		return Kind.DIRECT;
	}

	public boolean canSerializeIn(HolderOwner<T> p_255833_)
	{
		return false;
	}
}
