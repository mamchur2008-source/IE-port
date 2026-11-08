/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.utils.BakedQuadBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public record PortedCoresampleModel(
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();

	@Override
	public TextureSlots.Data textureSlots()
	{
		return textureSlots;
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		Material.Baked material = modelBaker.materials().resolveSlot(textureSlots, "particle", name);
		TextureAtlasSprite stone = material.sprite();
		Transformation transform = modelState.transformation().blockCenterToCorner();
		QuadCollection.Builder result = new QuadCollection.Builder();

		float width = .25f;
		float depth = .25f;
		float wOff = (1-width)/2;
		float dOff = (1-depth)/2;
		double[] stoneUVs = {
				16*wOff, 16*dOff,
				16*(wOff+width), 16*(dOff+depth),
		};

		putFace(result, transform, new Vec3(0, -1, 0), new Vec3[]{
				new Vec3(wOff, 0, dOff),
				new Vec3(wOff+width, 0, dOff),
				new Vec3(wOff+width, 0, dOff+depth),
				new Vec3(wOff, 0, dOff+depth)
		}, stoneUVs, stone);
		putFace(result, transform, new Vec3(0, 1, 0), new Vec3[]{
				new Vec3(wOff, 1, dOff),
				new Vec3(wOff, 1, dOff+depth),
				new Vec3(wOff+width, 1, dOff+depth),
				new Vec3(wOff+width, 1, dOff)
		}, stoneUVs, stone);

		double[][] uvs = new double[4][];
		for(int i = 0; i < 4; i++)
			uvs[i] = new double[]{i*4, 0, (i+1)*4, 16};

		putFace(result, transform, new Vec3(0, 0, -1), new Vec3[]{
				new Vec3(wOff, 0, dOff),
				new Vec3(wOff, 1, dOff),
				new Vec3(wOff+width, 1, dOff),
				new Vec3(wOff+width, 0, dOff)
		}, uvs[0], stone);
		putFace(result, transform, new Vec3(0, 0, 1), new Vec3[]{
				new Vec3(wOff+width, 0, dOff+depth),
				new Vec3(wOff+width, 1, dOff+depth),
				new Vec3(wOff, 1, dOff+depth),
				new Vec3(wOff, 0, dOff+depth)
		}, uvs[2], stone);
		putFace(result, transform, new Vec3(-1, 0, 0), new Vec3[]{
				new Vec3(wOff, 0, dOff+depth),
				new Vec3(wOff, 1, dOff+depth),
				new Vec3(wOff, 1, dOff),
				new Vec3(wOff, 0, dOff)
		}, uvs[3], stone);
		putFace(result, transform, new Vec3(1, 0, 0), new Vec3[]{
				new Vec3(wOff+width, 0, dOff),
				new Vec3(wOff+width, 1, dOff),
				new Vec3(wOff+width, 1, dOff+depth),
				new Vec3(wOff+width, 0, dOff+depth)
		}, uvs[1], stone);

		return result.build();
	}

	private static void putFace(
			QuadCollection.Builder result, Transformation transform, Vec3 faceNormal, Vec3[] vertices,
			double[] uvs, TextureAtlasSprite sprite
	)
	{
		BakedQuadBuilder quadBuilder = new BakedQuadBuilder();
		Vec3 transformedNormal = transformNormal(transform, faceNormal);
		for(int i = 0; i < 4; i++)
			quadBuilder.putVertexData(
					transformPosition(transform, vertices[i]), transformedNormal,
					uvs[i < 2?0: 2], uvs[i==0||i==3?1: 3],
					sprite, new float[]{1, 1, 1, 1}, 1
			);
		Direction direction = Direction.getNearest(
				(int)Math.signum(transformedNormal.x),
				(int)Math.signum(transformedNormal.y),
				(int)Math.signum(transformedNormal.z),
				Direction.NORTH
		);
		BakedQuad quad = quadBuilder.bake(-1, direction, sprite, true);
		result.addUnculledFace(quad);
	}

	private static Vec3 transformPosition(Transformation transform, Vec3 position)
	{
		Vector4f transformed = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
		transform.transformPosition(transformed);
		transformed.mul(1/transformed.w());
		return new Vec3(
				snap(transformed.x()),
				snap(transformed.y()),
				snap(transformed.z())
		);
	}

	private static Vec3 transformNormal(Transformation transform, Vec3 normal)
	{
		Vector3f transformed = new Vector3f((float)normal.x, (float)normal.y, (float)normal.z);
		transform.transformNormal(transformed);
		return new Vec3(transformed.x(), transformed.y(), transformed.z());
	}

	private static float snap(float value)
	{
		final float epsilon = 1e-5f;
		if(Math.abs(value) < epsilon)
			return 0;
		if(Math.abs(value-1) < epsilon)
			return 1;
		return value;
	}

	public static class Loader implements UnbakedModelLoader<PortedCoresampleModel>
	{
		@Override
		public PortedCoresampleModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			TextureSlots.Data textures = modelContents.has("textures")?
					TextureSlots.parseTextureMap(modelContents.getAsJsonObject("textures")):
					TextureSlots.Data.EMPTY;
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			return new PortedCoresampleModel(textures, transforms, parent);
		}
	}
}
