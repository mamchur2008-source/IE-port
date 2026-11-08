/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public class PotionBucketItem extends IEBaseItem
{
	public PotionBucketItem()
	{
		super(itemProperties().stacksTo(1).component(POTION_CONTENTS, PotionContents.EMPTY));
	}

	public static ItemStack forPotion(Holder<Potion> type)
	{
		if(type==Potions.WATER||type==null)
			return new ItemStack(Items.WATER_BUCKET);
		ItemStack result = new ItemStack(Misc.POTION_BUCKET);
		result.set(POTION_CONTENTS, new PotionContents(type));
		return result;
	}

	public void fillCreativeTab(Output out)
	{
	}

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registrar.register(Capabilities.Fluid.ITEM, (stack, context) -> new FluidHandler(stack, context));
	}

	@Nonnull
	public Component getName(@Nonnull ItemStack stack)
	{
		return Component.translatable(
				"item.immersiveengineering.potion_bucket", getPotionName(stack.get(POTION_CONTENTS).potion())
		);
	}

	private static Component getPotionName(Optional<Holder<Potion>> potion)
	{
		return potion.map(value -> new PotionContents(value).getName(Items.POTION.getDescriptionId()+".effect."))
				.orElse(Component.empty());
	}

	@Nonnull
	public InteractionResult use(
			@Nonnull Level worldIn, @Nonnull Player playerIn, @Nonnull InteractionHand handIn
	)
	{
		ItemStack stack = playerIn.getItemInHand(handIn);
		return InteractionResult.PASS;
	}

	public void appendHoverText(
			@Nonnull ItemStack stack, TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn
	)
	{
		PotionContents contents = stack.getOrDefault(POTION_CONTENTS, PotionContents.EMPTY);
		PotionContents.addPotionTooltip(contents.getAllEffects(), tooltip::add, 1.0F, ctx.tickRate());
	}

	private static class FluidHandler extends ItemAccessResourceHandler<FluidResource>
	{
		private final net.minecraft.world.item.Item validItem;

		private FluidHandler(ItemStack stack, Object context)
		{
			super(context instanceof ItemAccess itemAccess?itemAccess: ItemAccess.forStack(stack), 1);
			this.validItem = stack.getItem();
		}

		private FluidStack getFluid(ItemResource resource)
		{
			if(!resource.is(validItem))
				return FluidStack.EMPTY;
			PotionContents contents = resource.toStack().getOrDefault(POTION_CONTENTS, PotionContents.EMPTY);
			return PotionFluid.getFluidStackForType(
					contents.potion(), FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.REGULAR
			);
		}

		@Override
		protected FluidResource getResourceFrom(ItemResource resource, int tank)
		{
			FluidStack fluid = getFluid(resource);
			return fluid.isEmpty()?FluidResource.EMPTY: FluidResource.of(fluid);
		}

		@Override
		protected int getAmountFrom(ItemResource resource, int tank)
		{
			return getFluid(resource).isEmpty()?0: FluidType.BUCKET_VOLUME;
		}

		@Override
		protected ItemResource update(ItemResource resource, int tank, FluidResource fluid, int amount)
		{
			if(amount <= 0)
				return ItemResource.of(Items.BUCKET);
			return resource;
		}

		@Override
		public boolean isValid(int tank, FluidResource resource)
		{
			return !resource.isEmpty();
		}

		@Override
		protected int getCapacity(int tank, FluidResource resource)
		{
			return FluidType.BUCKET_VOLUME;
		}
	}
}
