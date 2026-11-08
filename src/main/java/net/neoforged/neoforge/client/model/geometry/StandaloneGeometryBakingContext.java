package net.neoforged.neoforge.client.model.geometry;

import net.minecraft.resources.Identifier;

public class StandaloneGeometryBakingContext implements IGeometryBakingContext
{
	private final IGeometryBakingContext parent;
	private final Identifier name;

	private StandaloneGeometryBakingContext(IGeometryBakingContext parent, Identifier name)
	{
		this.parent = parent;
		this.name = name;
	}

	public static Builder builder(IGeometryBakingContext parent)
	{
		return new Builder(parent);
	}

	public Identifier getModelName()
	{
		return name;
	}

	public static class Builder
	{
		private final IGeometryBakingContext parent;

		private Builder(IGeometryBakingContext parent)
		{
			this.parent = parent;
		}

		public Builder withGui3d(boolean gui3d)
		{
			return this;
		}

		public Builder withUseBlockLight(boolean useBlockLight)
		{
			return this;
		}

		public StandaloneGeometryBakingContext build(Identifier name)
		{
			return new StandaloneGeometryBakingContext(parent, name);
		}
	}
}
