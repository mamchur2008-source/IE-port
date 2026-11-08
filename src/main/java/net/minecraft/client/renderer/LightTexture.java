package net.minecraft.client.renderer;

public final class LightTexture
{
	public static final int FULL_BRIGHT = pack(15, 15);

	private LightTexture()
	{
	}

	public static int pack(int block, int sky)
	{
		return block<<4|sky<<20;
	}

	public static int block(int packedLight)
	{
		return packedLight>>4&15;
	}

	public static int sky(int packedLight)
	{
		return packedLight>>20&15;
	}
}
