package net.neoforged.neoforge.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public interface IGeometryLoader<T>
{
	T read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
