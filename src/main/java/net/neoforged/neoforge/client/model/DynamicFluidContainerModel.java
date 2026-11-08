package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public class DynamicFluidContainerModel
{
	public static class Loader
	{
		public static final Loader INSTANCE = new Loader();

		public IUnbakedGeometry<?> read(JsonObject modelContents, JsonDeserializationContext context)
		{
			return (owner, bakery, spriteGetter, modelState, overrides) -> bakery==null?null: null;
		}
	}
}
