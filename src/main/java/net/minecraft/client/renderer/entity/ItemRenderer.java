package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRenderer
{
	private static final BakedModel MISSING = new SimpleBakedModel();

	public BakedModel getModel(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed)
	{
		return MISSING;
	}

	public void renderStatic(
			ItemStack stack,
			ItemDisplayContext displayContext,
			int packedLight,
			int packedOverlay,
			PoseStack poseStack,
			MultiBufferSource buffers,
			@Nullable Level level,
			int seed
	)
	{
	}

	public void render(
			ItemStack stack,
			ItemDisplayContext displayContext,
			boolean leftHand,
			PoseStack poseStack,
			MultiBufferSource buffers,
			int packedLight,
			int packedOverlay,
			BakedModel model
	)
	{
	}

	public void renderModelLists(
			BakedModel model,
			ItemStack stack,
			int packedLight,
			int packedOverlay,
			PoseStack poseStack,
			VertexConsumer consumer
	)
	{
	}

	public void renderQuadList(
			PoseStack poseStack,
			VertexConsumer consumer,
			List<BakedQuad> quads,
			ItemStack stack,
			int packedLight,
			int packedOverlay
	)
	{
	}
}
