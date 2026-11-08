package net.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public class KeyMapping
{
	private final String description;

	public KeyMapping(String description, int key, Category category)
	{
		this.description = description;
	}

	public KeyMapping(String description, InputConstants.Type type, int key, Category category)
	{
		this.description = description;
	}

	public KeyMapping(String description, int key, String category)
	{
		this.description = description;
	}

	public KeyMapping(String description, InputConstants.Type type, int key, String category)
	{
		this.description = description;
	}

	public boolean isUnbound()
	{
		return false;
	}

	public boolean consumeClick()
	{
		return false;
	}

	public boolean isDown()
	{
		return false;
	}

	public boolean isDefault()
	{
		return false;
	}

	public boolean isActiveAndMatches(InputConstants.Key key)
	{
		return false;
	}

	public void setKeyConflictContext(IKeyConflictContext context)
	{
	}

	public String getName()
	{
		return description;
	}

	public Component getTranslatedKeyMessage()
	{
		return Component.literal(description);
	}

	public record Category(Identifier id)
	{
		public static final Category MISC = new Category(Identifier.withDefaultNamespace("misc"));

		public static Category register(Identifier id)
		{
			return new Category(id);
		}
	}
}
