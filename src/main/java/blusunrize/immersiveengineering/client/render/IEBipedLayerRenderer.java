/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class IEBipedLayerRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
		extends RenderLayer<S, M>
{
	public IEBipedLayerRenderer(RenderLayerParent<S, M> entityRendererIn, EntityModelSet models)
	{
		super(entityRendererIn);
	}

	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, S state, float limbSwing, float limbSwingAmount)
	{
	}
}
