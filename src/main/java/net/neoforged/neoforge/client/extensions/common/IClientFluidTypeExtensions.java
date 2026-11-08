package net.neoforged.neoforge.client.extensions.common;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector4f;

public interface IClientFluidTypeExtensions
{
	IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions()
	{
	};

	static IClientFluidTypeExtensions of(Fluid fluid)
	{
		return DEFAULT;
	}

	static IClientFluidTypeExtensions of(FluidState fluid)
	{
		return DEFAULT;
	}

	static IClientFluidTypeExtensions of(FluidType fluid)
	{
		return DEFAULT;
	}

	default Identifier getRenderOverlayTexture(Minecraft minecraft)
	{
		return null;
	}

	default void renderOverlay(Minecraft minecraft, PoseStack poseStack, MultiBufferSource buffers)
	{
	}

	default void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor)
	{
	}

	default void modifyFogRender(Camera camera, FogEnvironment fogEnvironment, float renderDistance, float partialTick, FogData fogData)
	{
	}
}
