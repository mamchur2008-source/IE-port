/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.manual;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.utils.GuiGraphicsPose;
import blusunrize.immersiveengineering.common.util.fakeworld.TemplateWorld;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.SpecialManualElements;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Predicate;

import static blusunrize.immersiveengineering.api.client.TextUtils.applyFormat;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID)
public class ManualElementMultiblock extends SpecialManualElements
{
	private static final int RENDER_WIDTH = 160;
	private static final int FULL_BRIGHT_LIGHT = 0xf000f0;

	private final IMultiblock multiblock;
	private final MultiblockManualData renderProperties;

	private boolean canTick = true;
	private boolean showCompleted = false;

	private float scale = 50f;
	private float transX = 0;
	private float transY = 0;
	private Transformation additionalTransform;
	private List<Component> componentTooltip;
	private final MultiblockRenderInfo renderInfo;
	private final TemplateWorld structureWorld;
	private final int yOffTotal;
	private final ClientLevel level;

	private long lastStep = -1;
	private long lastPrintedErrorTimeMs = -1;
	private final List<BlockStateModelPart> reusableModelParts = new ArrayList<>();

	public ManualElementMultiblock(ManualInstance manual, IMultiblock multiblock)
	{
		super(manual);
		level = Objects.requireNonNull(Minecraft.getInstance().level);
		this.multiblock = multiblock;
		this.renderProperties = ClientMultiblocks.get(multiblock);
		List<StructureBlockInfo> structure = multiblock.getStructure(level);
		renderInfo = new MultiblockRenderInfo(structure);
		float diagLength = (float)Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+
				renderInfo.structureWidth*renderInfo.structureWidth+
				renderInfo.structureLength*renderInfo.structureLength);
		structureWorld = new TemplateWorld(structure, renderInfo, level.registryAccess());
		transX = 60+renderInfo.structureWidth/2F;
		transY = 35+diagLength/2;
		additionalTransform = new Transformation(
				null,
				new Quaternionf().rotateXYZ((float)Math.toRadians(25), 0, 0),
				null,
				new Quaternionf().rotateXYZ(0, (float)Math.toRadians(-45), 0)
		);
		scale = multiblock.getManualScale();
		yOffTotal = (int)(transY+scale*diagLength/2);
	}

	private static final Component greenTick = applyFormat(
			Component.literal("\u2713"), ChatFormatting.GREEN, ChatFormatting.BOLD
	).append(" ");

	public void onOpened(ManualScreen gui, int x, int y, List<Button> pageButtons)
	{
		int yOff = 0;
		if(multiblock.getStructure(level)!=null)
		{
			boolean canRenderFormed = renderProperties.canRenderFormedStructure();

			yOff = (int)(transY+scale*Math.sqrt(renderInfo.structureHeight*renderInfo.structureHeight+renderInfo.structureWidth*renderInfo.structureWidth+renderInfo.structureLength*renderInfo.structureLength)/2);
			pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY-(canRenderFormed?11: 5), 10, 10, 4, btn -> {
				GuiButtonManualNavigation btnNav = (GuiButtonManualNavigation)btn;
				canTick = !canTick;
				lastStep = -1;
				btnNav.type = btnNav.type==4?5: 4;
			}));
			if(this.renderInfo.structureHeight > 1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY-(canRenderFormed?14: 8)-16, 10, 16, 3,
						btn -> renderInfo.setShowLayer(Math.min(renderInfo.showLayer+1, renderInfo.structureHeight-1))
				));
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY+(canRenderFormed?14: 8), 10, 16, 2,
						btn -> renderInfo.setShowLayer(Math.max(renderInfo.showLayer-1, -1))));
			}
			if(canRenderFormed)
				pageButtons.add(new GuiButtonManualNavigation(gui, x+4, y+(int)transY+1, 10, 10, 6,
						btn -> showCompleted = !showCompleted));
		}

		checkMaterials();
		super.onOpened(gui, x, yOff, pageButtons);
	}

	private void checkMaterials()
	{
		NonNullList<ItemStack> totalMaterials = this.renderProperties.getTotalMaterials();
		if(totalMaterials!=null)
		{
			componentTooltip = new ArrayList<>();
			componentTooltip.add(Component.translatable("desc.immersiveengineering.info.reqMaterial"));
			int maxOff = 1;
			boolean hasAnyItems = false;
			boolean[] hasItems = new boolean[totalMaterials.size()];
			for(int ss = 0; ss < totalMaterials.size(); ss++)
			{
				ItemStack req = totalMaterials.get(ss);
				int reqSize = req.getCount();
				for(int slot = 0; slot < ManualUtils.mc().player.getInventory().getContainerSize(); slot++)
				{
					ItemStack inSlot = ManualUtils.mc().player.getInventory().getItem(slot);
					if(!inSlot.isEmpty()&&ItemStack.isSameItem(inSlot, req))
						if((reqSize -= inSlot.getCount()) <= 0)
							break;
				}
				if(reqSize <= 0)
				{
					hasItems[ss] = true;
					if(!hasAnyItems)
						hasAnyItems = true;
				}
				maxOff = Math.max(maxOff, (""+req.getCount()).length());
			}
			for(int ss = 0; ss < totalMaterials.size(); ss++)
			{
				ItemStack req = totalMaterials.get(ss);
				int indent = maxOff-(""+req.getCount()).length();
				StringBuilder sIndent = new StringBuilder();
				if(indent > 0)
					sIndent.append("0".repeat(indent));
				MutableComponent s;
				if(hasItems[ss])
					s = greenTick.copy();
				else
					s = Component.literal(hasAnyItems?"   ": "");
				s.append(applyFormat(
						Component.literal(sIndent.toString()+req.getCount()+"x "), ChatFormatting.GRAY
				));
				if(!req.isEmpty())
					s.append(applyFormat(req.getHoverName().copy(), req.getRarity().color()));
				else
					s.append("???");
				componentTooltip.add(s);
			}
		}
	}

	public void render(GuiGraphicsExtractor graphics, ManualScreen gui, int x, int y, int mouseX, int mouseY)
	{
		if(multiblock.getStructure(level)!=null)
		{
			try
			{
				long currentTime = System.currentTimeMillis();
				if(lastStep < 0)
					lastStep = currentTime;
				else if(canTick&&currentTime-lastStep > 500)
				{
					renderInfo.step();
					lastStep = currentTime;
				}
				Object pose = GuiGraphicsPose.pose(graphics);
				Matrix3x2f guiPose = pose instanceof Matrix3x2fc matrix?new Matrix3x2f(matrix): new Matrix3x2f();
				graphics.submitPictureInPictureRenderState(new MultiblockRenderState(
						this, guiPose, 0, 0, RENDER_WIDTH, Math.max(1, yOffTotal), 1,
						graphics.peekScissorStack()
				));
			} catch(Exception|LinkageError e)
			{
				printRenderException(e);
			}

			if(componentTooltip!=null)
			{
				graphics.text(manual.fontRenderer(), "?", 116, yOffTotal/2-4, manual.getTextColour());
				if(mouseX >= 116&&mouseX < 122&&mouseY >= yOffTotal/2-4&&mouseY < yOffTotal/2+4)
					graphics.setTooltipForNextFrame(manual.fontRenderer(), Language.getInstance().getVisualOrder(
							Collections.unmodifiableList(componentTooltip)
					), mouseX, mouseY);
			}
		}
	}

	public void mouseDragged(int x, int y, double clickX, double clickY, double mouseX, double mouseY, double lastX, double lastY, int mouseButton)
	{
		if((clickX >= 40&&clickX < 144&&mouseX >= 20&&mouseX < 164)&&(clickY >= 30&&clickY < 130&&mouseY >= 30&&mouseY < 180))
		{
			double dx = mouseX-lastX;
			double dy = mouseY-lastY;
			additionalTransform = forRotation(dx*80D/104, dy*0.8).compose(additionalTransform);
		}
	}

	private Transformation forRotation(double rX, double rY)
	{
		Vector3f axis = new Vector3f((float)rY, (float)rX, 0);
		if(axis.lengthSquared() < 1e-3)
			return Transformation.IDENTITY;
		float angle = (float)Math.sqrt(axis.dot(axis));
		axis.normalize();
		return new Transformation(null, new Quaternionf().rotateAxis((float)Math.toRadians(angle), axis), null, null);
	}

	@SubscribeEvent
	public static void registerPictureInPictureRenderer(RegisterPictureInPictureRenderersEvent ev)
	{
		ev.register(MultiblockRenderState.class, MultiblockPictureInPictureRenderer::new);
	}

	private void renderToTexture(PoseStack transform, MultiBufferSource.BufferSource bufferSource, int width)
	{
		PoseStack.Pose lastEntryBeforeTry = transform.last();
		try
		{
			int structureLength = renderInfo.structureLength;
			int structureWidth = renderInfo.structureWidth;
			int structureHeight = renderInfo.structureHeight;

			transform.pushPose();
			transform.translate(-width/2f, 0, 0);

			Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
			BlockStateModelSet blockModels = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
			BlockAndTintGetter previewWorld = new MultiblockPreviewWorld(structureWorld, level);
			QuadInstance quadInstance = new QuadInstance();

			transform.translate(transX, transY, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
			transform.scale(scale, -scale, 1);
			applyAdditionalTransform(transform);
			transform.mulPose(new Quaternionf().rotateXYZ(0, Mth.HALF_PI, 0));

			transform.translate(structureLength/-2f, structureHeight/-2f, structureWidth/-2f);

			if(showCompleted&&renderProperties.canRenderFormedStructure())
				renderProperties.renderFormedStructure(transform, bufferSource);
			else
			{
				for(int h = 0; h < structureHeight; h++)
					for(int l = 0; l < structureLength; l++)
						for(int w = 0; w < structureWidth; w++)
						{
							BlockPos pos = new BlockPos(l, h, w);
							BlockState state = structureWorld.getBlockState(pos);
							if(!state.isAir())
							{
								int overlay;
								if(pos.equals(multiblock.getTriggerOffset()))
									overlay = OverlayTexture.pack(0, true);
								else
									overlay = OverlayTexture.NO_OVERLAY;
								if(state.getRenderShape()==RenderShape.MODEL)
								{
									BlockStateModel model = blockModels.get(state);
									model.collectParts(
											previewWorld, pos, state, RandomSource.create(state.getSeed(pos)), reusableModelParts
									);
									quadInstance.setOverlayCoords(overlay);
									quadInstance.setLightCoords(FULL_BRIGHT_LIGHT);
									transform.pushPose();
									transform.translate(l, h, w);
									for(BlockStateModelPart part : reusableModelParts)
										renderModelPart(part, transform, bufferSource, quadInstance);
									transform.popPose();
									reusableModelParts.clear();
								}
							}
						}
			}
			transform.popPose();
		} catch(Exception|LinkageError e)
		{
			printRenderException(e);
			while(lastEntryBeforeTry!=transform.last())
				transform.popPose();
			reusableModelParts.clear();
		}
	}

	private static void renderModelPart(
			BlockStateModelPart part, PoseStack transform, MultiBufferSource.BufferSource bufferSource,
			QuadInstance quadInstance
	)
	{
		for(Direction direction : Direction.values())
			renderQuads(part.getQuads(direction), transform, bufferSource, quadInstance);
		renderQuads(part.getQuads(null), transform, bufferSource, quadInstance);
	}

	private static void renderQuads(
			List<net.minecraft.client.resources.model.geometry.BakedQuad> quads, PoseStack transform,
			MultiBufferSource.BufferSource bufferSource, QuadInstance quadInstance
	)
	{
		for(net.minecraft.client.resources.model.geometry.BakedQuad quad : quads)
		{
			VertexConsumer consumer = bufferSource.getBuffer(quad.materialInfo().itemRenderType());
			consumer.putBakedQuad(transform.last(), quad, quadInstance);
		}
	}

	private void printRenderException(Throwable e)
	{
		final long now = System.currentTimeMillis();
		if(now > lastPrintedErrorTimeMs+1000)
		{
			e.printStackTrace();
			lastPrintedErrorTimeMs = now;
		}
	}

	private void applyAdditionalTransform(PoseStack transform)
	{
		Vector3fc translation = additionalTransform.translation();
		Vector3fc scale = additionalTransform.scale();
		transform.translate(translation.x(), translation.y(), translation.z());
		transform.mulPose(additionalTransform.leftRotation());
		transform.scale(scale.x(), scale.y(), scale.z());
		transform.mulPose(additionalTransform.rightRotation());
	}

	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	public int getPixelsTaken()
	{
		return yOffTotal;
	}

	public IMultiblock getMultiblock()
	{
		return this.multiblock;
	}

	private record MultiblockRenderState(
			ManualElementMultiblock element,
			Matrix3x2f pose,
			int x0,
			int y0,
			int x1,
			int y1,
			float scale,
			ScreenRectangle scissorArea,
			ScreenRectangle bounds
	) implements PictureInPictureRenderState
	{
		private MultiblockRenderState(
				ManualElementMultiblock element, Matrix3x2f pose, int x0, int y0, int x1, int y1, float scale,
				ScreenRectangle scissorArea
		)
		{
			this(element, pose, x0, y0, x1, y1, scale, scissorArea,
					transformedBounds(pose, x0, y0, x1, y1, scissorArea));
		}

		private static ScreenRectangle transformedBounds(
				Matrix3x2fc pose, int x0, int y0, int x1, int y1, ScreenRectangle scissorArea
		)
		{
			float x00 = pose.m00()*x0+pose.m10()*y0+pose.m20();
			float y00 = pose.m01()*x0+pose.m11()*y0+pose.m21();
			float x10 = pose.m00()*x1+pose.m10()*y0+pose.m20();
			float y10 = pose.m01()*x1+pose.m11()*y0+pose.m21();
			float x01 = pose.m00()*x0+pose.m10()*y1+pose.m20();
			float y01 = pose.m01()*x0+pose.m11()*y1+pose.m21();
			float x11 = pose.m00()*x1+pose.m10()*y1+pose.m20();
			float y11 = pose.m01()*x1+pose.m11()*y1+pose.m21();
			int left = (int)Math.floor(Math.min(Math.min(x00, x10), Math.min(x01, x11)));
			int top = (int)Math.floor(Math.min(Math.min(y00, y10), Math.min(y01, y11)));
			int right = (int)Math.ceil(Math.max(Math.max(x00, x10), Math.max(x01, x11)));
			int bottom = (int)Math.ceil(Math.max(Math.max(y00, y10), Math.max(y01, y11)));
			return PictureInPictureRenderState.getBounds(left, top, right, bottom, scissorArea);
		}
	}

	private record MultiblockPreviewWorld(TemplateWorld structureWorld, ClientLevel clientLevel) implements BlockAndTintGetter
	{
		public CardinalLighting cardinalLighting()
		{
			return clientLevel.cardinalLighting();
		}

		public int getBlockTint(BlockPos pos, ColorResolver colorResolver)
		{
			return clientLevel.getBlockTint(pos, colorResolver);
		}

		public BlockEntity getBlockEntity(BlockPos pos)
		{
			return structureWorld.getBlockEntity(pos);
		}

		public BlockState getBlockState(BlockPos pos)
		{
			return structureWorld.getBlockState(pos);
		}

		public FluidState getFluidState(BlockPos pos)
		{
			return structureWorld.getFluidState(pos);
		}

		public LevelLightEngine getLightEngine()
		{
			return structureWorld.getLightEngine();
		}

		public int getHeight()
		{
			return structureWorld.getHeight();
		}

		public int getMinY()
		{
			return structureWorld.getMinY();
		}
	}

	private static class MultiblockPictureInPictureRenderer extends PictureInPictureRenderer<MultiblockRenderState>
	{
		protected MultiblockPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource)
		{
			super(bufferSource);
		}

		public Class<MultiblockRenderState> getRenderStateClass()
		{
			return MultiblockRenderState.class;
		}

		protected void renderToTexture(MultiblockRenderState state, PoseStack transform)
		{
			state.element().renderToTexture(transform, bufferSource, state.x1()-state.x0());
		}

		protected String getTextureLabel()
		{
			return "immersiveengineering_manual_multiblock";
		}

		protected float getTranslateY(int height, int guiScale)
		{
			return 0;
		}
	}

	//Stolen back from boni's StructureInfo
	static class MultiblockRenderInfo implements Predicate<BlockPos>
	{
		public Map<BlockPos, StructureBlockInfo> data = new HashMap<>();
		private final int structureHeight;
		private final int structureLength;
		private final int structureWidth;
		private final int maxBlockIndex;

		private int showLayer = -1;
		private int blockIndex;

		MultiblockRenderInfo(List<StructureBlockInfo> structure)
		{
			int structureHeight = 0;
			int structureWidth = 0;
			int structureLength = 0;
			for(StructureBlockInfo block : structure)
			{
				structureHeight = Math.max(structureHeight, block.pos().getY()+1);
				structureWidth = Math.max(structureWidth, block.pos().getZ()+1);
				structureLength = Math.max(structureLength, block.pos().getX()+1);
				data.put(block.pos(), block);
			}
			this.maxBlockIndex = this.blockIndex = structureHeight*structureLength*structureWidth;
			this.structureHeight = structureHeight;
			this.structureLength = structureLength;
			this.structureWidth = structureWidth;
		}

		void setShowLayer(int layer)
		{
			showLayer = layer;
			if(layer < 0)
				reset();
			else
				blockIndex = (layer+1)*(structureLength*structureWidth)-1;
		}

		public void reset()
		{
			blockIndex = maxBlockIndex;
		}

		void step()
		{
			final int start = blockIndex;
			do
			{
				if(++blockIndex >= maxBlockIndex)
					blockIndex = 0;
			}
			while(isEmpty(blockIndex)&&blockIndex!=start);
		}

		private boolean isEmpty(int index)
		{
			int y = index/(structureLength*structureWidth);
			int r = index%(structureLength*structureWidth);
			int x = r/structureWidth;
			int z = r%structureWidth;

			return !data.containsKey(new BlockPos(x, y, z));
		}

		int getLimiter()
		{
			return blockIndex;
		}

		public boolean test(BlockPos blockPos)
		{
			int index = blockPos.getZ()+structureWidth*(blockPos.getX()+structureLength*blockPos.getY());
			return index <= getLimiter();
		}
	}
}
