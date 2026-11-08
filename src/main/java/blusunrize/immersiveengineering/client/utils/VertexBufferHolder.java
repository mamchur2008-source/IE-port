package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID)
public class VertexBufferHolder implements IVertexBufferHolder
{
	private final ResettableLazy<Renderer> renderer;

	private static VertexBufferHolder forQuads(Supplier<List<BakedQuad>> quads)
	{
		final ResettableLazy<List<BakedQuad>> cachedQuads = new ResettableLazy<>(quads);
		return new VertexBufferHolder(new Renderer()
		{
			@Override
			public void render(com.mojang.blaze3d.vertex.VertexConsumer builder, PoseStack transform, int light, int overlay)
			{
				QuadInstance instance = new QuadInstance();
				instance.setColor(0xffffffff);
				instance.setLightCoords(light);
				instance.setOverlayCoords(overlay);
				for(BakedQuad quad : cachedQuads.get())
					builder.putBakedQuad(transform.last(), quad, instance);
			}

			@Override
			public void reset()
			{
				cachedQuads.reset();
			}
		});
	}

	private VertexBufferHolder(Renderer renderer)
	{
		this.renderer = new ResettableLazy<>(() -> renderer, Renderer::reset);
	}

	public static void addToAPI()
	{
		IVertexBufferHolder.CREATE.setValue(new VertexBufferHolderFactory()
		{
			public IVertexBufferHolder create(Renderer renderer)
			{
				return new VertexBufferHolder(renderer);
			}

			public IVertexBufferHolder apply(Supplier<List<BakedQuad>> quads)
			{
				return forQuads(quads);
			}
		});
	}

	public void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform, boolean inverted)
	{
		renderer.get().render(directOut.getBuffer(type), transform, light, overlay);
	}

	public void reset()
	{
		renderer.reset();
	}
}
