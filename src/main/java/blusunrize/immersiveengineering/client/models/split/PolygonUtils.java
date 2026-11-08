/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.client.utils.BakedQuadBuilder;
import com.google.common.base.Preconditions;
import net.minecraft.client.model.geom.builders.UVPair;
import com.mojang.math.Transformation;
import malte0811.modelsplitter.math.Vec3d;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.UVCoords;
import malte0811.modelsplitter.model.Vertex;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class PolygonUtils
{
	public static Polygon<ExtraQuadData> toPolygon(BakedQuad quad)
	{
		List<Vertex> vertices = new ArrayList<>(4);
		Vector3fc normal = quad.direction().getUnitVec3f();
		for(int v = 0; v < 4; ++v)
		{
			final Vec3d normalVec = new Vec3d(
					normal.x(),
					normal.y(),
					normal.z()
			).normalize();
			long packedUv = quad.packedUV(v);
			final UVCoords uv = new UVCoords(
					UVPair.unpackU(packedUv),
					UVPair.unpackV(packedUv)
			);
			Vector3fc position = quad.position(v);
			final Vec3d pos = new Vec3d(
					position.x(),
					position.y(),
					position.z()
			);
			vertices.add(new Vertex(pos, normalVec, uv));
		}
		return new Polygon<>(vertices, new ExtraQuadData(
				quad.materialInfo(),
				Color4.WHITE)
		);
	}

	public static BakedQuad toBakedQuad(Polygon<ExtraQuadData> poly, ModelState transform)
	{
		return toBakedQuad(poly.getPoints(), poly.getTexture(), transform.transformation().blockCenterToCorner(), true, true);
	}

	public static BakedQuad toBakedQuad(List<Vertex> points, ExtraQuadData data, Transformation rotation, boolean absoluteUV, boolean shade)
	{
		Preconditions.checkArgument(points.size()==4);
		BakedQuadBuilder quadBuilder = new BakedQuadBuilder();
		Vector3f normal = new Vector3f();
		for(Vertex v : points)
		{
			Vector4f pos = new Vector4f();
			pos.set(toArray(v.position(), 4));
			normal.set(toArray(v.normal(), 3));
			rotation.transformPosition(pos);
			rotation.transformNormal(normal);
			pos.mul(1 / pos.w);
			final double epsilon = 1e-5;
			for(int i = 0; i < 2; ++i)
			{
				if(Math.abs(i-pos.x()) < epsilon) pos.setComponent(0, i);
				if(Math.abs(i-pos.y()) < epsilon) pos.setComponent(1, i);
				if(Math.abs(i-pos.z()) < epsilon) pos.setComponent(2, i);
			}
			quadBuilder.putVertexData(
					new Vec3(pos.x(), pos.y(), pos.z()),
					new Vec3(normal),
					absoluteUV?v.uv().u(): data.materialInfo().sprite().getU((float)v.uv().u()),
					absoluteUV?v.uv().v(): data.materialInfo().sprite().getV((float)(1-v.uv().v())),
					new float[]{data.color.r(), data.color.g(), data.color.b(), data.color.a()},
					1
			);
		}
		return quadBuilder.bake(
				Direction.getNearest(
						(int)Math.signum(normal.x()), (int)Math.signum(normal.y()), (int)Math.signum(normal.z()),
						Direction.NORTH
				),
				data.materialInfo()
		);
	}

	private static float[] toArray(Vec3d vec, int length)
	{
		float[] ret = new float[length];
		for(int i = 0; i < 3; ++i)
			ret[i] = (float)vec.get(i);
		for(int i = 3; i < length; ++i)
			ret[i] = 1;
		return ret;
	}

	public record ExtraQuadData(BakedQuad.MaterialInfo materialInfo, Color4 color)
	{
		public ExtraQuadData(TextureAtlasSprite sprite, Color4 color, boolean shade)
		{
			this(
					BakedQuad.MaterialInfo.of(new Material.Baked(sprite, false), sprite.transparency(), -1, shade, 0),
					color
			);
		}

		public ExtraQuadData(TextureAtlasSprite sprite, Color4 color)
		{
			this(sprite, color, true);
		}
	}
}
