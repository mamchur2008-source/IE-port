package net.neoforged.neoforge.client.model.geometry;

import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.RenderTypeGroup;

public interface IGeometryBakingContext
{
	default ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	default Material getMaterial(String name)
	{
		return null;
	}

	default RenderTypeGroup getRenderType(String name)
	{
		return RenderTypeGroup.EMPTY;
	}

	default RenderTypeGroup getRenderType(Identifier name)
	{
		return RenderTypeGroup.EMPTY;
	}

	default Identifier getRenderTypeHint()
	{
		return null;
	}

	default Identifier getModelName()
	{
		return Identifier.parse("immersiveengineering:legacy_model");
	}
}
