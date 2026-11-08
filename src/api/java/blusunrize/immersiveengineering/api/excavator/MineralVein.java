/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import net.minecraft.IdentifierException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class MineralVein
{
	private final ColumnPos pos;
	private final Identifier mineralName;
	@Nullable
	private MineralMix mineral;
	private final int radius;
	private int depletion;

	public MineralVein(ColumnPos pos, Identifier mineral, int radius)
	{
		this.pos = pos;
		this.mineralName = mineral;
		this.radius = radius;
	}

	public ColumnPos getPos()
	{
		return pos;
	}

	@Nullable
	public RecipeHolder<MineralMix> getMineralHolder(Level level)
	{
		MineralMix mineral = getMineral(level);
		return mineral!=null?new RecipeHolder<>(recipeKey(getMineralName()), mineral): null;
	}

	@Nullable
	public MineralMix getMineral(Level level)
	{
		if (mineral == null)
			mineral = MineralMix.RECIPES.getById(level, mineralName);
		return mineral;
	}

	public int getRadius()
	{
		return radius;
	}

	// fail chance grows with distance from the center of the vein
	public double getFailChance(BlockPos pos)
	{
		double dX = pos.getX()-this.pos.x();
		double dZ = pos.getZ()-this.pos.z();
		double d = (dX*dX+dZ*dZ)/(radius*radius);
		return d*d*(-2*d+3);
	}

	public int getDepletion()
	{
		return depletion;
	}

	public void setDepletion(int depletion)
	{
		this.depletion = depletion;
	}

	public void deplete()
	{
		if(!isDepleted())
		{
			depletion++;
			ExcavatorHandler.MARK_SAVE_DATA_DIRTY.get().run();
		}
	}

	public boolean isDepleted()
	{
		return ExcavatorHandler.mineralVeinYield > 0&&getDepletion() >= ExcavatorHandler.mineralVeinYield;
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("x", pos.x());
		tag.putInt("z", pos.z());
		tag.putString("mineral", mineralName.toString());
		tag.putInt("radius", radius);
		tag.putInt("depletion", depletion);
		return tag;
	}

	@Nullable
	public static MineralVein readFromNBT(CompoundTag tag)
	{
		try
		{
			ColumnPos pos = new ColumnPos(tag.getIntOr("x", 0), tag.getIntOr("z", 0));
			Identifier id = Identifier.parse(tag.getStringOr("mineral", ""));
			int radius = tag.getIntOr("radius", 0);
			MineralVein info = new MineralVein(pos, id, radius);
			info.depletion = tag.getIntOr("depletion", 0);
			return info;
		} catch(IdentifierException ex)
		{
			return null;
		}
	}

	public Identifier getMineralName()
	{
		return mineralName;
	}

	private static ResourceKey<Recipe<?>> recipeKey(Identifier id)
	{
		return ResourceKey.create(Registries.RECIPE, id);
	}
}
