/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Loader;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Type;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class SideConfigBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T>
{
	public static <T extends ModelBuilder<T>>
	SideConfigBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper)
	{
		return new SideConfigBuilder<>(parent, existingFileHelper);
	}

	protected SideConfigBuilder(T parent, ExistingFileHelper existingFileHelper)
	{
		super(Loader.NAME, parent, existingFileHelper, false);
	}

	private Type type;
	private Identifier baseName;

	public SideConfigBuilder<T> type(Type type)
	{
		Preconditions.checkNotNull(type);
		Preconditions.checkState(this.type==null);
		this.type = type;
		return this;
	}

	public SideConfigBuilder<T> baseName(Identifier baseName)
	{
		Preconditions.checkNotNull(baseName);
		Preconditions.checkState(this.baseName==null);
		this.baseName = baseName;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json)
	{
		json = super.toJson(json);
		json.addProperty("type", type.getName());
		json.addProperty("base_name", baseName.toString());
		JsonObject textures = json.has("textures")?json.getAsJsonObject("textures"): new JsonObject();
		for(Direction side : DirectionUtils.VALUES)
			for(IOSideConfig cfg : IOSideConfig.values())
			{
				Identifier texture = baseName.withSuffix("_"+textureName(type, side, cfg));
				if(!textures.has(texture.toString()))
					textures.addProperty(texture.toString(), texture.toString());
			}
		Identifier particle = baseName.withSuffix("_"+textureName(type, Direction.DOWN, IOSideConfig.NONE));
		if(!textures.has("particle"))
			textures.addProperty("particle", particle.toString());
		json.add("textures", textures);
		return json;
	}

	private static String textureName(Type type, Direction side, IOSideConfig cfg)
	{
		String sideName = switch(type)
		{
			case SIDE_TOP_BOTTOM -> side.getAxis()==Direction.Axis.Y?side.getSerializedName(): "side";
			case SIDE_VERTICAL -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case VERTICAL -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case ALL_SAME_TEXTURE -> "side";
		};
		if(type==Type.VERTICAL&&side.getAxis()!=Direction.Axis.Y)
			return sideName;
		return sideName+"_"+cfg.getTextureName();
	}
}
