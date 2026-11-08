package net.minecraft.client.resources.model.geometry;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BakedQuad
{
	private static final int POSITION_OFFSET = getOffset(VertexFormatElement.POSITION);
	private static final int COLOR_OFFSET = getOffset(VertexFormatElement.COLOR);
	private static final int UV_OFFSET = getOffset(VertexFormatElement.UV);
	private static final int NORMAL_OFFSET = getOffset(VertexFormatElement.NORMAL);
	private static final int VERTEX_SIZE = DefaultVertexFormat.BLOCK.getVertexSize()/4;

	private final int[] vertices;
	private final Direction direction;
	private final MaterialInfo materialInfo;
	private final Vector3fc position0;
	private final Vector3fc position1;
	private final Vector3fc position2;
	private final Vector3fc position3;
	private final long packedUV0;
	private final long packedUV1;
	private final long packedUV2;
	private final long packedUV3;

	private static int getOffset(VertexFormatElement element)
	{
		int offset = 0;
		for(VertexFormatElement e : DefaultVertexFormat.BLOCK.getElements())
			if(e==element)
				return offset/4;
			else
				offset += e.byteSize();
		throw new IllegalStateException("Did not find vertex element with type "+element.type().name());
	}

	public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade)
	{
		this.vertices = vertices;
		this.direction = direction;
		this.materialInfo = MaterialInfo.of(new Material.Baked(sprite, false), sprite.transparency(), tintIndex, shade, 0);
		this.position0 = readPosition(vertices, 0);
		this.position1 = readPosition(vertices, 1);
		this.position2 = readPosition(vertices, 2);
		this.position3 = readPosition(vertices, 3);
		this.packedUV0 = readPackedUV(vertices, 0);
		this.packedUV1 = readPackedUV(vertices, 1);
		this.packedUV2 = readPackedUV(vertices, 2);
		this.packedUV3 = readPackedUV(vertices, 3);
	}

	public BakedQuad(
			Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3,
			long uv0, long uv1, long uv2, long uv3, Direction direction, MaterialInfo materialInfo
	)
	{
		this.vertices = writeVertices(p0, p1, p2, p3, uv0, uv1, uv2, uv3, direction);
		this.direction = direction;
		this.materialInfo = materialInfo;
		this.position0 = p0;
		this.position1 = p1;
		this.position2 = p2;
		this.position3 = p3;
		this.packedUV0 = uv0;
		this.packedUV1 = uv1;
		this.packedUV2 = uv2;
		this.packedUV3 = uv3;
	}

	public int[] getVertices()
	{
		return vertices;
	}

	public int getTintIndex()
	{
		return materialInfo.tintIndex();
	}

	public Direction getDirection()
	{
		return direction;
	}

	public Direction direction()
	{
		return direction;
	}

	public TextureAtlasSprite getSprite()
	{
		return materialInfo.sprite();
	}

	public boolean isShade()
	{
		return materialInfo.shade();
	}

	public MaterialInfo materialInfo()
	{
		return materialInfo;
	}

	public Vector3fc position(int vertex)
	{
		return switch(vertex)
		{
			case 0 -> position0;
			case 1 -> position1;
			case 2 -> position2;
			case 3 -> position3;
			default -> throw new IndexOutOfBoundsException(vertex);
		};
	}

	public long packedUV(int vertex)
	{
		return switch(vertex)
		{
			case 0 -> packedUV0;
			case 1 -> packedUV1;
			case 2 -> packedUV2;
			case 3 -> packedUV3;
			default -> throw new IndexOutOfBoundsException(vertex);
		};
	}

	public Vector3fc position0()
	{
		return position0;
	}

	public Vector3fc position1()
	{
		return position1;
	}

	public Vector3fc position2()
	{
		return position2;
	}

	public Vector3fc position3()
	{
		return position3;
	}

	public long packedUV0()
	{
		return packedUV0;
	}

	public long packedUV1()
	{
		return packedUV1;
	}

	public long packedUV2()
	{
		return packedUV2;
	}

	public long packedUV3()
	{
		return packedUV3;
	}

	private static Vector3fc readPosition(int[] vertices, int vertex)
	{
		int base = vertex*VERTEX_SIZE+POSITION_OFFSET;
		return new Vector3f(
				Float.intBitsToFloat(vertices[base]),
				Float.intBitsToFloat(vertices[base+1]),
				Float.intBitsToFloat(vertices[base+2])
		);
	}

	private static long readPackedUV(int[] vertices, int vertex)
	{
		int base = vertex*VERTEX_SIZE+UV_OFFSET;
		return UVPair.pack(Float.intBitsToFloat(vertices[base]), Float.intBitsToFloat(vertices[base+1]));
	}

	private static int[] writeVertices(
			Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3,
			long uv0, long uv1, long uv2, long uv3, Direction direction
	)
	{
		int[] vertices = new int[4*VERTEX_SIZE];
		Vector3fc normal = direction.getUnitVec3f();
		writeVertex(vertices, 0, p0, uv0, normal);
		writeVertex(vertices, 1, p1, uv1, normal);
		writeVertex(vertices, 2, p2, uv2, normal);
		writeVertex(vertices, 3, p3, uv3, normal);
		return vertices;
	}

	private static void writeVertex(int[] vertices, int vertex, Vector3fc position, long uv, Vector3fc normal)
	{
		int base = vertex*VERTEX_SIZE;
		vertices[base+POSITION_OFFSET] = Float.floatToRawIntBits(position.x());
		vertices[base+POSITION_OFFSET+1] = Float.floatToRawIntBits(position.y());
		vertices[base+POSITION_OFFSET+2] = Float.floatToRawIntBits(position.z());
		vertices[base+COLOR_OFFSET] = -1;
		vertices[base+UV_OFFSET] = Float.floatToRawIntBits(UVPair.unpackU(uv));
		vertices[base+UV_OFFSET+1] = Float.floatToRawIntBits(UVPair.unpackV(uv));
		vertices[base+NORMAL_OFFSET] = packNormal(normal);
	}

	private static int packNormal(Vector3fc normal)
	{
		return ((byte)(normal.x()*127)&255)|(((byte)(normal.y()*127)&255) << 8)|(((byte)(normal.z()*127)&255) << 16);
	}

	public record MaterialInfo(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType, int tintIndex, boolean shade, int lightEmission)
	{
		public static MaterialInfo of(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission)
		{
			return new MaterialInfo(material.sprite(), null, null, tintIndex, shade, lightEmission);
		}

		public int flags()
		{
			return 0;
		}
	}
}
