/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class IEMinecartEntity<T extends BlockEntity> extends AbstractMinecart implements MenuProvider
{
	private static final EntityDataAccessor<String> DATA_ID_BE_DATA = SynchedEntityData.defineId(IEMinecartEntity.class, EntityDataSerializers.STRING);
	protected T containedBlockEntity;

	protected IEMinecartEntity(EntityType<?> type, Level world, double x, double y, double z)
	{
		super(type, world, x, y, z);
		this.containedBlockEntity = getTileProvider().get();
	}

	protected IEMinecartEntity(EntityType<?> type, Level world)
	{
		super(type, world);
		this.containedBlockEntity = getTileProvider().get();
	}

	protected abstract Supplier<T> getTileProvider();

	public T getContainedBlockEntity()
	{
		return containedBlockEntity;
	}

	public abstract void writeTileToItem(ItemStack itemStack);

	public abstract void readTileFromItem(LivingEntity placer, ItemStack itemStack);

	public void destroy(@Nonnull DamageSource source)
	{
		if(this.level() instanceof ServerLevel serverLevel)
			this.kill(serverLevel);
		if(this.level() instanceof ServerLevel serverLevel&&serverLevel.getGameRules().get(GameRules.ENTITY_DROPS))
		{
			ItemStack itemstack = getPickResult();
			this.writeTileToItem(itemstack);
			if(this.hasCustomName())
				itemstack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
			this.spawnAtLocation(serverLevel, itemstack);
		}
	}

	public int getComparatorLevel()
	{
		if(this.containedBlockEntity instanceof IComparatorOverride)
			return ((IComparatorOverride)this.containedBlockEntity).getComparatorInputOverride();
		return 0;
	}

	@Nonnull
	public InteractionResult interact(@Nonnull Player player, @Nonnull InteractionHand hand)
	{
		InteractionResult superResult = super.interact(player, hand, Vec3.ZERO);
		if(superResult==InteractionResult.SUCCESS)
			return superResult;
		if(player instanceof ServerPlayer serverPlayer&&this.containedBlockEntity instanceof MenuProvider menuProvider)
		{
			if(menuProvider instanceof IInteractionObjectIE<?>)
				serverPlayer.openMenu(this, buffer -> buffer.writeInt(this.getId()));
			else
				serverPlayer.openMenu(this);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Nullable
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory playerInventory, @Nonnull Player playerEntity)
	{
		return null;
	}

	protected void defineSynchedData(Builder builder)
	{
		super.defineSynchedData(builder);
		builder.define(DATA_ID_BE_DATA, "");
	}


	protected void addAdditionalSaveData(@Nonnull ValueOutput compound)
	{
		super.addAdditionalSaveData(compound);
	}

	protected void readAdditionalSaveData(@Nonnull ValueInput compound)
	{
		super.readAdditionalSaveData(compound);
		this.containedBlockEntity = getTileProvider().get();
		this.updateSynchedData();
	}

	public void updateSynchedData()
	{
		this.getEntityData().set(DATA_ID_BE_DATA, "");
	}

	public void onSyncedDataUpdated(EntityDataAccessor<?> p_38527_)
	{
		super.onSyncedDataUpdated(p_38527_);
	}

	// This is only used by the super impl of destroy, which does not allow attaching NBT to the drop. So it's actually
	// unused for our minecarts
	protected Item getDropItem()
	{
		return Items.MINECART;
	}

	public interface MinecartConstructor
	{
		IEMinecartEntity<?> make(Level world, double x, double y, double z);
	}
}
