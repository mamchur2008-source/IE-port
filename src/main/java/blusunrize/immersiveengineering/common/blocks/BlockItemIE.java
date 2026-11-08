/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BlockItemIE extends BlockItem
{
	private int burnTime;

	public BlockItemIE(Block b, Item.Properties props)
	{
		super(b, props.useBlockDescriptionPrefix());
	}

	public BlockItemIE(Block b)
	{
		this(b, IEItems.defaultProperties());
	}

	public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag advanced)
	{
		if(getBlock() instanceof IIEBlock ieBlock&&ieBlock.hasFlavour())
		{
			String flavourKey = Lib.DESC_FLAVOUR+ieBlock.getNameForFlavour();
			tooltip.accept(TextUtils.applyFormat(Component.translatable(flavourKey), ChatFormatting.GRAY));
		}
		super.appendHoverText(stack, ctx, display, tooltip, advanced);
		if(stack.has(IEDataComponents.GENERIC_ENERGY))
			tooltip.accept(TextUtils.applyFormat(
					Component.translatable(Lib.DESC_INFO+"energyStored", stack.get(IEDataComponents.GENERIC_ENERGY)),
					ChatFormatting.GRAY
			));
		if(stack.has(IEDataComponents.GENERIC_FLUID))
		{
			var fs = stack.get(IEDataComponents.GENERIC_FLUID).copy();
			tooltip.accept(Component.translatable(
					Lib.DESC_INFO+"fluidStored", fs.getHoverName(), fs.getAmount()
			).withStyle(ChatFormatting.GRAY));
		}
	}


	public BlockItemIE setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return this.burnTime;
	}

	protected boolean placeBlock(BlockPlaceContext context, BlockState newState)
	{
		Block b = newState.getBlock();
		if(b instanceof IEBaseBlock ieBlock)
		{
			if(!ieBlock.canIEBlockBePlaced(newState, context))
				return false;
			boolean ret = super.placeBlock(context, newState);
			if(ret)
				ieBlock.onIEBlockPlacedBy(context, newState);
			return ret;
		}
		else
			return super.placeBlock(context, newState);
	}

	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state)
	{
		// Skip reading the tile from NBT if the block is a (general) multiblock
		if(!state.hasProperty(IEProperties.MULTIBLOCKSLAVE))
			return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
		else
			return false;
	}

	@Nonnull
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack)
	{
		final ItemContainerContents items = stack.get(DataComponents.CONTAINER);
		if(items!=null)
			return super.getTooltipImage(stack);
		return super.getTooltipImage(stack);
	}

	public boolean canFitInsideContainerItems()
	{
		return !(getBlock() instanceof IEBaseBlock ieBlock)||ieBlock.fitsIntoContainer();
	}
}
