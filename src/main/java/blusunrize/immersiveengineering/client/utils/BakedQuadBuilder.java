/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class BakedQuadBuilder
{
	private int nextVertex = 0;
	private final Vector3f[] positions = new Vector3f[4];
	private final long[] uvs = new long[4];

	public void putVertexData(
			Vec3 pos, Vec3 faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha
	)
	{
		// TODO pass un-16-nification further up in the logic
		putVertexData(pos, faceNormal, sprite.getU((float)(u/16)), sprite.getV((float)(v/16)), colour, alpha);
	}

	public void putVertexData(Vec3 pos, Vec3 faceNormal, double u, double v, float[] colour, float alpha)
	{
		positions[nextVertex] = new Vector3f((float)pos.x, (float)pos.y, (float)pos.z);
		uvs[nextVertex] = UVPair.pack((float)u, (float)v);
		++nextVertex;
	}

	public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade)
	{
		BakedQuad.MaterialInfo material = BakedQuad.MaterialInfo.of(
				new Material.Baked(texture, false), texture.transparency(), tint, shade, 0
		);
		return bake(side, material);
	}

	public BakedQuad bake(Direction side, BakedQuad.MaterialInfo material)
	{
		return new BakedQuad(
				positions[0], positions[1], positions[2], positions[3],
				uvs[0], uvs[1], uvs[2], uvs[3],
				side, material
		);
	}
}
