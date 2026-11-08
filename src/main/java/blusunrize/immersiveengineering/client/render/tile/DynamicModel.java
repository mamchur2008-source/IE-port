/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.models.obj.PortedIEOBJModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.model.data.ModelData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DynamicModel
{
	private static final List<DynamicModel> MODELS = new ArrayList<>();
	private static final BakedDynamicModel EMPTY_MODEL = ($state, $data, $renderType) -> List.of();

	public static void registerModels(ModelEvent.RegisterStandalone ev)
	{
		for(DynamicModel model : MODELS)
			ev.register(
					model.key,
					new SimpleUnbakedStandaloneModel<>(model.modelId, DynamicModel::bakeStandaloneModel)
			);
	}

	private final Identifier modelId;
	private final StandaloneModelKey<BakedDynamicModel> key;

	public DynamicModel(String desc)
	{
		this.modelId = IEApi.ieLoc("dynamic/"+desc);
		this.key = new StandaloneModelKey<>(() -> modelId+"#standalone");
		MODELS.add(this);
	}

	public BakedDynamicModel get()
	{
		Object modelManager = Minecraft.getInstance().getModelManager();
		try
		{
			Method getStandaloneModel = modelManager.getClass().getMethod("getStandaloneModel", StandaloneModelKey.class);
			Object model = getStandaloneModel.invoke(modelManager, key);
			if(model instanceof BakedDynamicModel bakedModel)
				return bakedModel;
		} catch(ReflectiveOperationException|LinkageError ignored)
		{
		}
		return EMPTY_MODEL;
	}

	private static BakedDynamicModel bakeStandaloneModel(ResolvedModel model, ModelBaker baker, ModelDebugName name)
	{
		TextureSlots textureSlots = model.getTopTextureSlots();
		if(model.wrapped() instanceof PortedIEOBJModel ieobjModel)
			return ieobjModel.bakeStandaloneModel(textureSlots, baker, BlockModelRotation.IDENTITY, name);
		QuadCollection quadCollection = model.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
		return ($state, $data, $renderType) -> quadCollection.getQuads(null);
	}

	public List<BakedQuad> getNullQuads()
	{
		return getNullQuads(ModelData.EMPTY);
	}

	public List<BakedQuad> getNullQuads(ModelData data)
	{
		return get().getQuads(null, data, null);
	}

	public Identifier getName()
	{
		return modelId;
	}

	public interface BakedDynamicModel
	{
		List<BakedQuad> getQuads(BlockState state, ModelData extraData, RenderType renderType);
	}
}
