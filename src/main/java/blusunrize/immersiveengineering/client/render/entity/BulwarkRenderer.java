package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

public class BulwarkRenderer extends IllagerRenderer<Bulwark, BulwarkRenderer.State>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/bulwark.png");

	public BulwarkRenderer(EntityRendererProvider.Context context)
	{
		super(context, new BulwarkModel(context.bakeLayer(IEModelLayers.BULWARK)), 0.5F);
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
	public void extractRenderState(Bulwark entity, State state, float partialTicks)
	{
		super.extractRenderState(entity, state, partialTicks);
		state.isBlocking = entity.isBlocking();
	}

	public static LayerDefinition createBodyLayer()
	{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
		partdefinition1.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
		partdefinition1.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
						.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(1F)),
				PartPose.offset(0.0F, 0.0F, 0.0F)
		);
		PartDefinition arms = partdefinition.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F).texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
		arms.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(-2.0F, 12.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(2.0F, 12.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(28, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 40).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F)
		);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(28, 40).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(.5F))
						.texOffs(44, 40).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(1F)),
				PartPose.offset(5.0F, 2.0F, 0.0F)
		);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public static class State extends IllagerRenderState
	{
		private boolean isBlocking;
	}

	private static class BulwarkModel extends IllagerModel<State>
	{
		public BulwarkModel(ModelPart root)
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
			if(state.isBlocking)
				if(isLefthanded)
				{
					rightArm.xRot = rightArm.xRot*0.5F-1.11701F;
					rightArm.yRot = (-(float)Math.PI/4F);
				}
				else
				{
					leftArm.xRot = leftArm.xRot*0.5F-1.11701F;
					leftArm.yRot = ((float)Math.PI/4F);
				}
			if(isLefthanded)
				leftArm.xRot = -.523599f;
			else
				rightArm.xRot = -.523599f;
		}
	}
}
