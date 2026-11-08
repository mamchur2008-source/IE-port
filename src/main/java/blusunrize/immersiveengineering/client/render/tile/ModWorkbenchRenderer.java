/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.BlueprintLines;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.items.EngineersBlueprintItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModWorkbenchRenderer extends IEBlockEntityRenderer<ModWorkbenchBlockEntity>
{
	private static final Map<String, IVertexBufferHolder> VBO_BY_BLUEPRINT = new HashMap<>();

	@Override
	public void submit(
			RenderState<ModWorkbenchBlockEntity> state, PoseStack transform, SubmitNodeCollector nodes,
			CameraRenderState cameraState
	)
	{
		ModWorkbenchBlockEntity te = state.blockEntity;
		if(te==null)
			return;
		if(te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		transform.pushPose();
		transform.translate(.5, .5, .5);

		Direction facing = te.getFacing();

		float angle = facing==Direction.NORTH?0: facing==Direction.WEST?Mth.HALF_PI: facing==Direction.EAST?-Mth.HALF_PI: Mth.PI;

		transform.mulPose(new Quaternionf().rotateY(angle));

		ItemStack stack = te.getInventory().get(0);
		boolean showIngredients = true;
		if(!stack.isEmpty())
		{
			if(stack.getItem() instanceof EngineersBlueprintItem)
			{
				double playerDistanceSq = ClientUtils.mc().player.distanceToSqr(Vec3.atCenterOf(te.getBlockPos()));
				if(playerDistanceSq < 120)
				{
					final String category = IEApiDataComponents.getBlueprintType(stack);
					if(!category.isEmpty() && !IEApiDataComponents.INVALID_BLUEPRINT.equals(category))
					{
						final ClientLevel level = ClientUtils.mc().level;
						List<RecipeHolder<BlueprintCraftingRecipe>> recipes = BlueprintCraftingRecipe.findRecipes(level, category);
						if(!recipes.isEmpty())
						{
							IVertexBufferHolder vbo = VBO_BY_BLUEPRINT.computeIfAbsent(category, this::buildVBO);
							submitBlueprint(vbo, transform, nodes, state.lightCoords);
						}
					}
				}
			}
			else
			{
				showIngredients = false;
				transform.pushPose();
				transform.translate(0, .5625, 0);

				transform.mulPose(new Quaternionf().rotateY(Mth.PI).rotateX(Mth.HALF_PI));
				transform.translate(-.875, 0, 0);
				transform.scale(.75f, .75f, .75f);
				submitItem(te, stack, transform, nodes, state.lightCoords);
				transform.popPose();
			}
		}
		if(showIngredients)
		{
			for(int i = 1; i < te.getInventory().size(); i++)
			{
				double dX, dZ;
				if(i < 5)
				{
					dX = -.5+(i==2?-.0625: i==4?.03215: 0);
					dZ = i*.25-.625;
				}
				else
				{
					dX = -1.25;
					dZ = -.125+(i-5)*-.25;
				}

				stack = te.getInventory().get(i);
				if(!stack.isEmpty())
				{
					transform.pushPose();
					transform.mulPose(new Quaternionf().rotateY(Mth.PI).rotateX(Mth.HALF_PI));
					transform.translate(dX, dZ, -.515);
					transform.scale(.25f, .25f, .25f);
					submitItem(te, stack, transform, nodes, state.lightCoords);
					transform.popPose();
				}
			}
		}
		transform.popPose();
	}

	private static void submitItem(
			ModWorkbenchBlockEntity te, ItemStack stack, PoseStack transform, SubmitNodeCollector nodes, int light
	)
	{
		ItemStackRenderState itemState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
				itemState, stack, ItemDisplayContext.FIXED, te.getLevel(), null, 0
		);
		itemState.submit(transform, nodes, light, OverlayTexture.NO_OVERLAY, 0);
	}

	private static void submitBlueprint(IVertexBufferHolder vbo, PoseStack transform, SubmitNodeCollector nodes, int light)
	{
		nodes.submitCustomGeometry(
				transform,
				BlueprintRenderer.RENDER_TYPE,
				(pose, consumer) -> {
					PoseStack renderPose = new PoseStack();
					renderPose.last().set(pose);
					vbo.render(BlueprintRenderer.RENDER_TYPE, light, OverlayTexture.NO_OVERLAY, type -> consumer, renderPose);
				}
		);
	}

	private IVertexBufferHolder buildVBO(String category)
	{
		return IVertexBufferHolder.create((builder, transform, light, overlay) -> {
			final ClientLevel level = ClientUtils.mc().level;
			List<RecipeHolder<BlueprintCraftingRecipe>> recipes = BlueprintCraftingRecipe.findRecipes(level, category);
			transform.pushPose();
			int numRecipes = Math.min(recipes.size(), 9);
			int perRow;
			if(numRecipes > 6) perRow = numRecipes-3;
			else if(numRecipes > 4) perRow = numRecipes-2;
			else if(numRecipes==1) perRow = 2;
			else if(numRecipes==2) perRow = 3;
			else perRow = numRecipes;
			transform.translate(0, .502, 0);
			transform.mulPose(new Quaternionf().rotateY(-Mth.PI/8).rotateX(-Mth.HALF_PI));
			transform.translate(0.39, numRecipes > 4?.72: .78, 0);
			float scale = numRecipes > 4?.009375f: .012f;
			transform.scale(scale, -scale, scale);
			int rendered = 0;
			for(int i = 0; i < numRecipes; i++)
			{
				BlueprintCraftingRecipe recipe = recipes.get(i%recipes.size()).value();
				BlueprintLines blueprint = recipe==null?null: BlueprintRenderer.getBlueprintDrawable(recipe, level);
				if(blueprint!=null)
				{
					double dX = rendered < perRow?(.93725/scale-perRow*16.6)+rendered*16.6: (.70375/scale-rendered%perRow*16.6);
					double dY = rendered < perRow?0: -.15625;
					transform.translate(dX, dY/scale, 0);

					//Width depends on distance
					float texScale = blueprint.textureScale/16f;
					transform.scale(1/texScale, 1/texScale, 1/texScale);
					blueprint.draw(transform, builder, light);
					transform.scale(texScale, texScale, texScale);
					transform.translate(-dX, -dY/scale, 0);
					rendered++;
				}
			}
			transform.popPose();
		});
	}

	public AABB getRenderBoundingBox(ModWorkbenchBlockEntity workbench)
	{
		if(workbench.renderAABB==null)
			workbench.renderAABB = new AABB(-1, 0, -1, 2, 2, 2).move(workbench.getBlockPos());
		return workbench.renderAABB;
	}

}
