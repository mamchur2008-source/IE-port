package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose;

public class FusilierRenderer extends IllagerRenderer<Fusilier, IllagerRenderState>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/fusilier.png");

	public FusilierRenderer(EntityRendererProvider.Context context)
	{
		super(context, new FusilierModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
		this.model.getHat().visible = true;
		this.addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(IllagerRenderState state)
	{
		return TEXTURE;
	}

	@Override
	public IllagerRenderState createRenderState()
	{
		return new IllagerRenderState();
	}

	private static class FusilierModel extends IllagerModel<IllagerRenderState>
	{
		public FusilierModel(ModelPart root)
		{
			super(root);
		}

		@Override
		public void setupAnim(IllagerRenderState state)
		{
			super.setupAnim(state);
			if(state.armPose==IllagerArmPose.NEUTRAL)
				this.root().getChild("right_arm").xRot = -.87266f;
		}
	}
}
