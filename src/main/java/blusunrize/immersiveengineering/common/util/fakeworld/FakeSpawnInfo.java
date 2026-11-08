/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fakeworld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;

public class FakeSpawnInfo implements WritableLevelData
{
	private static final GameRules RULES = new GameRules(FeatureFlags.DEFAULT_FLAGS);

	private BlockPos spawnPos = BlockPos.ZERO;
	private float spawnAngle;

	public void setSpawn(BlockPos pos, float angle)
	{
		spawnAngle = angle;
		spawnPos = pos;
	}

	public void setSpawn(LevelData.RespawnData respawnData)
	{
		spawnAngle = respawnData.yaw();
		spawnPos = respawnData.pos();
	}

	public BlockPos getSpawnPos()
	{
		return spawnPos;
	}

	public float getSpawnAngle()
	{
		return spawnAngle;
	}

	public LevelData.RespawnData getRespawnData()
	{
		return LevelData.RespawnData.of(net.minecraft.world.level.Level.OVERWORLD, spawnPos, spawnAngle, 0);
	}

	public long getGameTime()
	{
		return 0;
	}

	public long getDayTime()
	{
		return 0;
	}

	public boolean isThundering()
	{
		return false;
	}

	public boolean isRaining()
	{
		return false;
	}

	public void setRaining(boolean isRaining)
	{

	}

	public boolean isHardcore()
	{
		return false;
	}

	public GameRules getGameRules()
	{
		return RULES;
	}

	public Difficulty getDifficulty()
	{
		return Difficulty.PEACEFUL;
	}

	public boolean isDifficultyLocked()
	{
		return false;
	}
}
