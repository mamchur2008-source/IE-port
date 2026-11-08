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

//A version of the vanilla bubble particle that can exist outside of water blocks
public class IEBubbleParticle extends SingleQuadParticle
{
	public IEBubbleParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, TextureAtlasSprite sprite)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, sprite);
		this.setSize(0.02F, 0.02F);
		this.quadSize *= this.random.nextFloat()*0.6F+0.2F;
		this.xd = xSpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.yd = ySpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.zd = zSpeedIn*(double)0.2F+(Math.random()*2.0D-1.0D)*(double)0.02F;
		this.lifetime = (int)(8.0D/(Math.random()*0.8D+0.2D));
	}

	public void tick()
	{
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if(this.lifetime-- <= 0)
			this.remove();
		else
		{
			this.yd += 0.002D;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.85F;
			this.yd *= 0.85F;
			this.zd *= 0.85F;
		}
	}

	@Nonnull
	protected Layer getLayer()
	{
		return Layer.OPAQUE;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet texture;

		public Factory(SpriteSet spriteSet)
		{
			this.texture = spriteSet;
		}

		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random)
		{
			return new IEBubbleParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.texture.get(random));
		}
	}
}
