/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.IEApi;
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
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public record PortedConveyorModel(
		Identifier conveyorType,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();
	public static final Identifier LOADER_ID = IEApi.ieLoc("models/conveyor");
	public static final String TYPE_KEY = "conveyorType";

	private static final Identifier TEXTURE_CONVEYOR = IEApi.ieLoc("block/conveyor/conveyor");
	private static final Identifier TEXTURE_DROPPER = IEApi.ieLoc("block/conveyor/dropper");
	private static final Identifier TEXTURE_REDSTONE = IEApi.ieLoc("block/conveyor/redstone");
	private static final Identifier TEXTURE_SPLIT = IEApi.ieLoc("block/conveyor/split");
	private static final Identifier TEXTURE_VERTICAL = IEApi.ieLoc("block/conveyor/vertical");
	private static final Identifier TEXTURE_CASING_SIDE = IEApi.ieLoc("block/conveyor/casing_side");
	private static final Identifier TEXTURE_CASING_WALLS = IEApi.ieLoc("block/conveyor/casing_walls");
	private static final Identifier TEXTURE_CASING_FULL = IEApi.ieLoc("block/conveyor/casing_full");
	private static final Identifier TEXTURE_SPLIT_WALL = IEApi.ieLoc("block/conveyor/split_wall");

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
		QuadCollection.Builder result = new QuadCollection.Builder();
		Transformation transform = modelState.transformation().blockCenterToCorner();

		TextureAtlasSprite belt = spriteFor(textureSlots, modelBaker, name, beltTexture(conveyorType));
		TextureAtlasSprite casingSide = spriteFor(textureSlots, modelBaker, name, TEXTURE_CASING_SIDE);
		TextureAtlasSprite casingWalls = spriteFor(textureSlots, modelBaker, name, TEXTURE_CASING_WALLS);
		TextureAtlasSprite casingFull = spriteFor(textureSlots, modelBaker, name, TEXTURE_CASING_FULL);
		TextureAtlasSprite splitWall = spriteFor(textureSlots, modelBaker, name, TEXTURE_SPLIT_WALL);

		if("vertical".equals(conveyorType.getPath()))
			bakeVertical(
					result, transform, belt, spriteFor(textureSlots, modelBaker, name, TEXTURE_CONVEYOR),
					casingSide, casingWalls, casingFull
			);
		else
			bakeHorizontal(result, transform, belt, casingSide, casingWalls, casingFull, splitWall);
		return result.build();
	}

	private void bakeHorizontal(
			QuadCollection.Builder result,
			Transformation transform,
			TextureAtlasSprite belt,
			TextureAtlasSprite casingSide,
			TextureAtlasSprite casingWalls,
			TextureAtlasSprite casingFull,
			TextureAtlasSprite splitWall
	)
	{
		addBox(result, transform, new Vec3(0, 0, 0), new Vec3(1, .125, 1),
				side -> side==Direction.DOWN?casingFull: side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
		addBox(result, transform, new Vec3(.0625, .125, 0), new Vec3(.9375, .1875, 1), side -> belt);
		addBox(result, transform, new Vec3(0, .125, 0), new Vec3(.0625, .1875, 1),
				side -> side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
		addBox(result, transform, new Vec3(.9375, .125, 0), new Vec3(1, .1875, 1),
				side -> side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
		addBox(result, transform, new Vec3(0, .125, 0), new Vec3(1, .1875, .0625), side -> casingWalls);
		addBox(result, transform, new Vec3(0, .125, .9375), new Vec3(1, .1875, 1), side -> casingWalls);

		if("splitter".equals(conveyorType.getPath()))
			addBox(result, transform, new Vec3(.46875, .1875, 0), new Vec3(.53125, .25, .5625), side -> splitWall);
	}

	private void bakeVertical(
			QuadCollection.Builder result,
			Transformation transform,
			TextureAtlasSprite verticalBelt,
			TextureAtlasSprite lowerBelt,
			TextureAtlasSprite casingSide,
			TextureAtlasSprite casingWalls,
			TextureAtlasSprite casingFull
	)
	{
		addBox(result, transform, new Vec3(0, 0, 0), new Vec3(1, .125, .75),
				side -> side==Direction.DOWN?casingFull: side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
		addBox(result, transform, new Vec3(.0625, .125, 0), new Vec3(.9375, .1875, .75), side -> lowerBelt);
		addBox(result, transform, new Vec3(0, 0, .6875), new Vec3(1, 1, 1),
				side -> side.getAxis()==Direction.Axis.Z?casingWalls: casingSide);
		addBox(result, transform, new Vec3(.0625, 0, .625), new Vec3(.9375, 1, .6875), side -> verticalBelt);
		addBox(result, transform, new Vec3(0, 0, .625), new Vec3(.0625, 1, 1),
				side -> side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
		addBox(result, transform, new Vec3(.9375, 0, .625), new Vec3(1, 1, 1),
				side -> side.getAxis()==Direction.Axis.Y?casingWalls: casingSide);
	}

	private TextureAtlasSprite spriteFor(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelDebugName name, Identifier texture
	)
	{
		Material.Baked material = modelBaker.materials().resolveSlot(textureSlots, texture.toString(), name);
		return material.sprite();
	}

	private static Identifier beltTexture(Identifier conveyorType)
	{
		return switch(conveyorType.getPath())
		{
			case "dropper" -> TEXTURE_DROPPER;
			case "redstone" -> TEXTURE_REDSTONE;
			case "splitter" -> TEXTURE_SPLIT;
			case "vertical" -> TEXTURE_VERTICAL;
			default -> TEXTURE_CONVEYOR;
		};
	}

	public static void addTextureSlots(JsonObject textures, Identifier conveyorType)
	{
		addTextureSlot(textures, beltTexture(conveyorType));
		addTextureSlot(textures, TEXTURE_CONVEYOR);
		addTextureSlot(textures, TEXTURE_CASING_SIDE);
		addTextureSlot(textures, TEXTURE_CASING_WALLS);
		addTextureSlot(textures, TEXTURE_CASING_FULL);
		addTextureSlot(textures, TEXTURE_SPLIT_WALL);
		if(!textures.has("particle"))
			textures.addProperty("particle", beltTexture(conveyorType).toString());
	}

	private static void addTextureSlot(JsonObject textures, Identifier texture)
	{
		if(!textures.has(texture.toString()))
			textures.addProperty(texture.toString(), texture.toString());
	}

	private static void addBox(
			QuadCollection.Builder result, Transformation transform, Vec3 from, Vec3 to,
			Function<Direction, TextureAtlasSprite> textureGetter
	)
	{
		addFace(result, transform, Direction.DOWN, new Vec3[]{
				new Vec3(from.x, from.y, from.z), new Vec3(from.x, from.y, to.z),
				new Vec3(to.x, from.y, to.z), new Vec3(to.x, from.y, from.z)
		}, new double[]{from.x*16, 16-from.z*16, to.x*16, 16-to.z*16}, textureGetter.apply(Direction.DOWN), true);
		addFace(result, transform, Direction.UP, new Vec3[]{
				new Vec3(from.x, to.y, from.z), new Vec3(from.x, to.y, to.z),
				new Vec3(to.x, to.y, to.z), new Vec3(to.x, to.y, from.z)
		}, new double[]{from.x*16, from.z*16, to.x*16, to.z*16}, textureGetter.apply(Direction.UP), false);
		addFace(result, transform, Direction.NORTH, new Vec3[]{
				new Vec3(to.x, to.y, from.z), new Vec3(to.x, from.y, from.z),
				new Vec3(from.x, from.y, from.z), new Vec3(from.x, to.y, from.z)
		}, new double[]{from.x*16, 16-to.y*16, to.x*16, 16-from.y*16}, textureGetter.apply(Direction.NORTH), false);
		addFace(result, transform, Direction.SOUTH, new Vec3[]{
				new Vec3(to.x, to.y, to.z), new Vec3(to.x, from.y, to.z),
				new Vec3(from.x, from.y, to.z), new Vec3(from.x, to.y, to.z)
		}, new double[]{16-to.x*16, 16-to.y*16, 16-from.x*16, 16-from.y*16}, textureGetter.apply(Direction.SOUTH), true);
		addFace(result, transform, Direction.WEST, new Vec3[]{
				new Vec3(from.x, to.y, to.z), new Vec3(from.x, from.y, to.z),
				new Vec3(from.x, from.y, from.z), new Vec3(from.x, to.y, from.z)
		}, new double[]{to.z*16, 16-to.y*16, from.z*16, 16-from.y*16}, textureGetter.apply(Direction.WEST), true);
		addFace(result, transform, Direction.EAST, new Vec3[]{
				new Vec3(to.x, to.y, to.z), new Vec3(to.x, from.y, to.z),
				new Vec3(to.x, from.y, from.z), new Vec3(to.x, to.y, from.z)
		}, new double[]{16-to.z*16, 16-to.y*16, 16-from.z*16, 16-from.y*16}, textureGetter.apply(Direction.EAST), false);
	}

	private static void addFace(
			QuadCollection.Builder result, Transformation transform, Direction side, Vec3[] vertices,
			double[] uvs, TextureAtlasSprite sprite, boolean invert
	)
	{
		BakedQuadBuilder builder = new BakedQuadBuilder();
		Vec3i normalInt = side.getUnitVec3i();
		Vec3 faceNormal = new Vec3(normalInt.getX(), normalInt.getY(), normalInt.getZ());
		int vId = invert?3: 0;
		int u = vId > 1?2: 0;
		builder.putVertexData(transformPosition(transform, vertices[vId]), faceNormal, uvs[u], uvs[1], sprite, new float[]{1, 1, 1, 1}, 1);
		vId = invert?2: 1;
		u = vId > 1?2: 0;
		builder.putVertexData(transformPosition(transform, vertices[vId]), faceNormal, uvs[u], uvs[3], sprite, new float[]{1, 1, 1, 1}, 1);
		vId = invert?1: 2;
		u = vId > 1?2: 0;
		builder.putVertexData(transformPosition(transform, vertices[vId]), faceNormal, uvs[u], uvs[3], sprite, new float[]{1, 1, 1, 1}, 1);
		vId = invert?0: 3;
		u = vId > 1?2: 0;
		builder.putVertexData(transformPosition(transform, vertices[vId]), faceNormal, uvs[u], uvs[1], sprite, new float[]{1, 1, 1, 1}, 1);
		BakedQuad quad = builder.bake(-1, transformDirection(transform, side), sprite, true);
		result.addUnculledFace(quad);
	}

	private static Vec3 transformPosition(Transformation transform, Vec3 position)
	{
		Vector4f transformed = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
		transform.transformPosition(transformed);
		transformed.mul(1/transformed.w());
		return new Vec3(snap(transformed.x()), snap(transformed.y()), snap(transformed.z()));
	}

	private static Direction transformDirection(Transformation transform, Direction side)
	{
		Vec3i normal = side.getUnitVec3i();
		Vector3f transformed = new Vector3f(normal.getX(), normal.getY(), normal.getZ());
		transform.transformNormal(transformed);
		return Direction.getNearest(
				(int)Math.signum(transformed.x()),
				(int)Math.signum(transformed.y()),
				(int)Math.signum(transformed.z()),
				side
		);
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

	public static class Loader implements UnbakedModelLoader<PortedConveyorModel>
	{
		@Override
		public PortedConveyorModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			Identifier conveyorType = Identifier.parse(modelContents.get(TYPE_KEY).getAsString());
			JsonObject textures = modelContents.has("textures")?
					modelContents.getAsJsonObject("textures").deepCopy():
					new JsonObject();
			addTextureSlots(textures, conveyorType);
			TextureSlots.Data textureSlots = TextureSlots.parseTextureMap(textures);
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			return new PortedConveyorModel(conveyorType, textureSlots, transforms, parent);
		}
	}
}
