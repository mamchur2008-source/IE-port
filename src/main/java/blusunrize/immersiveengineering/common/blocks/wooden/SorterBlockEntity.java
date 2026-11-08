/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IInteractionObjectIE;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntIterators;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class SorterBlockEntity extends IEBaseBlockEntity implements IInteractionObjectIE<SorterBlockEntity>, IBlockEntityDrop
{
	public static final int FILTER_SLOTS_PER_SIDE = 8;
	public static final int TOTAL_SLOTS = 6*SorterBlockEntity.FILTER_SLOTS_PER_SIDE;
	public static final DualCodec<ByteBuf, Map<Direction, FilterConfig>> FILTER_CODEC = IEDualCodecs.forMap(
			IEDualCodecs.forEnum(Direction.values()), FilterConfig.CODEC
	);

	public SorterInventory filter;
	public Map<Direction, FilterConfig> sideFilter = Util.make(new EnumMap<>(Direction.class), l -> {
		for(Direction d : Direction.values())
			l.put(d, FilterConfig.DEFAULT);
	});
	/**
	 * The positions of the routers that have been used in the current "outermost" `routeItem` call.
	 * Necessary to stop "blocks" of routers (and similar setups) from causing massive lag (using just a boolean
	 * results in every possible path to be "tested"). Using a set results in effectively a DFS.
	 */
	private static Set<BlockPos> routed = null;

	@SuppressWarnings({"rawtypes", "unchecked"})
	private final Map<Direction, IEBlockCapabilityCache<IItemHandler>> neighborCaps = (Map)IEBlockCapabilityCaches.allNeighbors(
			Capabilities.Item.BLOCK, this
	);

	public SorterBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.SORTER.get(), pos, state);
		filter = new SorterInventory();
	}

	public ItemStack routeItem(Direction inputSide, ItemStack stack, boolean simulate)
	{
		if(!level.isClientSide()&&canRoute())
		{
			boolean first = startRouting();
			TransferPaths paths = getValidOutputs(inputSide, stack);
			if(!paths.filteredSides.isEmpty())
				stack = doInsert(stack, paths.filteredSides.toArray(Direction[]::new), simulate);
			else
				// Only if no filtered outputs were found, use unfiltered
				stack = doInsert(stack, paths.unfilteredSides.toArray(Direction[]::new), simulate);
			if(first)
				routed = null;
		}
		return stack;
	}

	private boolean canRoute()
	{
		return routed==null||!routed.contains(worldPosition);
	}

	private boolean startRouting()
	{
		boolean first = routed==null;
		if(first)
			routed = new HashSet<>();
		routed.add(worldPosition);
		return first;
	}

	private ItemStack doInsert(ItemStack stack, Direction[] sides, boolean simulate)
	{
		int lengthFiltered = sides.length;
		while(lengthFiltered > 0&&!stack.isEmpty())
		{
			int rand = ApiUtils.getRandom().nextInt(lengthFiltered);
			stack = this.outputItemToInv(stack, sides[rand], simulate);
			sides[rand] = sides[lengthFiltered-1];
			lengthFiltered--;
		}
		return stack;
	}

	public boolean canUseGui(Player player)
	{
		return true;
	}

	public SorterBlockEntity getGuiMaster()
	{
		return this;
	}

	public ArgContainer<SorterBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.SORTER;
	}

	public TransferPaths getValidOutputs(Direction inputSide, ItemStack stack)
	{
		if(stack.isEmpty())
			return TransferPaths.EMPTY;
		TransferPaths paths = new TransferPaths();
		for(Direction side : Direction.values())
			if(side!=inputSide)
			{
				EnumFilterResult result = checkStackAgainstFilter(stack, side);
				if(result==EnumFilterResult.VALID_FILTERED)
					paths.filteredSides.add(side);
				else if(result==EnumFilterResult.VALID_UNFILTERED)
					paths.unfilteredSides.add(side);
			}

		return paths;
	}

	public ItemStack pullItem(Direction outputSide, int amount, boolean simulate)
	{
		if(!level.isClientSide()&&canRoute())
		{
			boolean first = startRouting();
			for(Direction side : Direction.values())
				if(side!=outputSide)
				{
					IEBlockCapabilityCache<IItemHandler> capRef = neighborCaps.get(side);
					IItemHandler itemHandler = capRef.getCapability();
					if(itemHandler!=null)
					{
						Predicate<ItemStack> concatFilter = null;
						for(int i = 0; i < itemHandler.getSlots(); i++)
						{
							ItemStack extractItem = itemHandler.extractItem(i, amount, true);
							if(!extractItem.isEmpty())
							{
								if(concatFilter==null)//Init the filter here, to save on resources
									concatFilter = this.concatFilters(side, outputSide);
								if(concatFilter.test(extractItem))
								{
									if(first)
										routed = null;
									if(!simulate)
										itemHandler.extractItem(i, amount, false);
									return extractItem;
								}
							}
						}
					}
				}
			if(first)
				routed = null;
		}
		return ItemStack.EMPTY;
	}

	private static DataComponentMap getComponentsWithoutDamage(ItemStack stack)
	{
		return stack.getComponents().filter(type -> type!=DataComponents.DAMAGE);
	}

	/**
	 * @param stack the stack to check
	 * @param side  the side the filter is on
	 * @return If the stack is permitted by the given filter
	 */
	private EnumFilterResult checkStackAgainstFilter(ItemStack stack, Direction side)
	{
		boolean unmapped = true;
		for(Pair<ItemStack, FilterTag> filterStack : filter.getFilterStacksOnSide(side))
			if(!filterStack.getFirst().isEmpty())
			{
				unmapped = false;
				if(sideFilter.get(side).compareStackToFilterstack(stack, filterStack.getFirst(), filterStack.getSecond()))
					return EnumFilterResult.VALID_FILTERED;
			}
		if(unmapped)
			return EnumFilterResult.VALID_UNFILTERED;
		return EnumFilterResult.INVALID;
	}

	/**
	 * @return A Predicate representing the concatinated filters of two sides.<br>
	 * If one filter is empty, uses the full filter of the other side, else the matching items make up the filter
	 */
	private Predicate<ItemStack> concatFilters(Direction sideFrom, Direction sideTo)
	{
		final var filterFrom = sideFilter.get(sideFrom);
		final var filterTo = sideFilter.get(sideTo);

		// Build lists without emtpies
		final List<Pair<ItemStack, FilterTag>> stacksFrom = new ArrayList<>();
		for(Pair<ItemStack, FilterTag> filterStack : filter.getFilterStacksOnSide(sideFrom))
			if(!filterStack.getFirst().isEmpty())
				stacksFrom.add(filterStack);
		final List<Pair<ItemStack, FilterTag>> stacksTo = new ArrayList<>();
		for(Pair<ItemStack, FilterTag> filterStack : filter.getFilterStacksOnSide(sideTo))
			if(!filterStack.getFirst().isEmpty())
				stacksTo.add(filterStack);

		// If there is nothing configured, simply return true
		if(stacksFrom.isEmpty()&&stacksTo.isEmpty())
			return stack -> true;
		// If only sideFrom is filtered
		if(stacksTo.isEmpty())
			return stack -> stacksFrom.stream().anyMatch(pair -> filterFrom.compareStackToFilterstack(stack, pair.getFirst(), pair.getSecond()));
		// If only sideTo is filtered
		if(stacksFrom.isEmpty())
			return stack -> stacksTo.stream().anyMatch(pair -> filterTo.compareStackToFilterstack(stack, pair.getFirst(), pair.getSecond()));

		// If both are filled, then we build combined predicates
		List<Predicate<ItemStack>> combinedPredicates = stacksFrom.stream().flatMap(pairFrom -> {
			Builder<Predicate<ItemStack>> builder = Stream.builder();
			stacksTo.forEach(pairTo -> {
				if(filterFrom.compareStackToFilterstack(pairTo.getFirst(), pairFrom.getFirst(), pairFrom.getSecond()))
					builder.accept(itemStack ->
							filterFrom.compareStackToFilterstack(itemStack, pairFrom.getFirst(), pairFrom.getSecond())
									&&filterTo.compareStackToFilterstack(itemStack, pairTo.getFirst(), pairTo.getSecond())
					);
			});
			return builder.build();
		}).toList();

		return combinedPredicates.isEmpty()?stack -> false: stack -> {
			for(Predicate<ItemStack> p : combinedPredicates)
				if(p.test(stack))
					return true;
			return false;
		};
	}

	public ItemStack outputItemToInv(ItemStack stack, Direction side, boolean simulate)
	{
		return Utils.insertStackIntoInventory(neighborCaps.get(side), stack, simulate);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		sideFilter = FILTER_CODEC.fromNBT(nbt.get("sideFilter"));
		if(!descPacket)
		{
			ListTag filterList = nbt.getListOrEmpty("filter");
			filter = new SorterInventory();
			filter.readFromNBT(provider, filterList);
		}
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		nbt.put("sideFilter", FILTER_CODEC.toNBT(sideFilter));
		if(!descPacket)
		{
			ListTag filterList = new ListTag();
			filter.writeToNBT(provider, filterList);
			nbt.put("filter", filterList);
		}
	}

	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		CompoundTag data = new CompoundTag();
		writeCustomNBT(data, false, context.getLevel().registryAccess());
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(this.getType(), data));
		drop.accept(stack);
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final var data = ctx.getItemInHand().get(DataComponents.BLOCK_ENTITY_DATA);
		if(data!=null)
			readCustomNBT(data.copyTagWithoutId(), false, ctx.getLevel().registryAccess());
	}

	private final EnumMap<Direction, IItemHandler> insertionHandlers = new EnumMap<>(Direction.class);

	{
		for(Direction f : DirectionUtils.VALUES)
			insertionHandlers.put(f, new SorterInventoryHandler(this, f));
	}

	public static void registerCapabilities(BECapabilityRegistrar<SorterBlockEntity> registrar)
	{
		registrar.register(Capabilities.Item.BLOCK, (be, facing) -> facing!=null?be.insertionHandlers.get(facing): null);
	}

	public boolean triggerEvent(int id, int arg)
	{
		return id==0;
	}

	public static class SorterInventoryHandler implements IItemHandlerModifiable
	{
		SorterBlockEntity sorter;
		Direction side;

		public SorterInventoryHandler(SorterBlockEntity sorter, Direction side)
		{
			this.sorter = sorter;
			this.side = side;
		}

		public int getSlots()
		{
			return 1;
		}

		public ItemStack getStackInSlot(int slot)
		{
			return ItemStack.EMPTY;
		}

		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			return sorter.routeItem(this.side, stack, simulate);
		}

		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			return sorter.pullItem(this.side, amount, simulate);
		}

		public int getSlotLimit(int slot)
		{
			return 64;
		}

		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return true;
		}

		public void setStackInSlot(int slot, ItemStack stack)
		{
		}
	}

	public static class SorterInventory extends ItemStackHandler
	{
		private final FilterTag[] selectedTags = new FilterTag[TOTAL_SLOTS];

		public SorterInventory()
		{
			super(NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY));
		}

		public ItemStack getStackBySideAndSlot(Direction side, int slotOnSide)
		{
			return getStackInSlot(getSlotId(side, slotOnSide));
		}

		public int getSlotId(Direction side, int slotOnSide)
		{
			return side.ordinal()*FILTER_SLOTS_PER_SIDE+slotOnSide;
		}

		public int getSlotLimit(int slot)
		{
			return 1;
		}

		public Iterable<Pair<ItemStack, FilterTag>> getFilterStacksOnSide(Direction side)
		{
			return () -> Iterators.transform(
					IntIterators.fromTo(0, FILTER_SLOTS_PER_SIDE), i -> Pair.of(
							getStackBySideAndSlot(side, i),
							this.selectedTags[getSlotId(side, i)]
					)
			);
		}

		public void setStackInSlot(int slot, ItemStack stack)
		{
			ItemStack prev = getStackInSlot(slot);
			super.setStackInSlot(slot, stack);
			// reset selected tag
			if(!ItemStack.isSameItem(prev, stack))
				selectedTags[slot] = null;
		}

		public void setSelectedTag(int slot, @Nullable final Identifier location)
		{
			this.selectedTags[slot] = FilterTag.deserialize(this.getStackInSlot(slot), location);
		}

		@Nullable
		public Identifier getSelectedTag(int slot)
		{
			return this.selectedTags[slot]==null?null: this.selectedTags[slot].serialize();
		}

		public void writeToNBT(Provider provider, ListTag list)
		{
			for(int i = 0; i < getSlots(); ++i)
			{
				ItemStack stackInSlot = getStackInSlot(i);
				if(!stackInSlot.isEmpty())
				{
					CompoundTag slotTag = new CompoundTag();
					slotTag.putByte("Slot", (byte)i);
					if(this.selectedTags[i]!=null)
						this.selectedTags[i].writeToNbt(slotTag);
					ItemStack.OPTIONAL_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stackInSlot)
							.result()
							.flatMap(Tag::asCompound)
							.ifPresent(stackTag -> list.add(stackTag.merge(slotTag)));
				}
			}
		}

		public void readFromNBT(Provider provider, ListTag list)
		{
			for(int i = 0; i < list.size(); i++)
			{
				CompoundTag slotTag = list.getCompound(i).orElseGet(CompoundTag::new);
				int slot = slotTag.getByteOr("Slot", (byte)0)&255;
				if(slot < getSlots())
				{
					ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), slotTag)
							.result().orElse(ItemStack.EMPTY);
					setStackInSlot(slot, stack);
					this.selectedTags[slot] = FilterTag.readFromNbt(slotTag, stack);
				}
			}
		}
	}

	private enum EnumFilterResult
	{
		INVALID,
		VALID_FILTERED,
		VALID_UNFILTERED
	}

	public record FilterConfig(boolean allowTags, boolean considerComponents, boolean ignoreDamage)
	{
		public static final FilterConfig DEFAULT = new FilterConfig(false, false, false);
		public static final DualCodec<ByteBuf, FilterConfig> CODEC = DualCompositeCodecs.composite(
				DualCodecs.BOOL.fieldOf("allowTags"), FilterConfig::allowTags,
				DualCodecs.BOOL.fieldOf("considerComponents"), FilterConfig::considerComponents,
				DualCodecs.BOOL.fieldOf("ignoreDamage"), FilterConfig::ignoreDamage,
				FilterConfig::new
		);

		public boolean compareStackToFilterstack(ItemStack stack, ItemStack filterStack, @Nullable FilterTag tag)
		{
			// "Item level" tests
			if(allowTags&&tag!=null)
			{
				if(!tag.test(stack))
					return false;
			}
			else if(!ItemStack.isSameItem(filterStack, stack))
				return false;
			// "NBT level" tests
			if(!ignoreDamage&&(stack.isDamageableItem()||filterStack.isDamageableItem()))
			{
				final int damageStack = stack.getDamageValue();
				final int damageFilter = filterStack.getDamageValue();
				if(damageStack!=damageFilter)
					return false;
			}
			if(considerComponents)
			{
				final var stackTag = getComponentsWithoutDamage(stack);
				final var filterTag = getComponentsWithoutDamage(filterStack);
				if(!stackTag.equals(filterTag))
					return false;
			}
			return true;
		}
	}

	public record TransferPaths(List<Direction> filteredSides, List<Direction> unfilteredSides)
	{
		public static final TransferPaths EMPTY = new TransferPaths(List.of(), List.of());

		public TransferPaths()
		{
			this(new ArrayList<>(6), new ArrayList<>(6));
		}
	}

	private static final String COMMON_NAMESPACE = "c";
	private static final Comparator<TagKey<Item>> TAG_SORTER = (o1, o2) -> {
		Identifier rl1 = o1.location();
		Identifier rl2 = o2.location();
		// common namespace always comes first
		if(COMMON_NAMESPACE.equals(rl1.getNamespace())&&!COMMON_NAMESPACE.equals(rl2.getNamespace()))
			return -1;
		if(!COMMON_NAMESPACE.equals(rl1.getNamespace())&&COMMON_NAMESPACE.equals(rl2.getNamespace()))
			return 1;
		return rl1.compareNamespaced(rl2);
	};


	public record FilterTag(Either<TagKey<Item>, String> inner) implements Predicate<ItemStack>
	{
		public FilterTag(TagKey<Item> tag)
		{
			this(Either.left(tag));
		}

		public boolean test(ItemStack stack)
		{
			return inner().map(stack::is, modId -> modId.equals(Utils.getModIdForItemStack(stack)));
		}

		public Component getComponent()
		{
			return inner().map((Function<TagKey<Item>, Component>)tag -> {
				String tagTranslationKey = Tags.getTagTranslationKey(tag);
				return Component.translatableWithFallback(tagTranslationKey, "#"+tag.location());
			}, modId -> {
				return Component.translatable(Lib.DESC_INFO+"filter.tag.from_mod", Utils.getModName(modId));
			});
		}

		public Identifier serialize()
		{
			return inner().map(TagKey::location, modid -> Identifier.fromNamespaceAndPath("modid", modid));
		}

		@Nullable
		public static FilterTag deserialize(ItemStack stack, @Nullable Identifier location)
		{
			if(location==null)
				return null;
			if(location.getNamespace().equals("modid"))
				return new FilterTag(Either.right(location.getPath()));
			return stack.typeHolder().tags().filter(t -> t.location().equals(location))
					.findFirst().map(FilterTag::new).orElse(null);
		}


		@Nullable
		public static FilterTag readFromNbt(CompoundTag slotTag, ItemStack stack)
		{
			if(slotTag.contains("selectedMod"))
				return new FilterTag(Either.right(slotTag.getStringOr("selectedMod", "")));
			if(slotTag.contains("selectedTag"))
			{
				Identifier rl = Identifier.parse(slotTag.getStringOr("selectedTag", ""));
				return stack.typeHolder().tags()
						.filter(t -> t.location().equals(rl))
						.findFirst()
						.map(itemTagKey -> new FilterTag(Either.left(itemTagKey)))
						.orElse(null);
			}
			return null;
		}

		public void writeToNbt(CompoundTag slotTag)
		{
			this.inner().ifLeft(tag -> slotTag.putString("selectedTag", tag.location().toString()))
					.ifRight(mod -> slotTag.putString("selectedMod", mod));
		}

		public static List<Identifier> getAvailableForItem(ItemStack stack)
		{
			List<Identifier> list = new ArrayList<>(stack.typeHolder().tags().sorted(TAG_SORTER).map(TagKey::location).toList());
			list.add(Identifier.fromNamespaceAndPath("modid", Utils.getModIdForItemStack(stack)));
			return list;
		}

	}
}
