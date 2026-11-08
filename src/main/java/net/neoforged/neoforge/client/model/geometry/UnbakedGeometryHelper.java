package net.neoforged.neoforge.client.model.geometry;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public class UnbakedGeometryHelper
{
	public static Material resolveDirtyMaterial(String name, IGeometryBakingContext owner)
	{
		Material material = owner.getMaterial(name);
		if(material!=null)
			return material;
		if(name!=null&&name.indexOf(':') >= 0)
			return new Material(Identifier.parse(name));
		return owner.getMaterial("particle");
	}
}
