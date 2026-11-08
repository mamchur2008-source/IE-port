/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.ClocheCallbacks.Key;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.SLOT_SOIL;

public class ClocheCallbacks implements BlockCallback<Key>
{
	public static final ClocheCallbacks INSTANCE = new ClocheCallbacks();
	private static final Key INVALID = new Key(new ComparableItemStack(ItemStack.EMPTY));

	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ClocheBlockEntity clocheHere))
			return getDefaultKey();
		ClocheBlockEntity mainCloche = clocheHere.master();
		if(mainCloche==null)
			return getDefaultKey();
		ItemStack soil = mainCloche.getInventory().get(SLOT_SOIL);
		return new Key(new ComparableItemStack(soil, true));
	}

	public Key getDefaultKey()
	{
		return INVALID;
	}

	public boolean dependsOnLayer()
	{
		return true;
	}

	@Nullable
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		ItemStack soil = key.soil().stack;
		if(!soil.isEmpty()&&"farmland".equals(material))
		{
			Identifier rl = getSoilTexture(soil);
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	public boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		return "glass".equals(group)==(layer==blusunrize.immersiveengineering.client.utils.RenderTypeCompat.translucent());
	}

	@Nullable
	private static Identifier getSoilTexture(ItemStack soil)
	{
		Identifier rl = ClocheRecipe.getSoilTexture(soil);
		if(rl==null)
		{
			try
			{
				BlockState state = Utils.getStateFromItemStack(soil);
				if(state!=null)
					rl = ModelUtils.getSideTexture(state, Direction.UP);
			} catch(Exception e)
			{
				rl = ModelUtils.getSideTexture(soil, Direction.UP);
			}
		}
		if(rl==null&&!soil.isEmpty()&&Utils.isFluidRelatedItemStack(soil))
			rl = FluidUtil.getFluidContained(soil)
					.map(GuiHelper::getFluidStillTexture)
					.orElse(null);
		return rl;
	}

	public Color4 getRenderColor(Key key, String group, String material, ShaderCase shaderCase, Color4 original)
	{
		ItemStack soil = key.soil().stack;
		if(!soil.isEmpty()&&"farmland".equals(material)&&Utils.isFluidRelatedItemStack(soil))
			return Color4.fromARGB(
					FluidUtil.getFluidContained(soil)
							.map(GuiHelper::getFluidColor)
							.orElse(0xffffffff)
			);
		return original;
	}

	public record Key(ComparableItemStack soil)
	{
	}
}
