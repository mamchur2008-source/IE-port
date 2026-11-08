/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.client.gui.BlastFurnaceScreen;
import blusunrize.immersiveengineering.client.gui.elements.ManualUnlockToast;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer;
import blusunrize.immersiveengineering.client.render.tile.BlueprintRenderer.BlueprintLines;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.CrateItem;
import blusunrize.immersiveengineering.common.blocks.generic.CatwalkBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WindowBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TurntableBlockEntity;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.network.MessageMagnetEquip;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.network.MessageRevolverRotate;
import blusunrize.immersiveengineering.common.network.MessageScrollwheelItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.client.AdvancementToastAccess;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities.Energy;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class ClientEventHandler implements ResourceManagerReloadListener
{
	private boolean shieldToggleButton = false;
	private int shieldToggleTimer = 0;

	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		ImmersiveEngineering.proxy.clearRenderCaches();
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event)
	{
		final var player = event.getEntity();
		if(!player.level().isClientSide()||player!=ClientUtils.mc().player)
			return;
		if(this.shieldToggleTimer > 0)
			this.shieldToggleTimer--;
		if(IEKeybinds.keybind_magnetEquip.isDown()&&!this.shieldToggleButton)
			if(this.shieldToggleTimer <= 0)
				this.shieldToggleTimer = 7;
			else
			{
				ItemStack held = player.getItemInHand(InteractionHand.OFF_HAND);
				if(!held.isEmpty()&&held.getItem() instanceof IEShieldItem)
				{
					if(UpgradeableToolItem.getUpgradesStatic(held).get(UpgradeEffect.MAGNET).prevSlot().isPresent())
						ClientPacketDistributor.sendToServer(new MessageMagnetEquip(-1));
				}
				else
				{
					for(int i = 0; i < player.getInventory().getNonEquipmentItems().size(); i++)
					{
						ItemStack s = player.getInventory().getNonEquipmentItems().get(i);
						if(!s.isEmpty()&&s.getItem() instanceof IEShieldItem&&((IEShieldItem)s.getItem()).getUpgrades(s).has(UpgradeEffect.MAGNET))
							ClientPacketDistributor.sendToServer(new MessageMagnetEquip(i));
					}
				}
			}
		if(this.shieldToggleButton!=ClientUtils.mc().options.keyDown.isDown())
			this.shieldToggleButton = ClientUtils.mc().options.keyDown.isDown();


		if(!IEKeybinds.keybind_chemthrowerSwitch.isUnbound()&&IEKeybinds.keybind_chemthrowerSwitch.consumeClick())
		{
			ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
			if(held.getItem() instanceof IScrollwheel)
				ClientPacketDistributor.sendToServer(new MessageScrollwheelItem(true));
		}

		if(!IEKeybinds.keybind_railgunZoom.isUnbound()&&IEKeybinds.keybind_railgunZoom.consumeClick())
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack held = player.getItemInHand(hand);
				if(held.getItem() instanceof IZoomTool&&((IZoomTool)held.getItem()).canZoom(held, player))
				{
					ZoomHandler.isZooming = !ZoomHandler.isZooming;
					if(ZoomHandler.isZooming)
					{
						float[] steps = ((IZoomTool)held.getItem()).getZoomSteps(held, player);
						if(steps!=null&&steps.length > 0)
							ZoomHandler.fovZoom = steps[ZoomHandler.getCurrentZoomStep(steps)];
					}
					break;
				}
			}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent.Pre event)
	{
		LevelStageRenders.FAILED_CONNECTIONS.entrySet().removeIf(entry -> entry.getValue().getSecond().decrementAndGet() <= 0);
		ClientLevel world = Minecraft.getInstance().level;
		if(world!=null)
			GlobalWireNetwork.getNetwork(world).update(world);
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if(event.getItemStack().isEmpty())
			return;
		CapabilityShader.ShaderWrapper wrapper = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(event.getItemStack(), CapabilityShader.ITEM);
		if(wrapper!=null)
		{
			var shader = wrapper.getShader();
			if(shader!=null)
				event.getToolTip().add(TextUtils.applyFormat(
						ShaderItem.getShaderName(shader),
						ChatFormatting.DARK_GRAY
				));
		}
		if(event.getItemStack().has(IEDataComponents.CONTAINED_EARMUFF))
		{
			ItemStack earmuffs = event.getItemStack().get(IEDataComponents.CONTAINED_EARMUFF).attached();
			if(!earmuffs.isEmpty())
				event.getToolTip().add(TextUtils.applyFormat(
						earmuffs.getHoverName(),
						ChatFormatting.GRAY
				));
		}
		if(event.getItemStack().has(IEDataComponents.CONTAINED_POWERPACK))
		{
			ItemStack powerpack = event.getItemStack().get(IEDataComponents.CONTAINED_POWERPACK).attached();
			IEnergyStorage packStorage = blusunrize.immersiveengineering.common.util.CapabilityCompat.getItemCapability(powerpack, Energy.ITEM);
			if(!powerpack.isEmpty()&&packStorage!=null)
			{
				List<Component> tooltip = event.getToolTip();
				// find gap
				int idx = IntStream.range(0, tooltip.size()).filter(i -> tooltip.get(i)==CommonComponents.EMPTY).findFirst().orElse(tooltip.size()-1);
				// put tooltip in that gap
				tooltip.add(idx++, CommonComponents.EMPTY);
				tooltip.add(idx++, TextUtils.applyFormat(powerpack.getHoverName(), ChatFormatting.GRAY));
				tooltip.add(idx++, TextUtils.applyFormat(
						Component.literal(packStorage.getEnergyStored()+"/"+packStorage.getMaxEnergyStored()+" IF"),
						ChatFormatting.GRAY
				));
				tooltip.add(idx, TextUtils.applyFormat(Component.translatable("desc.immersiveengineering.info.noChargeOnArmor"), ChatFormatting.DARK_GRAY));
			}
		}
		Level clientLevel = ClientUtils.mc().level;
		if(ClientUtils.mc().screen!=null
				&&ClientUtils.mc().screen instanceof BlastFurnaceScreen
				&&BlastFurnaceFuel.isValidBlastFuel(clientLevel, event.getItemStack()))
			event.getToolTip().add(TextUtils.applyFormat(
					Component.translatable("desc.immersiveengineering.info.blastFuelTime", BlastFurnaceFuel.getBlastFuelTime(clientLevel, event.getItemStack())),
					ChatFormatting.GRAY
			));

		if(IEClientConfig.tagTooltips.get()&&event.getFlags().isAdvanced())
			event.getItemStack().getItem().builtInRegistryHolder().tags()
					.map(TagKey::location)
					.forEach(oid ->
							event.getToolTip().add(TextUtils.applyFormat(
									Component.literal(oid.toString()),
									ChatFormatting.GRAY
							)));
	}

	@SubscribeEvent
	public void onRenderItemFrame(RenderItemInFrameEvent event)
	{
	}

	private static void handleSubtitleOffset(boolean pre)
	{
	}

	@SubscribeEvent
	public void onRenderOverlayPre(RenderGuiLayerEvent.Pre event)
	{
		if(event.getName().equals(VanillaGuiLayers.SUBTITLE_OVERLAY))
			ItemOverlayUtils.handleTooltipOffset(event.getGuiGraphics(), true);
	}

	@SubscribeEvent
	public void onRenderOverlayPost(RenderGuiLayerEvent.Post event)
	{
		if(event.getName().equals(VanillaGuiLayers.SUBTITLE_OVERLAY))
			ItemOverlayUtils.handleTooltipOffset(event.getGuiGraphics(), false);
	}

	@SubscribeEvent()
	public void onFogUpdate(ViewportEvent.RenderFog event)
	{
	}

	@SubscribeEvent()
	public void onFogColourUpdate(ViewportEvent.ComputeFogColor event)
	{
	}

	@SubscribeEvent()
	public void onFOVUpdate(ComputeFovModifierEvent event)
	{
		Player player = ClientUtils.mc().player;

		// Check if player is holding a zoom-allowing item
		boolean mayZoom = Arrays.stream(InteractionHand.values())
				.map(player::getItemInHand)
				.anyMatch(s -> s.getItem() instanceof IZoomTool zoomTool&&zoomTool.canZoom(s, player));
		// Set zoom if allowed, otherwise stop zooming
		if(ZoomHandler.isZooming)
		{
			if(mayZoom)
				event.setNewFovModifier(ZoomHandler.fovZoom);
			else
				ZoomHandler.isZooming = false;
		}

		// Concrete feet slow you, but shouldn't break FoV
		if(player.getEffect(IEPotions.CONCRETE_FEET)!=null)
			event.setNewFovModifier(1);
	}

	@SubscribeEvent()
	public void onPlayerTurn(CalculatePlayerTurnEvent event)
	{
		if(event.getCinematicCameraEnabled())
			return;

		// Check if player is holding a zoom-allowing item and using them
		Player player = ClientUtils.mc().player;
		boolean mayZoom = Arrays.stream(InteractionHand.values())
				.map(player::getItemInHand)
				.anyMatch(s -> s.getItem() instanceof IZoomTool zoomTool&&zoomTool.canZoom(s, player));
		if(ZoomHandler.isZooming&&mayZoom)
		{
			// final math is: (m*0.6 + 0.2)³ * 8; where m is the mouse sensitivity
			// we want to avoid the "* 8" so the modifier to fix that is: (6m + 1)/(3m)
			// however this only applies for the spyglass which has a fov modifier of 0.1,
			// so we'll also scale it by the current zoom level
			double mouseSensitivity = event.getMouseSensitivity();
			double mod = 0.5-1/(6*mouseSensitivity);
			double fovMod = 0.1/ZoomHandler.fovZoom;
			event.setMouseSensitivity(mod*mouseSensitivity/fovMod);
		}
	}

	@SubscribeEvent
	public void onMouseEvent(MouseScrollingEvent event)
	{
		Player player = ClientUtils.mc().player;
		if(event.getScrollDeltaY()!=0&&ClientUtils.mc().screen==null&&player!=null)
		{
			// Handle zooming in and out
			if(ZoomHandler.isZooming)
			{
				float[] zoomSteps = Arrays.stream(InteractionHand.values())
						.map(player::getItemInHand)
						.mapMulti((BiConsumer<ItemStack, Consumer<float[]>>)(itemStack, consumer) -> {
							if(itemStack.getItem() instanceof IZoomTool zoomTool&&zoomTool.canZoom(itemStack, player))
								consumer.accept(zoomTool.getZoomSteps(itemStack, player));
						}).findFirst().orElse(null);
				if(zoomSteps!=null&&zoomSteps.length > 0)
				{
					int curStep = ZoomHandler.getCurrentZoomStep(zoomSteps);
					int newStep = curStep+(event.getScrollDeltaY() > 0?-1: 1);
					if(newStep >= 0&&newStep < zoomSteps.length)
						ZoomHandler.fovZoom = zoomSteps[newStep];
					event.setCanceled(true);
				}
			}
			else
			{
				ItemStack equipped = player.getItemInHand(InteractionHand.MAIN_HAND);
				// Handle sneak + scrolling
				if(player.isShiftKeyDown())
				{
					if(IEServerConfig.TOOLS.chemthrower_scroll.get()&&equipped.getItem() instanceof IScrollwheel)
					{
						ClientPacketDistributor.sendToServer(new MessageScrollwheelItem(event.getScrollDeltaY() < 0));
						event.setCanceled(true);
					}
					if(equipped.getItem() instanceof RevolverItem)
					{
						ClientPacketDistributor.sendToServer(new MessageRevolverRotate(event.getScrollDeltaY() < 0));
						event.setCanceled(true);
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void renderAdditionalBlockBounds(ExtractBlockOutlineRenderStateEvent event)
	{
	}

	@SubscribeEvent()
	public void onRenderLivingPre(RenderLivingEvent.Pre<?, ?, ?> event)
	{
	}

	@SubscribeEvent()
	public void onRenderLivingPost(RenderLivingEvent.Post<?, ?, ?> event)
	{
	}

	private static void enableHead(LivingEntityRenderer<?, ?, ?> renderer, boolean shouldEnable)
	{
		if(renderer.getModel() instanceof HeadedModel model)
			model.getHead().visible = shouldEnable;
	}

	private static void enableUpperBody(LivingEntityRenderer<?, ?, ?> renderer, boolean shouldEnable)
	{
		if(renderer.getModel() instanceof PlayerModel model)
		{
			model.leftArm.visible = shouldEnable;
			model.leftSleeve.visible = shouldEnable;
			model.rightArm.visible = shouldEnable;
			model.rightSleeve.visible = shouldEnable;
			model.head.visible = shouldEnable;
			model.hat.visible = shouldEnable;
		}
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinLevelEvent event)
	{
		if(event.getEntity().level().isClientSide()&&event.getEntity() instanceof AbstractMinecart)
			ClientPacketDistributor.sendToServer(new MessageMinecartShaderSync(event.getEntity().getId(), Optional.empty()));
	}

	@SubscribeEvent
	public void onToast(ToastAddEvent event)
	{
		if(event.getToast() instanceof AdvancementToast advToast)
		{
			AdvancementHolder advancement = ((AdvancementToastAccess)advToast).getAdvancement();
			ManualInstance manual = ManualHelper.getManual();
			if(manual.contentsByName.isEmpty()) // we need to load the manual if not already done
				manual.reload();
			List<ManualEntry> entries = manual.contentsByName.values().stream()
					.filter(entry -> entry.getRequiredAdvancement().map(loc -> loc.equals(advancement.id())).orElse(false))
					.toList();
			if(!entries.isEmpty())
			{
				// wrap the toast if it has a title, cancel the original
				Optional<AdvancementToast> wrapped = advancement.value().display().map(
						displayInfo -> displayInfo.getTitle().getString().isEmpty()?null: advToast
				);
				event.setCanceled(true);
				// then enqueue the manual toast
				ClientUtils.mc().getToastManager().addToast(new ManualUnlockToast(wrapped, entries));
			}
		}
	}
}
