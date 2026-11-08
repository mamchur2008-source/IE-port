/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.blastfurnace;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.jei.DoubleIcon;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

public class BlastFurnaceFuelCategory extends IERecipeCategory<BlastFurnaceFuel>
{
	private final IDrawableAnimated flame;

	public BlastFurnaceFuelCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.BLAST_FUEL, "gui.immersiveengineering.blastFurnace.fuel");
		Identifier furnaceBackgroundLocation = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
		setBackground(helper.drawableBuilder(furnaceBackgroundLocation, 55, 36, 18, 36).addPadding(0, 0, 0, 68).build());
		setIcon(new DoubleIcon(
				helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, IEMultiblockLogic.BLAST_FURNACE.iconStack()),
				helper.createDrawable(IEApi.ieLoc("textures/gui/blast_furnace.png"), 176, 0, 14, 14),
				0.75f
		));
		IDrawableStatic flameStatic = helper.createDrawable(furnaceBackgroundLocation, 176, 0, 14, 14);
		this.flame = helper.createAnimatedDrawable(flameStatic, 20*4, IDrawableAnimated.StartDirection.TOP, true);
	}

	public void setRecipe(IRecipeLayoutBuilder builder, BlastFurnaceFuel recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 17)
				.addItemStacks(java.util.List.of());
	}

	public void draw(BlastFurnaceFuel recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY)
	{
		this.flame.draw(graphics, 1, 0);
		String burnTime = I18n.get("desc.immersiveengineering.info.seconds", Utils.formatDouble(recipe.burnTime/20, "#.##"));
		graphics.text(ClientUtils.font(), burnTime, 24, 12, 0x777777, false);
	}
}
