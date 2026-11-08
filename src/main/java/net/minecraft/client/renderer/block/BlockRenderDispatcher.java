package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

public class BlockRenderDispatcher
{
	private final BakedModel missing = new SimpleBakedModel();
	private final BlockModelShaper blockModelShaper = new BlockModelShaper();
	private final ModelRenderer modelRenderer = new ModelRenderer();

	public BakedModel getBlockModel(BlockState state)
	{
		return missing;
	}

	public BlockModelShaper getBlockModelShaper()
	{
		return blockModelShaper;
	}

	public ModelRenderer getModelRenderer()
	{
		return modelRenderer;
	}

	public void renderSingleBlock(
			BlockState state,
			PoseStack poseStack,
			MultiBufferSource buffers,
			int packedLight,
			int packedOverlay
	)
	{
	}

	public static class BlockModelShaper
	{
		private final ModelManager modelManager = new ModelManager();
		private final BakedModel missing = new SimpleBakedModel();

		public BakedModel getBlockModel(BlockState state)
		{
			return missing;
		}

		public ModelManager getModelManager()
		{
			return modelManager;
		}
	}

	public static class ModelManager
	{
		private final BakedModel missing = new SimpleBakedModel();

		public BakedModel getModel(ModelResourceLocation name)
		{
			return missing;
		}
	}

	public static class ModelRenderer
	{
		public void tesselateBlock(
				Object level,
				BakedModel model,
				BlockState state,
				BlockPos pos,
				PoseStack poseStack,
				VertexConsumer consumer,
				boolean checkSides,
				RandomSource random,
				long seed,
				int overlay,
				ModelData modelData,
				RenderType renderType
		)
		{
		}

		public void renderModel(
				PoseStack.Pose pose,
				VertexConsumer consumer,
				BlockState state,
				BakedModel model,
				float red,
				float green,
				float blue,
				int packedLight,
				int packedOverlay,
				ModelData modelData,
				RenderType renderType
		)
		{
		}
	}
}
