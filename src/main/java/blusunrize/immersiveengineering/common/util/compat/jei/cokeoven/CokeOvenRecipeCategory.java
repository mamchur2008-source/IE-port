/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.cokeoven;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIHelper;
import blusunrize.immersiveengineering.common.util.compat.jei.JEIRecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import java.util.List;

public class CokeOvenRecipeCategory extends IERecipeCategory<CokeOvenRecipe>
{
	private final IDrawableStatic tankOverlay;
	private final IDrawableAnimated flame;
	private final static int TANK_SIZE = 12*FluidType.BUCKET_VOLUME;

	public CokeOvenRecipeCategory(IGuiHelper helper)
	{
		super(helper, JEIRecipeTypes.COKE_OVEN, "block.immersiveengineering.coke_oven");
		Identifier background = IEApi.ieLoc("textures/gui/coke_oven.png");
		setBackground(helper.createDrawable(background, 26, 16, 123, 55));
		setIcon(IEMultiblockLogic.COKE_OVEN.iconStack());
		tankOverlay = helper.createDrawable(background, 178, 33, 16, 47);
		flame = helper.drawableBuilder(background, 177, 0, 14, 14).buildAnimated(500, IDrawableAnimated.StartDirection.TOP, true);
	}

	public void setRecipe(IRecipeLayoutBuilder builder, CokeOvenRecipe recipe, IFocusGroup focuses)
	{
		final int batchSize = recipe.input.getCount();
		builder.addSlot(RecipeIngredientRole.INPUT, 4, 19)
				.addItemStacks(recipe.input.getMatchingStackList())
				.addRichTooltipCallback((slot, tooltip) -> tooltip.add(
						Component.translatable(Lib.DESC_INFO+"batched", batchSize).withStyle(ChatFormatting.GOLD))
				);

		IRecipeSlotBuilder outputSlotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, 59, 19);
		if(!recipe.output.get().isEmpty())
			outputSlotBuilder.addItemStack(recipe.output.get());

		if(recipe.creosoteOutput > 0)
		{
			builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 4)
					.setFluidRenderer(TANK_SIZE, false, 16, 47)
					.setOverlay(tankOverlay, 0, 0)
					.addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput))
					.addRichTooltipCallback(JEIHelper.fluidTooltipCallback);
		}
	}

	public void onDisplayedIngredientsUpdate(@Nonnull RecipeHolder<CokeOvenRecipe> recipe, @Nonnull List<IRecipeSlotDrawable> recipeSlots, @Nonnull IFocusGroup focuses)
	{
		// timing shenanigans, this is the same formula that JEI uses internally for cycling
		long now = System.currentTimeMillis();
		long index = now/1000L%100000L;

		// calculate qty to display
		int batchSize = recipe.value().input.getCount();
		int qty = 1+(Math.toIntExact(index)%batchSize);

		// adjust input quantities
		IRecipeSlotDrawable inputSlot = recipeSlots.getFirst();
		inputSlot.createDisplayOverrides().addItemStacks(inputSlot.getItemStacks().map(stack -> stack.copyWithCount(qty)).toList());
		// adjust output quantities
		IRecipeSlotDrawable outputSlot = recipeSlots.get(1);
		outputSlot.createDisplayOverrides().addItemStacks(outputSlot.getItemStacks().map(stack -> stack.copyWithCount(stack.getCount()*qty)).toList());
		// adjust fluid display
		if(recipeSlots.size() > 2)
		{
			IRecipeSlotDrawable tank = recipeSlots.get(2);
			tank.createDisplayOverrides().addFluidStack(IEFluids.CREOSOTE.getStill(), (long)recipe.value().creosoteOutput*qty);
		}
	}

	public void draw(CokeOvenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY)
	{
		flame.draw(graphics, 31, 20);
	}
}
