package net.minecraft.resources;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ResourceLocation(String namespace, String path) implements Comparable<ResourceLocation>
{
	public static final Codec<ResourceLocation> CODEC = Identifier.CODEC.xmap(ResourceLocation::fromIdentifier, ResourceLocation::toIdentifier);
	public static final StreamCodec<ByteBuf, ResourceLocation> STREAM_CODEC = Identifier.STREAM_CODEC.map(ResourceLocation::fromIdentifier, ResourceLocation::toIdentifier);

	public static ResourceLocation fromNamespaceAndPath(String namespace, String path)
	{
		return new ResourceLocation(namespace, path);
	}

	public static ResourceLocation fromIdentifier(Identifier id)
	{
		return new ResourceLocation(id.getNamespace(), id.getPath());
	}

	public Identifier toIdentifier()
	{
		return Identifier.fromNamespaceAndPath(namespace, path);
	}

	public static ResourceLocation parse(String serialized)
	{
		Identifier id = Identifier.parse(serialized);
		return new ResourceLocation(id.getNamespace(), id.getPath());
	}

	public String getNamespace()
	{
		return namespace;
	}

	public String getPath()
	{
		return path;
	}

	@Override
	public String toString()
	{
		return namespace+":"+path;
	}

	@Override
	public int compareTo(ResourceLocation other)
	{
		return toString().compareTo(other.toString());
	}
}
