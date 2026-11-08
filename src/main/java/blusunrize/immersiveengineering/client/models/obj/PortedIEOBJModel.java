/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.client.ieobj.DefaultCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallbacks;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.client.models.split.PolygonUtils;
import blusunrize.immersiveengineering.client.models.split.PolygonUtils.ExtraQuadData;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import malte0811.modelsplitter.model.Group;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.CALLBACKS_KEY;
import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.DYNAMIC_KEY;
import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.LAYERS_KEY;
import static blusunrize.immersiveengineering.client.models.obj.IEOBJLoader.MODEL_KEY;

public record PortedIEOBJModel(
		OBJModel<OBJMaterial> baseModel,
		IEOBJCallback<?> callback,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();

	@Override
	public TextureSlots.Data textureSlots()
	{
		return textureSlots;
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		return bakeForCallback(cast(callback), cast(callback).getDefaultKey(), textureSlots, modelBaker, modelState, name);
	}

	public DynamicModel.BakedDynamicModel bakeStandaloneModel(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		return makeStandaloneModel(cast(callback), textureSlots, modelBaker, modelState, name);
	}

	private <T> QuadCollection bakeForCallback(
			IEOBJCallback<T> callback, T key, TextureSlots textureSlots, ModelBaker modelBaker,
			ModelState modelState, ModelDebugName name
	)
	{
		IEObjState state = callback.getIEOBJState(key);
		List<BakedQuad> quads = new ArrayList<>();
		TextureCoordinateRemapper coordinateRemapper = new TextureCoordinateRemapper(null);

		for(Entry<String, Group<OBJMaterial>> entry : baseModel.getFacesByGroup().entrySet())
		{
			String groupName = entry.getKey();
			if(!state.visibility().isVisible(groupName)||!callback.shouldRenderGroup(key, groupName, null))
				continue;

			Transformation groupTransform = callback.applyTransformations(key, groupName, modelState.transformation());
			Transformation transform = state.transform().compose(groupTransform.blockCenterToCorner());
			for(Polygon<OBJMaterial> face : entry.getValue().getFaces())
			{
				OBJMaterial material = face.getTexture();
				if(material==null)
					continue;
				TextureAtlasSprite sprite = getTexture(callback, key, groupName, material, textureSlots, modelBaker, name);
				Color4 color = callback.getRenderColor(key, groupName, material.name(), (ShaderCase)null, Color4.WHITE);
				Polygon<OBJMaterial> remappedFace = coordinateRemapper.remapCoord(face);
				if(remappedFace!=null)
				{
					boolean shade = callback.shadeQuads(key, material.name());
					quads.add(PolygonUtils.toBakedQuad(
							remappedFace.getPoints(), new ExtraQuadData(sprite, color, shade), transform,
							callback.useAbsoluteUV(key, material.name()), shade
					));
				}
			}
		}

		QuadCollection.Builder result = new QuadCollection.Builder();
		for(BakedQuad quad : quads)
			result.addUnculledFace(quad);
		return result.build();
	}

	public QuadCollection bakeForKey(
			Object key, TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		return bakeForCallback(cast(callback), key, textureSlots, modelBaker, modelState, name);
	}

	private <T> DynamicModel.BakedDynamicModel makeStandaloneModel(
			IEOBJCallback<T> callback, TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState,
			ModelDebugName name
	)
	{
		return new StandaloneModel<>(callback, textureSlots, modelBaker, modelState, name);
	}

	private class StandaloneModel<T> implements DynamicModel.BakedDynamicModel
	{
		private final IEOBJCallback<T> callback;
		private final TextureSlots textureSlots;
		private final ModelBaker modelBaker;
		private final ModelState modelState;
		private final ModelDebugName name;
		private final ModelProperty<T> keyProperty;

		private StandaloneModel(
				IEOBJCallback<T> callback, TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState,
				ModelDebugName name
		)
		{
			this.callback = callback;
			this.textureSlots = textureSlots;
			this.modelBaker = modelBaker;
			this.modelState = modelState;
			this.name = name;
			this.keyProperty = IEOBJCallbacks.getModelProperty(callback);
		}

		public List<BakedQuad> getQuads(BlockState state, ModelData extraData, RenderType renderType)
		{
			T key = extraData.has(keyProperty)?extraData.get(keyProperty): callback.getDefaultKey();
			return bakeForCallback(callback, key, textureSlots, modelBaker, modelState, name).getQuads(null);
		}
	}

	private <T> TextureAtlasSprite getTexture(
			IEOBJCallback<T> callback, T key, String groupName, OBJMaterial objMaterial, TextureSlots textureSlots,
			ModelBaker modelBaker, ModelDebugName name
	)
	{
		TextureAtlasSprite replacement = callback.getTextureReplacement(key, groupName, objMaterial.name());
		if(replacement!=null)
			return replacement;
		String materialName = objMaterial.map_Kd();
		Material.Baked material;
		if(materialName!=null&&materialName.indexOf(':') >= 0)
			material = modelBaker.materials().get(new Material(Identifier.parse(materialName)), name);
		else
			material = modelBaker.materials().resolveSlot(textureSlots, materialName, name);
		return material.sprite();
	}

	@SuppressWarnings("unchecked")
	private static <T> IEOBJCallback<T> cast(IEOBJCallback<?> callback)
	{
		return (IEOBJCallback<T>)callback;
	}

	public static class Loader implements UnbakedModelLoader<PortedIEOBJModel>
	{
		@Override
		public PortedIEOBJModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			Identifier modelLoc = toRL(modelContents.get(MODEL_KEY).getAsString(), null);
			try(InputStream input = getStream(modelLoc))
			{
				OBJModel<OBJMaterial> model = OBJModel.readFromStream(input, s -> getStream(toRL(s, modelLoc)))
						.quadify()
						.recomputeZeroNormals();
				IEOBJCallback<?> callback = DefaultCallback.INSTANCE;
				if(modelContents.has(CALLBACKS_KEY))
				{
					Identifier callbackName = Identifier.parse(modelContents.get(CALLBACKS_KEY).getAsString());
					IEOBJCallback<?> registered = IEOBJCallbacks.getCallback(callbackName);
					if(registered!=null)
						callback = registered;
				}
				TextureSlots.Data textures = TextureSlots.parseTextureMap(collectTextureSlots(modelContents, model));
				ItemTransforms transforms = modelContents.has("display")?
						context.deserialize(modelContents.get("display"), ItemTransforms.class):
						null;
				Identifier parent = modelContents.has("parent")?
						Identifier.parse(modelContents.get("parent").getAsString()):
						null;
				// These keys are still parsed by datagen. The first scoped renderer only needs the default model.
				ignoreLegacyKeys(modelContents);
				return new PortedIEOBJModel(model, callback, textures, transforms, parent);
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		private static void ignoreLegacyKeys(JsonObject modelContents)
		{
			if(modelContents.has(DYNAMIC_KEY))
				modelContents.get(DYNAMIC_KEY);
			if(modelContents.has(LAYERS_KEY))
				for(JsonElement ignored : modelContents.getAsJsonArray(LAYERS_KEY))
					ignored.getAsString();
		}

		private static JsonObject collectTextureSlots(JsonObject modelContents, OBJModel<OBJMaterial> model)
		{
			JsonObject textures = modelContents.has("textures")?
					modelContents.getAsJsonObject("textures").deepCopy():
					new JsonObject();
			for(String texture : getMaterialTextures(model))
				if(texture!=null&&!texture.isBlank()&&texture.charAt(0)!='#'&&!textures.has(texture))
					textures.addProperty(texture, texture);
			return textures;
		}

		private static Set<String> getMaterialTextures(OBJModel<OBJMaterial> model)
		{
			Set<String> result = new LinkedHashSet<>();
			for(Group<OBJMaterial> group : model.getFacesByGroup().values())
				for(Polygon<OBJMaterial> face : group.getFaces())
				{
					OBJMaterial material = face.getTexture();
					if(material!=null)
						result.add(material.map_Kd());
				}
			return result;
		}

		private static Identifier toRL(String name, @Nullable Identifier basePath)
		{
			if(name.contains(":"))
				return Identifier.parse(name);
			else if(basePath!=null)
			{
				String baseDir = basePath.getPath().substring(0, basePath.getPath().lastIndexOf('/')+1);
				return basePath.withPath(baseDir+name);
			}
			else
				return ImmersiveEngineering.rl(name);
		}

		private static InputStream getStream(Identifier path)
		{
			try
			{
				return Minecraft.getInstance().getResourceManager().getResource(path).orElseThrow().open();
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
