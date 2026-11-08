/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data;

import com.google.common.base.Preconditions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGenUtils
{
	private static final Pattern MTLLIB = Pattern.compile("^mtllib\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern USEMTL = Pattern.compile("^usemtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern NEWMTL = Pattern.compile("^newmtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern MAP_KD = Pattern.compile("^map_Kd\\s+(.*)$", Pattern.MULTILINE);

	public static String getTextureFromObj(Identifier obj, ExistingFileHelper helper)
	{
		return getTexturesFromObj(obj, helper).get(0);
	}

	public static List<String> getTexturesFromObj(Identifier obj, ExistingFileHelper helper)
	{
		try
		{
			String prefix = "models";
			if (obj.getPath().startsWith("models/"))
				prefix = "";
			Resource objResource = helper.getResource(obj, PackType.CLIENT_RESOURCES, "", prefix);
			InputStream objStream = objResource.open();
			String fullObj = IOUtils.toString(objStream, StandardCharsets.US_ASCII);
			String libLoc = findFirstOccurrenceGroup(MTLLIB, fullObj);
			Identifier libRL = relative(obj, libLoc);
			Set<String> materialNames = findAllOccurrenceGroups(USEMTL, fullObj);
			List<String> result = new ArrayList<>();
			for(String materialName : materialNames)
				result.add(getMTLTexture(libRL, materialName, helper));
			Preconditions.checkArgument(!result.isEmpty());
			return result;
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getMTLTexture(Identifier mtl, String materialName, ExistingFileHelper helper)
	{
		try
		{
			Resource mtlResource = helper.getResource(mtl, PackType.CLIENT_RESOURCES, "", "models");
			String fullMtl = IOUtils.toString(mtlResource.open(), StandardCharsets.US_ASCII);
			Matcher materialMatcher = NEWMTL.matcher(fullMtl);
			while(materialMatcher.find()&&!materialMatcher.group(1).equals(materialName)) ;
			int materialStart = materialMatcher.start();
			int materialEnd;
			if(materialMatcher.find())
				materialEnd = materialMatcher.start();
			else
				materialEnd = fullMtl.length();
			String material = fullMtl.substring(materialStart, materialEnd);
			return findFirstOccurrenceGroup(MAP_KD, material);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String findFirstOccurrenceGroup(Pattern pattern, String input)
	{
		Matcher matcher = pattern.matcher(input);
		Preconditions.checkArgument(matcher.find());
		return matcher.group(1);
	}

	private static Set<String> findAllOccurrenceGroups(Pattern pattern, String input)
	{
		Matcher matcher = pattern.matcher(input);
		Set<String> result = new LinkedHashSet<>();
		while(matcher.find())
			result.add(matcher.group(1));
		return result;
	}

	private static Identifier relative(Identifier base, String relativePath)
	{
		String basePath = base.getPath();
		String lastDir = basePath.substring(0, basePath.lastIndexOf('/')+1);
		return base.withPath(lastDir+relativePath);
	}
}
