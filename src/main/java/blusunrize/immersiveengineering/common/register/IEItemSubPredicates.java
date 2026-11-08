/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.Lib;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IEItemSubPredicates
{
	public static final DeferredRegister<DataComponentPredicate.Type<?>> REGISTER = DeferredRegister.create(
			Registries.DATA_COMPONENT_PREDICATE_TYPE, Lib.MODID
	);

	public static final Supplier<DataComponentPredicate.Type<ItemBlueprintPredicate>> BLUEPRINT = REGISTER.register(
			"blueprint", () -> new DataComponentPredicate.ConcreteType<>(ItemBlueprintPredicate.CODEC)
	);

	public record ItemBlueprintPredicate(String blueprint) implements SingleComponentItemPredicate<String>
	{
		public static final Codec<ItemBlueprintPredicate> CODEC = RecordCodecBuilder.create((inst) -> inst.group(
				Codec.STRING.fieldOf("blueprint").forGetter(ItemBlueprintPredicate::blueprint)
		).apply(inst, ItemBlueprintPredicate::new));

		public DataComponentType<String> componentType()
		{
			return IEApiDataComponents.BLUEPRINT_TYPE.get();
		}

		public boolean matches(String s)
		{
			return this.blueprint.equals(s);
		}
	}
}
