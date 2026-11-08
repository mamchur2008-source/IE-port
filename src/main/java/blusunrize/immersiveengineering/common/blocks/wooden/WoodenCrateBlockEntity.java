/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.codec.IECodecs;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.gui.CrateMenu;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import blusunrize.immersiveengineering.mixin.accessors.BaseContainerBEAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class WoodenCrateBlockEntity extends RandomizableContainerBlockEntity
		implements IIEInventory, IBlockEntityDrop, IComparatorOverride, IPlayerInteraction, IBlockOverlayText
{
	public static final int CONTAINER_SIZE = 27;
	public static final int HITS_TO_SEAL = 6;
	private NonNullList<ItemStack> inventory = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private ItemEnchantments enchantments = ItemEnchantments.EMPTY;

	private int sealingProgress = 0;

	public WoodenCrateBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.WOODEN_CRATE.get(), pos, state);
	}

	public void loadAdditional(CompoundTag nbt, Provider provider)
	{
		loadIEData(nbt, provider);
	}

	private void loadIEData(CompoundTag nbt, Provider provider)
	{
		if(nbt.contains("enchantments"))
			this.enchantments = IECodecs.fromNbtOrThrow(ItemEnchantments.CODEC, nbt.get("enchantments"));
		else
			this.enchantments = ItemEnchantments.EMPTY;
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.loadAllItems(nbt, inventory, provider);
		this.sealingProgress = nbt.getIntOr("sealingProgress", 0);
	}

	protected void saveAdditional(CompoundTag nbt, Provider provider)
	{
		nbt.put("enchantments", IECodecs.toNbtOrThrow(ItemEnchantments.CODEC, enchantments));
		blusunrize.immersiveengineering.common.util.ContainerHelperCompat.saveAllItems(nbt, inventory, provider);
		nbt.putInt("sealingProgress", this.sealingProgress);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this, (be, access) -> {
			CompoundTag nbttagcompound = new CompoundTag();
			this.saveAdditional(nbttagcompound, access);
			return nbttagcompound;
		});
	}

	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider access)
	{
		CompoundTag nonNullTag = pkt.getTag()!=null?pkt.getTag(): new CompoundTag();
		if(nonNullTag.contains("CustomName"))
		{
			Component customName = Component.literal(nonNullTag.getStringOr("CustomName", ""));
			if(customName!=null)
				this.setCustomName(customName);
		}
	}

	public CompoundTag getUpdateTag(Provider provider)
	{
		CompoundTag nbt = super.getUpdateTag(provider);
		if(getCustomName()!=null)
			nbt.putString("CustomName", getCustomName().getString());
		return nbt;
	}

	protected Component getDefaultName()
	{
		Block b = getBlockState().getBlock();
		if(b==WoodenDevices.REINFORCED_CRATE.get())
			return Component.translatable("block.immersiveengineering.reinforced_crate");
		else
			return Component.translatable("block.immersiveengineering.crate");
	}

	protected NonNullList<ItemStack> getItems()
	{
		return inventory;
	}

	protected void setItems(NonNullList<ItemStack> pItemStacks)
	{
		this.inventory = pItemStacks;
	}

	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory)
	{
		return new CrateMenu(IEMenuTypes.WOODEN_CRATE.get(), pContainerId, pInventory, this);
	}

	@Nonnull
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	public boolean isStackValid(int slot, ItemStack stack)
	{
		return IEApi.isAllowedInCrate(stack);
	}

	public int getSlotLimit(int slot)
	{
		return 64;
	}

	public void doGraphicalUpdates()
	{
		this.setChanged();
		// on minecarts, this can be null
		if(this.level!=null)
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
	}

	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		if(isSealed())
			stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.inventory));
		else
			this.inventory.forEach(drop);
		Component customName = getCustomName();
		if(customName!=null)
			stack.set(DataComponents.CUSTOM_NAME, customName);
		stack.set(DataComponents.ENCHANTMENTS, enchantments);
		drop.accept(stack);
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		onBEPlaced(ctx.getItemInHand());
	}

	public void onBEPlaced(ItemStack stack)
	{
		enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		// Inventory contents are loaded by vanilla via applyImplicitComponents
	}

	public boolean canOpen(Player player)
	{
		return super.canOpen(player)&&!isSealed();
	}

	public boolean isSealed()
	{
		return sealingProgress >= HITS_TO_SEAL;
	}

	private final IItemHandler inventoryCap = new IEInventoryHandler(CONTAINER_SIZE, this);

	public static void registerCapabilities(BECapabilityRegistrar<WoodenCrateBlockEntity> registrar)
	{
		registrar.registerAllContexts(Capabilities.Item.BLOCK, be -> be.inventoryCap);
	}

	public IItemHandler getInventoryCap()
	{
		return inventoryCap;
	}

	public boolean canPlaceItem(int index, ItemStack stack)
	{
		return isStackValid(index, stack);
	}

	public int getComparatorInputOverride()
	{
		return Utils.calcRedstoneFromInventory(this);
	}

	public int getContainerSize()
	{
		return CONTAINER_SIZE;
	}

	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(heldItem.is(IETags.hammers)&&player.isCrouching())
		{
			if(!player.getCooldowns().isOnCooldown(heldItem)&&!isSealed())
			{
				if(++sealingProgress >= HITS_TO_SEAL)
					player.sendOverlayMessage(Component.translatable(Lib.CHAT_INFO+"crate_sealed"));
				player.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR);
				player.getCooldowns().addCooldown(heldItem, 10);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	@Nullable
	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult mop, boolean hammer)
	{
		Component customName = getCustomName();
		if(customName!=null)
			return new Component[]{getCustomName()};
		return null;
	}

	public void setCustomName(Component customName)
	{
		((BaseContainerBEAccess)this).setName(customName);
	}
}
