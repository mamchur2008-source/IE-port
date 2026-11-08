package blusunrize.immersiveengineering.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

public class ShaderMinecartRenderer<T extends AbstractMinecart>
{
	public static Int2ObjectMap<Identifier> shadedCarts = new Int2ObjectOpenHashMap<>();

	public static void render(
			Object baseModel,
			AbstractMinecart entity,
			float entityYaw,
			float partialTicks,
			PoseStack poseStack,
			MultiBufferSource buffer,
			int packedLight
	)
	{
	}
}
