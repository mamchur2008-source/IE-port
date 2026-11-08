package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Consumer;

public class RegisterShadersEvent
{
	public Object getResourceProvider()
	{
		return null;
	}

	public void registerShader(ShaderInstance shader, Consumer<ShaderInstance> onLoaded)
	{
		onLoaded.accept(shader);
	}
}
