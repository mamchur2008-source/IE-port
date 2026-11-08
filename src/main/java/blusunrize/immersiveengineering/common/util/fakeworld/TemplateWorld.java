/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class TemplateWorld extends Level
{
	private static final DimensionType DIMENSION_TYPE = new DimensionType(
			false, true, false, false, 1, 0, 256, 256,
			BlockTags.INFINIBURN_OVERWORLD, 0,
			new DimensionType.MonsterSettings(ConstantInt.ZERO, 0),
			DimensionType.Skybox.OVERWORLD, CardinalLighting.Type.DEFAULT,
			EnvironmentAttributeMap.EMPTY, HolderSet.empty(), Optional.empty()
	);
	private static final Identifier DIMENSION_TYPE_ID = ImmersiveEngineering.rl("multiblock_preview");
	private static final ClockManager CLOCK_MANAGER = clock -> 0;
	private static final EnvironmentAttributeSystem ENVIRONMENT_ATTRIBUTES = EnvironmentAttributeSystem.builder().build();
	private static final WorldBorder WORLD_BORDER = new WorldBorder();

	private static final Holder<DimensionType> STRUCTURE_DIMENSION = new FakeRegisteredHolder<>(
			DIMENSION_TYPE, ResourceKey.create(Registries.DIMENSION_TYPE, DIMENSION_TYPE_ID)
	);

	private final Map<MapId, MapItemSavedData> maps = new HashMap<>();
	private final Scoreboard scoreboard = new Scoreboard();
	private final RecipeManager recipeManager;
	private final TemplateChunkProvider chunkProvider;
	private final TickRateManager tickRateManager = new TickRateManager();

	public static LevelReader createSingleBlock(BlockState stateAtZero, RegistryAccess regAccess)
	{
		return new TemplateWorld(
				List.of(new StructureBlockInfo(BlockPos.ZERO, stateAtZero, null)), bp -> true, regAccess
		);
	}

	public TemplateWorld(List<StructureBlockInfo> blocks, Predicate<BlockPos> shouldShow, RegistryAccess regAccess)
	{
		super(
				new FakeSpawnInfo(), Level.OVERWORLD, regAccess, STRUCTURE_DIMENSION,
				true, false, 0, 0
		);
		this.chunkProvider = new TemplateChunkProvider(blocks, this, shouldShow);
		this.recipeManager = new RecipeManager(regAccess);
	}

	public void sendBlockUpdated(@Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags)
	{
	}

	public void playSeededSound(@Nullable Player p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_, long p_262991_)
	{
	}

	public void playSeededSound(@Nullable Entity p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_, long p_262991_)
	{
	}

	public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_)
	{
	}

	public void playSeededSound(@Nullable Entity p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_)
	{
	}

	public void explode(
			@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionCalculator,
			double x, double y, double z, float radius, boolean fire, ExplosionInteraction explosionInteraction,
			ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles,
			WeightedList<ExplosionParticleInfo> explosionParticles, Holder<SoundEvent> explosionSound
	)
	{
	}

	@Nonnull
	public String gatherChunkSourceStats()
	{
		return "";
	}

	@Nullable
	public Entity getEntity(int id)
	{
		return null;
	}

	public Collection<? extends PartEntity<?>> dragonParts()
	{
		return List.of();
	}

	public TickRateManager tickRateManager()
	{
		return tickRateManager;
	}

	@Nullable
	public MapItemSavedData getMapData(MapId p_324234_)
	{
		return maps.get(p_324234_);
	}

	public void setMapData(MapId p_324009_, MapItemSavedData p_151534_)
	{
		maps.put(p_324009_, p_151534_);
	}

	public MapId getFreeMapId()
	{
		int i = 0;
		while(maps.containsKey(new MapId(i)))
			++i;
		return new MapId(i);
	}

	public void setRespawnData(LevelData.RespawnData respawnData)
	{
	}

	public LevelData.RespawnData getRespawnData()
	{
		return LevelData.RespawnData.DEFAULT;
	}

	public void destroyBlockProgress(int breakerId, @Nonnull BlockPos pos, int progress)
	{
	}

	@Nonnull
	public Scoreboard getScoreboard()
	{
		return scoreboard;
	}

	@Nonnull
	public RecipeManager getRecipeManager()
	{
		return recipeManager;
	}

	public RecipeAccess recipeAccess()
	{
		return recipeManager;
	}

	@Nonnull
	protected LevelEntityGetter<Entity> getEntities()
	{
		return new EmptyLevelEntityGetter<>();
	}

	@Nonnull
	public LevelTickAccess<Block> getBlockTicks()
	{
		return new EmptyTickAccess<>();
	}

	@Nonnull
	public LevelTickAccess<Fluid> getFluidTicks()
	{
		return new EmptyTickAccess<>();
	}

	@Nonnull
	public ChunkSource getChunkSource()
	{
		return chunkProvider;
	}

	public void levelEvent(@Nullable Player player, int type, @Nonnull BlockPos pos, int data)
	{
	}

	public void levelEvent(@Nullable Entity entity, int type, @Nonnull BlockPos pos, int data)
	{
	}

	public void gameEvent(Holder<GameEvent> p_316267_, Vec3 p_220405_, Context p_220406_)
	{

	}

	@Nonnull
	public RegistryAccess registryAccess()
	{
		Level clientWorld = ImmersiveEngineering.proxy.getClientWorld();
		return Objects.requireNonNull(clientWorld).registryAccess();
	}

	public PotionBrewing potionBrewing()
	{
		return null;
	}

	public FuelValues fuelValues()
	{
		return FuelValues.vanillaBurnTimes(registryAccess(), enabledFeatures());
	}

	public ClockManager clockManager()
	{
		return CLOCK_MANAGER;
	}

	public EnvironmentAttributeSystem environmentAttributes()
	{
		return ENVIRONMENT_ATTRIBUTES;
	}

	public void setDayTimeFraction(float dayTimeFraction)
	{
	}

	public float getDayTimeFraction()
	{
		return 1;
	}

	public float getDayTimePerTick()
	{
		return 1;
	}

	public void setDayTimePerTick(float dayTimePerTick)
	{
	}

	public FeatureFlagSet enabledFeatures()
	{
		return ImmersiveEngineering.proxy.getClientWorld().enabledFeatures();
	}

	public float getShade(@Nonnull Direction p_230487_1_, boolean p_230487_2_)
	{
		return 1;
	}

	@Nonnull
	public List<? extends Player> players()
	{
		return ImmutableList.of();
	}

	@Nonnull
	public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z)
	{
		return registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
	}

	public int getBrightness(@Nonnull LightLayer lightType, @Nonnull BlockPos pos)
	{
		return 15;
	}

	public int getSeaLevel()
	{
		return 63;
	}

	public WorldBorder getWorldBorder()
	{
		return WORLD_BORDER;
	}
}
