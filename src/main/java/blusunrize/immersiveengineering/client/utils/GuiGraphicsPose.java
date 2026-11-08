/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GuiGraphicsPose
{
	private static final Method POSE_METHOD = findPoseMethod();

	public static Object pose(GuiGraphicsExtractor graphics)
	{
		try
		{
			return POSE_METHOD.invoke(graphics);
		} catch(IllegalAccessException|InvocationTargetException e)
		{
			throw new RuntimeException("Failed to access GuiGraphicsExtractor pose stack", e);
		}
	}

	public static void push(Object pose)
	{
		if(pose instanceof PoseStack stack)
			stack.pushPose();
		else if(pose instanceof Matrix3x2fStack stack)
			stack.pushMatrix();
		else
			throw unknownPoseType(pose);
	}

	public static void pop(Object pose)
	{
		if(pose instanceof PoseStack stack)
			stack.popPose();
		else if(pose instanceof Matrix3x2fStack stack)
			stack.popMatrix();
		else
			throw unknownPoseType(pose);
	}

	public static void translate(Object pose, float x, float y, float z)
	{
		if(pose instanceof PoseStack stack)
			stack.translate(x, y, z);
		else if(pose instanceof Matrix3x2fStack stack)
			stack.translate(x, y);
		else
			throw unknownPoseType(pose);
	}

	public static void scale(Object pose, float x, float y, float z)
	{
		if(pose instanceof PoseStack stack)
			stack.scale(x, y, z);
		else if(pose instanceof Matrix3x2fStack stack)
			stack.scale(x, y);
		else
			throw unknownPoseType(pose);
	}

	public static void rotateZ(Object pose, float radians)
	{
		if(pose instanceof PoseStack stack)
			stack.mulPose(new Quaternionf().rotateZ(radians));
		else if(pose instanceof Matrix3x2fStack stack)
			stack.rotate(radians);
		else
			throw unknownPoseType(pose);
	}

	private static Method findPoseMethod()
	{
		try
		{
			return GuiGraphicsExtractor.class.getMethod("pose");
		} catch(NoSuchMethodException e)
		{
			throw new RuntimeException("GuiGraphicsExtractor no longer exposes a pose stack", e);
		}
	}

	private static RuntimeException unknownPoseType(Object pose)
	{
		return new IllegalArgumentException("Unknown GUI pose stack type: "+(pose==null?"null": pose.getClass().getName()));
	}
}
