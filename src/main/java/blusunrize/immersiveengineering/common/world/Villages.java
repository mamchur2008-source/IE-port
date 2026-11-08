/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.world;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.world.Villages.RerollingItemListing.GenerateOffer;
import blusunrize.immersiveengineering.mixin.accessors.TemplatePoolAccess;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent.UpdateCause;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.register.IEItems.Misc.WIRE_COILS;

@EventBusSubscriber(modid = Lib.MODID)
public class Villages
{
	public interface ItemListing
	{
		@Nullable
		MerchantOffer getOffer(@Nullable Entity trader, RandomSource random);
	}

	public static final Identifier ENGINEER = rl("engineer");
	public static final Identifier MACHINIST = rl("machinist");
	public static final Identifier ELECTRICIAN = rl("electrician");
	public static final Identifier OUTFITTER = rl("outfitter");
	public static final Identifier GUNSMITH = rl("gunsmith");

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent ev)
	{
		if(ev.getUpdateCause()!=UpdateCause.SERVER_DATA_LOAD)
			return;
	}

	public static void init()
	{
		// Hero gift registration moved to immutable vanilla data in MC 26; leave custom gifts disabled for now.
	}

	private static void addToPool(Identifier poolId, Identifier toAdd, RegistryAccess regAccess)
	{
	}

	public static class Registers
	{
		public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ImmersiveEngineering.MODID);
		public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(
				BuiltInRegistries.VILLAGER_PROFESSION, ImmersiveEngineering.MODID
		);

		// TODO: Add more workstations. We need a different one for each profession
		public static final Holder<PoiType> POI_CRAFTINGTABLE = POINTS_OF_INTEREST.register(
				"craftingtable", () -> createPOI(assembleStates(WoodenDevices.CRAFTING_TABLE.get()))
		);
		public static final Holder<PoiType> POI_TURNTABLE = POINTS_OF_INTEREST.register(
				"turntable", () -> createPOI(assembleStates(WoodenDevices.TURNTABLE.get()))
		);
		public static final Holder<PoiType> POI_CIRCUITTABLE = POINTS_OF_INTEREST.register(
				"circuit_table", () -> createPOI(assembleStates(WoodenDevices.CIRCUIT_TABLE.get()))
		);
		public static final Holder<PoiType> POI_BANNER = POINTS_OF_INTEREST.register(
				"shaderbanner", () -> createPOI(assembleStates(Cloth.SHADER_BANNER.get()))
		);
		public static final Holder<PoiType> POI_WORKBENCH = POINTS_OF_INTEREST.register(
				"workbench", () -> createPOI(assembleStates(WoodenDevices.WORKBENCH.get()))
		);

		public static final Holder<VillagerProfession> PROF_ENGINEER = PROFESSIONS.register(
				ENGINEER.getPath(), () -> createProf(ENGINEER, POI_TURNTABLE, SoundEvents.VILLAGER_WORK_MASON)
		);
		public static final Holder<VillagerProfession> PROF_MACHINIST = PROFESSIONS.register(
				MACHINIST.getPath(), () -> createProf(MACHINIST, POI_CRAFTINGTABLE, SoundEvents.VILLAGER_WORK_TOOLSMITH)
		);
		public static final Holder<VillagerProfession> PROF_ELECTRICIAN = PROFESSIONS.register(
				ELECTRICIAN.getPath(), () -> createProf(ELECTRICIAN, POI_CIRCUITTABLE, IESounds.spark.value())
		);
		public static final Holder<VillagerProfession> PROF_OUTFITTER = PROFESSIONS.register(
				OUTFITTER.getPath(), () -> createProf(OUTFITTER, POI_BANNER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER)
		);
		public static final Holder<VillagerProfession> PROF_GUNSMITH = PROFESSIONS.register(
				GUNSMITH.getPath(), () -> createProf(GUNSMITH, POI_WORKBENCH, IESounds.revolverReload.value())
		);

		private static PoiType createPOI(Collection<BlockState> block)
		{
			return new PoiType(ImmutableSet.copyOf(block), 1, 1);
		}

		private static VillagerProfession createProf(
				Identifier name, Holder<PoiType> poi, SoundEvent sound
		)
		{
			ResourceKey<PoiType> poiName = poi.unwrapKey().orElseThrow();
			return new VillagerProfession(
					Component.literal(name.toString()),
					holder -> holder.is(poiName),
					holder -> holder.is(poiName),
					ImmutableSet.of(),
					ImmutableSet.of(),
					sound,
					it.unimi.dsi.fastutil.ints.Int2ObjectMaps.emptyMap()
			);
		}

		private static Collection<BlockState> assembleStates(Block block)
		{
			return block.getStateDefinition().getPossibleStates().stream().filter(blockState -> {
				if(blockState.hasProperty(IEProperties.MULTIBLOCKSLAVE))
					return !blockState.getValue(IEProperties.MULTIBLOCKSLAVE);
				return true;
			}).collect(Collectors.toList());
		}
	}

	@EventBusSubscriber(modid = MODID)
	public static class Events
	{
		public static void registerTrades(VillagerTradesEvent ev)
		{
			Int2ObjectMap<List<ItemListing>> trades = ev.getTrades();
			final Identifier typeName = Identifier.parse(ev.getType().name());
			if(ENGINEER.equals(typeName))
			{
				/* Structural Engineer
				 * Sells various construction materials
				 */
				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.getItemTag(IETags.treatedWood), WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL), 6, 16, 2));
				trades.get(1).add(new TradeListing(BUY_FOR_ONE_EMERALD, WoodenDecoration.TREATED_SCAFFOLDING, 6, 16, 1));
				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, Cloth.BALLOON, 2, 12, 2));

				trades.get(2).add(new TradeListing(BUY_FOR_ONE_EMERALD, MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), 4, 12, 5));
				trades.get(2).add(new TradeListing(BUY_FOR_ONE_EMERALD, MetalDecoration.STEEL_CATWALK, 4, 12, 5));
				trades.get(2).add(new TradeListing(BUY_FOR_ONE_EMERALD, MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), 4, 12, 5));
				trades.get(2).add(new TradeListing(BUY_FOR_ONE_EMERALD, MetalDecoration.ALU_CATWALK, 4, 12, 5));

				trades.get(3).add(new BasicItemListing(new ItemStack(Connectors.CONNECTOR_STRUCTURAL), new ItemStack(WIRE_COILS.get(WireType.STRUCTURE_ROPE)), new ItemStack(Items.EMERALD), 16, 20, 0.05f));
				trades.get(3).add(new TradeListing(SELL_FOR_ONE_EMERALD, StoneDecoration.CONCRETE, 6, 12, 15));
				trades.get(3).add(new TradeListing(BUY_FOR_ONE_EMERALD, StoneDecoration.CONCRETE_LEADED, 4, 16, 10));

				trades.get(4).add(new TradeListing(SELL_FOR_ONE_EMERALD, StoneDecoration.INSULATING_GLASS, 4, 16, 30));
				trades.get(4).add(new TradeListing(SELL_FOR_ONE_EMERALD, StoneDecoration.CLINKER_BRICK, 8, 16, 10));
				trades.get(4).add(OreveinMapForEmeralds.INSTANCE);

				trades.get(5).add(new TradeListing(SELL_FOR_ONE_EMERALD, StoneDecoration.DUROPLAST, 4, 16, 30));
				trades.get(5).add(OreveinMapForEmeralds.INSTANCE);
			}
			else if(MACHINIST.equals(typeName))
			{
				/* Machinist
				 * Sells components, engineering blocks, and tool-heads
				 */
				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.coalCoke, Ingredients.COAL_COKE, 8, 16, 2));
				trades.get(1).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Tools.HAMMER, 1, 4, 5));
				trades.get(1).add(new TradeListing(BUY_FOR_MANY_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("components"), 4, 3, 5).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.getTagsFor(EnumMetals.COPPER).ingot, Items.COPPER_INGOT, 4, 12, 10));
				trades.get(2).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Ingredients.COMPONENT_IRON, 2, 12, 5));
				trades.get(2).add(new TradeListing(BUY_FOR_MANY_EMERALDS, WoodenDecoration.BASIC_ENGINEERING, 4, 12, 5));

				trades.get(3).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.getTagsFor(EnumMetals.STEEL).ingot, IEItems.Metals.INGOTS.get(EnumMetals.STEEL), 3, 12, 20));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Ingredients.COMPONENT_STEEL, 3, 12, 10));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, MetalDecoration.ENGINEERING_LIGHT, 4, 12, 10));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, MetalDevices.FLUID_PIPE, 2, 12, 10));

				trades.get(4).add(new TradeListing(BUY_FOR_MANY_EMERALDS, MetalDecoration.ENGINEERING_HEAVY, 5, 12, 15));
				trades.get(4).add(new TradeListing(BUY_FOR_MANY_EMERALDS, ConveyorHandler.getBlock(BasicConveyor.TYPE), 2, 12, 15));
				trades.get(4).add(new TradeListing(SELL_FOR_MANY_EMERALDS, MetalDecoration.GENERATOR, 4, 12, 30));
				trades.get(4).add(new TradeListing(SELL_FOR_MANY_EMERALDS, MetalDecoration.RADIATOR, 4, 12, 30));

				trades.get(5).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Tools.DRILLHEAD_STEEL, 24, 3, 30).setMultiplier(0.2f));
				trades.get(5).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Tools.GRINDINGDISK, 24, 3, 30).setMultiplier(0.2f));
			}
			else if(ELECTRICIAN.equals(typeName))
			{
				/* Electrician
				 * Sells wires, components and the faraday suit
				 */
				trades.get(1).add(new TradeListing(BUY_FOR_ONE_EMERALD, IETags.copperWire, Ingredients.WIRE_COPPER, 4, 16, 2));
				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, WIRE_COILS.get(WireType.COPPER), 2, 16, 1));
				trades.get(1).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Tools.WIRECUTTER, 2, 12, 1).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.electrumWire, Ingredients.WIRE_ELECTRUM, 6, 12, 10));
				trades.get(2).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.aluminumWire, Ingredients.WIRE_ALUMINUM, 6, 12, 10));
				trades.get(2).add(new TradeListing(BUY_FOR_MANY_EMERALDS, MetalDevices.ELECTRIC_LANTERN, 4, 12, 5));

				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorType.BOOTS), 4, 3, 15).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorType.HELMET), 5, 3, 15).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorType.CHESTPLATE), 9, 3, 15).setMultiplier(0.2f));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, IEItems.Misc.FARADAY_SUIT.get(ArmorType.LEGGINGS), 7, 3, 15).setMultiplier(0.2f));

				trades.get(4).add(new TradeListing(SELL_FOR_ONE_EMERALD, Ingredients.LIGHT_BULB, 3, 12, 30));
				trades.get(4).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Ingredients.COMPONENT_ELECTRONIC, 4, 16, 15));
				trades.get(4).add(new TradeListing(SELL_FOR_MANY_EMERALDS, IEItems.Misc.FLUORESCENT_TUBE, 16, 8, 30));

				trades.get(5).add(new TradeListing(BUY_FOR_MANY_EMERALDS, Ingredients.ELECTRON_TUBE, 8, 12, 30));
				trades.get(5).add(new TradeListing(BUY_FOR_MANY_EMERALDS, MetalDevices.TESLA_COIL, 24, 12, 30));
			}
			else if(OUTFITTER.equals(typeName))
			{
				/* Outfitter
				 * Sells Shaderbags and purchases cosmetic material
				 */
				ItemLike bag_common = IEItems.Misc.SHADER_BAG.get(Rarity.COMMON);
				ItemLike bag_uncommon = IEItems.Misc.SHADER_BAG.get(Rarity.UNCOMMON);
				ItemLike bag_rare = IEItems.Misc.SHADER_BAG.get(Rarity.RARE);
				ItemLike bag_epic = IEItems.Misc.SHADER_BAG.get(Rarity.EPIC);

				// why is there no array for this in vanilla :'D
				ItemLike[] dyes = {
						Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
						Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
						Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
						Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE
				};

				ItemLike[] purchasedPatterns = {
						IEBannerPatterns.BEVELS.item(),
						IEBannerPatterns.TREATED_WOOD.item(),
						IEBannerPatterns.ORNATE.item(),
				};

				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, Ingredients.HEMP_FABRIC, 8, 16, 2));
				trades.get(1).add((entity, randomSource) -> new MerchantOffer(
						new ItemCost(Items.EMERALD),
						new ItemStack(dyes[randomSource.nextInt(dyes.length)], 4),
						16, 1, 0.05f
				));

				trades.get(2).add((entity, randomSource) -> new MerchantOffer(
						new ItemCost(purchasedPatterns[randomSource.nextInt(purchasedPatterns.length)]),
						new ItemStack(Items.EMERALD, 2),
						12, 10, 0.05f
				));
				trades.get(2).add(new TradeListing(BUY_FOR_MANY_EMERALDS, bag_common, 4, 12, 5));

				trades.get(3).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.getTagsFor(EnumMetals.SILVER).dust, IEItems.Metals.DUSTS.get(EnumMetals.SILVER), 3, 12, 20));
				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, bag_uncommon, 8, 12, 10));

				trades.get(4).add(new TradeListing(SELL_FOR_ONE_EMERALD, IETags.getTagsFor(EnumMetals.GOLD).dust, IEItems.Metals.DUSTS.get(EnumMetals.GOLD), 3, 12, 30));
				trades.get(4).add(new TradeListing(BUY_FOR_MANY_EMERALDS, bag_rare, 12, 8, 15));

				trades.get(5).add(new TradeListing(SELL_FOR_ONE_EMERALD, Tags.Items.GEMS_LAPIS, Items.LAPIS_LAZULI, 3, 12, 30));
				trades.get(5).add(new TradeListing(BUY_FOR_MANY_EMERALDS, bag_epic, 16, 3, 30).setMultiplier(0.2f));
			}
			else if(GUNSMITH.equals(typeName))
			{
				/* Gunsmith
				 * Sells ammunition, blueprints and revolver parts
				 */
				trades.get(1).add(new TradeListing(SELL_FOR_ONE_EMERALD, Items.GUNPOWDER, 4, 12, 2));
				trades.get(1).add(new TradeListing(BUY_FOR_MANY_EMERALDS, WoodenDevices.GUNPOWDER_BARREL, 3, 16, 1));
				trades.get(1).add(new TradeListing(BUY_FOR_MANY_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("bullet"), 4, 3, 5).setMultiplier(0.2f));

				trades.get(2).add(new TradeListing(SELL_FOR_MANY_EMERALDS, Blocks.TARGET, 2, 12, 10));
				trades.get(2).add(GroupedListing.of(
						new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Ingredients.EMPTY_CASING, 4), BulletHandler.getBulletStack(IEBullets.CASULL).copyWithCount(4), 12, 5, 0.05f),
						new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Ingredients.EMPTY_CASING, 4), BulletHandler.getBulletStack(IEBullets.SILVER).copyWithCount(4), 12, 5, 0.05f),
						new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Ingredients.EMPTY_SHELL, 4), BulletHandler.getBulletStack(IEBullets.BUCKSHOT).copyWithCount(4), 12, 5, 0.05f)
				));

				trades.get(3).add(new TradeListing(BUY_FOR_MANY_EMERALDS, BlueprintCraftingRecipe.getTypedBlueprint("specialBullet"), 12, 3, 20).setMultiplier(0.2f));
				trades.get(3).add(GroupedListing.of(
						new TradeListing(SELL_FOR_ONE_EMERALD, Ingredients.EMPTY_CASING, 8, 16, 20),
						new TradeListing(SELL_FOR_ONE_EMERALD, Ingredients.EMPTY_SHELL, 8, 16, 20),
						RevolverPieceForEmeralds.INSTANCE
				));

				trades.get(4).add(new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Ingredients.EMPTY_CASING, 2), BulletHandler.getBulletStack(IEBullets.HIGH_EXPLOSIVE).copyWithCount(2), 12, 15, 0.05f));
				trades.get(4).add(new BasicItemListing(new ItemStack(Items.EMERALD, 2), new ItemStack(Ingredients.EMPTY_SHELL, 2), BulletHandler.getBulletStack(IEBullets.FLARE).copyWithCount(2), 12, 15, 0.05f));
				trades.get(4).add(RevolverPieceForEmeralds.INSTANCE);

				trades.get(5).add(RevolverPieceForEmeralds.INSTANCE);
				trades.get(5).add(RevolverPieceForEmeralds.INSTANCE);
			}
		}


		@SubscribeEvent
		public static void onTrade(TradeWithVillagerEvent ev)
		{
			AbstractVillager villager = ev.getAbstractVillager();
			CompoundTag randomizedOffers = villager.getPersistentData().getCompoundOrEmpty(RerollingItemListing.RANDOMIZED_OFFERS_KEY);
			int offerIndex = villager.getOffers().indexOf(ev.getMerchantOffer());
			String offerIndexS = String.valueOf(offerIndex);
			if(randomizedOffers.contains(offerIndexS))
			{
				GenerateOffer offerFunction = RerollingItemListing.OFFER_FUNCTIONS.get(randomizedOffers.getStringOr(offerIndexS, ""));
				MerchantOffer offer = offerFunction.generateOffer(villager, ev.getEntity(), villager.getRandom());
				offer.increaseUses();
				villager.getOffers().set(offerIndex, offer);
			}
		}
	}

	private static class VillagerTradesEvent
	{
		private final Int2ObjectMap<List<ItemListing>> trades = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();

		private VillagerTradesEvent()
		{
			for(int i = 1; i <= 5; ++i)
				trades.put(i, new ArrayList<>());
		}

		private Int2ObjectMap<List<ItemListing>> getTrades()
		{
			return trades;
		}

		private Type getType()
		{
			return new Type("minecraft:none");
		}

		private record Type(String name)
		{
		}
	}

	/**
	 * Functional interface to create constant implementations from
	 */
	@FunctionalInterface
	private interface TradeOutline
	{
		MerchantOffer generateOffer(ItemStack item, int price, RandomSource random, int maxUses, int xp, float priceMultiplier);
	}

	private static final TradeOutline SELL_FOR_ONE_EMERALD = (buying, price, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(buying.getItem(), price),
			new ItemStack(Items.EMERALD),
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline SELL_FOR_MANY_EMERALDS = (buying, price, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(buying.getItem()),
			new ItemStack(Items.EMERALD, price),
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline BUY_FOR_ONE_EMERALD = (selling, price, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(Items.EMERALD),
			selling.copyWithCount(price),
			maxUses, xp, priceMultiplier
	);
	private static final TradeOutline BUY_FOR_MANY_EMERALDS = (selling, price, random, maxUses, xp, priceMultiplier) -> new MerchantOffer(
			new ItemCost(Items.EMERALD, price),
			selling,
			maxUses, xp, priceMultiplier
	);

	private static class BasicItemListing implements ItemListing
	{
		private final ItemStack priceA;
		private final ItemStack priceB;
		private final ItemStack result;
		private final int maxUses;
		private final int xp;
		private final float priceMultiplier;

		private BasicItemListing(ItemStack priceA, ItemStack priceB, ItemStack result, int maxUses, int xp, float priceMultiplier)
		{
			this.priceA = priceA;
			this.priceB = priceB;
			this.result = result;
			this.maxUses = maxUses;
			this.xp = xp;
			this.priceMultiplier = priceMultiplier;
		}

		@Override
		public MerchantOffer getOffer(Entity trader, RandomSource random)
		{
			return new MerchantOffer(
					new ItemCost(priceA.getItem(), priceA.getCount()),
					Optional.of(new ItemCost(priceB.getItem(), priceB.getCount())),
					result, 0, maxUses, xp, priceMultiplier
			);
		}
	}

	private static class TradeListing implements ItemListing
	{
		private final TradeOutline outline;
		private final LazyItemStack lazyItem;
		private final int price;
		private final int maxUses;
		private final int xp;
		private float priceMultiplier = 0.05f;

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull Function<Level, ItemStack> item, int price, int maxUses, int xp)
		{
			this.outline = outline;
			this.lazyItem = new LazyItemStack(item);
			this.price = price;
			this.maxUses = maxUses;
			this.xp = xp;
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemStack itemStack, int price, int maxUses, int xp)
		{
			this(outline, l -> itemStack, price, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull ItemLike item, int price, int maxUses, int xp)
		{
			this(outline, new ItemStack(item), price, maxUses, xp);
		}

		public TradeListing(@Nonnull TradeOutline outline, @Nonnull TagKey<Item> tag, @Nonnull ItemLike backup, int price, int maxUses, int xp)
		{
			this(outline, l -> {
				ItemStack fromTag = l!=null?IEApi.getPreferredTagStack(l.registryAccess(), tag): ItemStack.EMPTY;
				return fromTag.isEmpty()?new ItemStack(backup): fromTag;
			}, price, maxUses, xp);
		}

		public TradeListing setMultiplier(float priceMultiplier)
		{
			this.priceMultiplier = priceMultiplier;
			return this;
		}

		@Nullable
		public MerchantOffer getOffer(@Nullable Entity trader, @Nonnull RandomSource rand)
		{
			ItemStack buying = this.lazyItem.apply(trader!=null?trader.level(): null);
			return this.outline.generateOffer(buying, price, rand, maxUses, xp, priceMultiplier);
		}
	}

	public record RerollingItemListing(String functionKey) implements ItemListing
	{
		private static final String RANDOMIZED_OFFERS_KEY = "immersiveengineering:randomized_offers";
		private static final Map<String, GenerateOffer> OFFER_FUNCTIONS = new HashMap<>();

		public static RerollingItemListing register(String key, GenerateOffer function)
		{
			OFFER_FUNCTIONS.put(key, function);
			return new RerollingItemListing(key);
		}

		public MerchantOffer getOffer(Entity trader, @Nonnull RandomSource random)
		{
			Player player = null;
			if(trader instanceof AbstractVillager villager&&!villager.level().isClientSide())
			{
				player = villager.getTradingPlayer();

				// make note that this is a randomized trade
				CompoundTag traderData = trader.getPersistentData();
				CompoundTag randomizedOffers = traderData.getCompoundOrEmpty(RANDOMIZED_OFFERS_KEY);
				int offerIndex = villager.getOffers().size();
				randomizedOffers.putString(String.valueOf(offerIndex), this.functionKey);
				traderData.put(RANDOMIZED_OFFERS_KEY, randomizedOffers);
			}
			return OFFER_FUNCTIONS.get(functionKey).generateOffer(trader, player, random);
		}

		@FunctionalInterface
		public interface GenerateOffer
		{
			MerchantOffer generateOffer(Entity trader, @Nullable Player player, @Nonnull RandomSource random);
		}

	}

	private static class OreveinMapForEmeralds
	{
		private static final int SEARCH_RADIUS = 16*16;
		private static final String TRADER_SOLD_KEY = "immersiveengineering:mapped_veins";

		public static RerollingItemListing INSTANCE = RerollingItemListing.register("orevein_map", (trader, player, random) -> {
			if(trader==null)
				return null;
			Level world = trader.level();
			BlockPos merchantPos = trader.blockPosition();
			// extract list of already sold veins from the trader
			CompoundTag traderData = trader.getPersistentData();
			List<Long> soldMaps = new ArrayList<>();
			if(traderData.contains(TRADER_SOLD_KEY))
				for(long l : traderData.getLongArray(TRADER_SOLD_KEY).orElse(new long[0]))
					soldMaps.add(l);
			// get veins in 16 chunk radius, ordered by their rarity (lowest weight first)
			List<MineralVein> veins = ExcavatorHandler.findVeinsForVillager(world, merchantPos, SEARCH_RADIUS, soldMaps);
			if(!veins.isEmpty())
			{
				//select random vein from top 10
				int select = random.nextInt(Math.min(10, veins.size()));
				MineralVein vein = veins.get(select);
				ColumnPos veinPos = vein.getPos();
				// store sold map in trader data
				soldMaps.add(veinPos.toLong());
				traderData.putLongArray(TRADER_SOLD_KEY, soldMaps.stream().mapToLong(Long::longValue).toArray());
				// build map
				if(!(world instanceof ServerLevel serverWorld))
					return null;
				BlockPos blockPos = new BlockPos(veinPos.x(), 64, veinPos.z());
				ItemStack selling = MapItem.create(serverWorld, blockPos.getX(), blockPos.getZ(), (byte)1, true, true);
				MapItemSavedData.addTargetDecoration(selling, blockPos, "ie:coresample_treasure", MapDecorationTypes.RED_X);
				selling.set(
						DataComponents.ITEM_NAME,
						Component.translatable("item.immersiveengineering.map_orevein")
				);
				selling.set(
						DataComponents.LORE,
						new ItemLore(List.of(Component.translatable(vein.getMineral(world).getTranslationKey(vein.getMineralName()))))
				);
				// return offer
				return new MerchantOffer(
						new ItemCost(Items.EMERALD, 8+random.nextInt(8)),
						Optional.of(new ItemCost(Items.COMPASS)),
						selling, 0, 1, 30, 0.5F
				);
			}
			return null;
		});
	}

	private static class RevolverPieceForEmeralds
	{
		public static RerollingItemListing INSTANCE = RerollingItemListing.register("revolver_piece", (trader, player, random) -> {
			int part = random.nextInt(3);

			ItemStack stack = new ItemStack(part==0?Ingredients.GUNPART_BARREL: part==1?Ingredients.GUNPART_DRUM: Ingredients.GUNPART_HAMMER);

			float luck = player==null?0: player.getLuck();
			var perksTag = RevolverItem.RevolverPerk.generatePerkSet(random, luck);
			stack.set(IEDataComponents.REVOLVER_PERKS, perksTag);
			int tier = Math.max(1, RevolverItem.RevolverPerk.calculateTier(perksTag));

			return new MerchantOffer(new ItemCost(Items.EMERALD, 5*tier+random.nextInt(5)), stack, 1, 45, 0.25F);
		});
	}

	private record GroupedListing(ItemListing[] listings) implements ItemListing
	{
		static GroupedListing of(ItemListing... listings)
		{
			return new GroupedListing(listings);
		}

		public @Nullable MerchantOffer getOffer(Entity entity, RandomSource randomSource)
		{
			int idx = randomSource.nextInt(listings.length);
			return listings[idx].getOffer(entity, randomSource);
		}
	}

	/**
	 * Lazy-loaded ItemStack to support tag-based trades
	 */
	private static class LazyItemStack implements Function<Level, ItemStack>
	{
		private final Function<Level, ItemStack> function;
		private ItemStack instance;

		private LazyItemStack(Function<Level, ItemStack> function)
		{
			this.function = function;
		}

		public ItemStack apply(Level level)
		{
			if(instance==null)
				instance = function.apply(level);
			return instance;
		}
	}
}
