package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Commando;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose;

public class CommandoRenderer extends IllagerRenderer<Commando, CommandoRenderer.State>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/commando.png");

	public CommandoRenderer(EntityRendererProvider.Context context)
	{
		super(context, new CommandoModel(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
		this.model.getHat().visible = true;
		this.addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public Identifier getTextureLocation(State state)
	{
		return TEXTURE;
	}

	@Override
	public State createRenderState()
	{
		return new State();
	}

	@Override
	public void extractRenderState(Commando entity, State state, float partialTicks)
	{
		super.extractRenderState(entity, state, partialTicks);
		state.isBlocking = entity.isBlocking();
		state.isAiming = entity.isAiming();
	}

	public static class State extends IllagerRenderState
	{
		private boolean isBlocking;
		private boolean isAiming;
	}

	private static class CommandoModel extends IllagerModel<State>
	{
		public CommandoModel(ModelPart root)
		{
			super(root);
		}

		@Override
		public void setupAnim(State state)
		{
			super.setupAnim(state);
			boolean isLefthanded = state.mainArm==HumanoidArm.LEFT;
			ModelPart rightArm = this.root().getChild("right_arm");
			ModelPart leftArm = this.root().getChild("left_arm");
			ModelPart head = this.root().getChild("head");
			if(state.isBlocking)
				if(isLefthanded)
				{
					rightArm.xRot = rightArm.xRot*0.5F-0.9424779F;
					rightArm.yRot = (-(float)Math.PI/6F);
				}
				else
				{
					leftArm.xRot = leftArm.xRot*0.5F-0.9424779F;
					leftArm.yRot = ((float)Math.PI/6F);
				}
			if(state.armPose==IllagerArmPose.NEUTRAL&&state.isAiming)
				if(isLefthanded)
				{
					leftArm.xRot = -1.39626f+head.xRot;
					leftArm.yRot = .08726f+head.yRot;
				}
				else
				{
					rightArm.xRot = -1.39626f+head.xRot;
					rightArm.yRot = -.08726f+head.yRot;
				}
		}
	}
}
