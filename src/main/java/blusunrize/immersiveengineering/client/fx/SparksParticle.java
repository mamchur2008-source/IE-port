/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SparksParticle extends SingleQuadParticle
{
	public SparksParticle(ClientLevel world, double x, double y, double z, double mx, double my, double mz, TextureAtlasSprite sprite)
	{
		super(world, x, y, z, mx, my, mz, sprite);
		this.setLifetime(16);
		this.x = x;
		this.y = y;
		this.z = z;
		this.xd = mx;
		this.yd = my;
		this.zd = mz;
	}

	protected int getLightCoords(float p_70070_1_)
	{
		return 240<<16|240;
	}

	public void tick()
	{
		super.tick();
		int particleAge = age;
		this.setColor(1, .2f+(16-particleAge)/16f, particleAge > 4?0: (4-particleAge)/4f);
	}

	@Nonnull
	protected Layer getLayer()
	{
		return Layer.OPAQUE;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprite;

		public Factory(SpriteSet sprite)
		{
			this.sprite = sprite;
		}

		@Nullable
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
		{
			return new SparksParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite.get(random));
		}
	}
}
