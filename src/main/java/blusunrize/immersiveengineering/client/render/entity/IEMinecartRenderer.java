package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.common.entities.IEMinecartEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;

public class IEMinecartRenderer<T extends IEMinecartEntity<?>> extends IEEntityRenderer<T>
{
	public IEMinecartRenderer(Context renderManagerIn, ModelLayerLocation layer)
	{
		super(renderManagerIn);
	}

	public static <T extends IEMinecartEntity<?>>
	EntityRendererProvider<T> provide(ModelLayerLocation layer)
	{
		return ctx -> new IEMinecartRenderer<>(ctx, layer);
	}
}
