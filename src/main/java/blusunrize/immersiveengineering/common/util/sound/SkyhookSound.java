/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.sound;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SkyhookSound implements TickableSoundInstance
{
	private final SkylineHookEntity hook;
	private final Identifier soundLoc;
	private Sound sound;
	private float speed = .01F;


	public SkyhookSound(SkylineHookEntity hook, Identifier soundLoc)
	{
		this.hook = hook;
		this.soundLoc = soundLoc;
	}

	public boolean isStopped()
	{
		return !hook.isAlive();
	}

	@Nonnull
	public Identifier getIdentifier()
	{
		return soundLoc;
	}

	@Nullable
	public WeighedSoundEvents resolve(@Nonnull SoundManager handler)
	{
		WeighedSoundEvents soundEvent = handler.getSoundEvent(this.soundLoc);
		if(soundEvent==null)
			this.sound = SoundManager.EMPTY_SOUND;
		else
			this.sound = soundEvent.getSound(ApiUtils.RANDOM_SOURCE);
		return soundEvent;
	}

	@Nonnull
	public Sound getSound()
	{
		return sound;
	}

	@Nonnull
	public SoundSource getSource()
	{
		return SoundSource.NEUTRAL;
	}

	public boolean isLooping()
	{
		return true;
	}

	public int getDelay()
	{
		return 0;
	}

	public float getVolume()
	{
		return Math.min(speed, .75F);
	}

	public float getPitch()
	{
		return Math.min(.5F*speed, .75F);
	}

	public double getX()
	{
		return (float)hook.getX();
	}

	public double getY()
	{
		return (float)hook.getY();
	}

	public double getZ()
	{
		return (float)hook.getZ();
	}

	@Nonnull
	public Attenuation getAttenuation()
	{
		return Attenuation.LINEAR;
	}

	public void tick()
	{
		speed = (float)hook.getSpeed();
		if(speed < .01)
			speed = .01F;
	}

	public boolean isRelative()
	{
		return false;
	}
}
