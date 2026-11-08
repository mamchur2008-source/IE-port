/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.mirror;

import blusunrize.immersiveengineering.client.utils.ModelUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record PortedMirroredModel(
		UnbakedModel innerModel,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();

	@Override
	public TextureSlots.Data textureSlots()
	{
		if(textureSlots.values().isEmpty())
			return innerModel.textureSlots();
		if(innerModel.textureSlots().values().isEmpty())
			return textureSlots;
		Map<String, TextureSlots.SlotContents> mergedSlots = new HashMap<>(innerModel.textureSlots().values());
		mergedSlots.putAll(textureSlots.values());
		return new TextureSlots.Data(mergedSlots);
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		UnbakedGeometry innerGeometry = innerModel.geometry();
		if(innerGeometry==null)
			return QuadCollection.EMPTY;
		return reverseQuads(innerGeometry.bake(textureSlots, modelBaker, new MirroredModelState(modelState), name));
	}

	private static QuadCollection reverseQuads(QuadCollection original)
	{
		QuadCollection.Builder mirrored = new QuadCollection.Builder();
		for(BakedQuad quad : original.getQuads(null))
			mirrored.addUnculledFace(ModelUtils.reverseOrder(quad));
		for(Direction direction : Direction.values())
			for(BakedQuad quad : original.getQuads(direction))
				mirrored.addCulledFace(mirrorDirection(direction), ModelUtils.reverseOrder(quad));
		return mirrored.build();
	}

	private static Direction mirrorDirection(Direction direction)
	{
		return switch(direction)
		{
			case EAST -> Direction.WEST;
			case WEST -> Direction.EAST;
			default -> direction;
		};
	}

	public static class Loader implements UnbakedModelLoader<PortedMirroredModel>
	{
		@Override
		public PortedMirroredModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			UnbakedModel innerModel = context.deserialize(
					modelContents.get(MirroredModelLoader.INNER_MODEL), UnbakedModel.class
			);
			TextureSlots.Data textures = modelContents.has("textures")?
					TextureSlots.parseTextureMap(modelContents.getAsJsonObject("textures")):
					TextureSlots.Data.EMPTY;
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			return new PortedMirroredModel(innerModel, textures, transforms, parent);
		}
	}
}
