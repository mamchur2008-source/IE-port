/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.Objects;

public class ManualElementEntity extends SpecialManualElements
{
	private final Lazy<RenderData> renderData;

	public ManualElementEntity(ManualInstance helper, EntityType<?> entityType, @Nullable CompoundTag entityData)
	{
		super(helper);
		this.renderData = Lazy.of(() -> new RenderData(entityType, entityData));
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, ManualScreen gui, int x, int y, int mx, int my)
	{
		Entity entity = renderData.get().entity;
		if(!(entity instanceof LivingEntity living))
			return;
		float yOff = renderData.get().ySize-4;
		float scale = renderData.get().scale;

		float pitch = (yOff/2)-my;
		float yaw = 60-mx;
		InventoryScreen.renderEntityInInventoryFollowsAngle(
				graphics, x, y, x+120, y+renderData.get().ySize, (int)scale, 0, yaw/40f, pitch/40f, living
		);
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return renderData.get().ySize;
	}

	private static class RenderData
	{
		final Entity entity;
		final float entitySize;
		final float scale;
		final int ySize;

		RenderData(EntityType<?> entityType, CompoundTag entityData)
		{
			this.entity = Objects.requireNonNull(entityType.create(Minecraft.getInstance().level, EntitySpawnReason.TRIGGERED));
			if(entityData!=null)
				this.entity.load(TagValueInput.create(ProblemReporter.DISCARDING, this.entity.registryAccess(), entityData));
			this.entitySize = Math.max(entity.getBbWidth(), entity.getBbHeight());
			this.scale = entitySize <= 1?36: entitySize <= 3?28: 26f-entitySize;
			this.ySize = entitySize <= 2?60: 90;
		}
	}

}
