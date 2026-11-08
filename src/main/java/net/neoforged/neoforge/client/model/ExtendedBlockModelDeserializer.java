package net.neoforged.neoforge.client.model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class ExtendedBlockModelDeserializer
{
	public static final ExtendedBlockModelDeserializer INSTANCE = new ExtendedBlockModelDeserializer();
	private final Gson gson = new Gson();

	public <T> T fromJson(JsonElement json, Class<T> type)
	{
		return gson.fromJson(json, type);
	}
}
