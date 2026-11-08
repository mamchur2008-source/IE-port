/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.Objects;

import static blusunrize.immersiveengineering.api.IEApiDataComponents.ATTACHED_SHADER;

/**
 * @author BluSunrize - 09.11.2016
 */
public class CapabilityShader
{
	// TODO probably rework
	public static final ItemCapability<ShaderWrapper, Void> ITEM = ItemCapability.createVoid(
			IEApi.ieLoc("shader_item"), ShaderWrapper.class
	);
	public static final BlockCapability<ShaderWrapper, Void> BLOCK = BlockCapability.createVoid(
			IEApi.ieLoc("shader_block"), ShaderWrapper.class
	);

	public static boolean shouldReequipDueToShader(ItemStack oldStack, ItemStack newStack)
	{
		ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.ITEM);
		ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.ITEM);
		if(wrapperOld==null&&wrapperNew!=null)
			return true;
		else if(wrapperOld!=null&&wrapperNew==null)
			return true;
		else if(wrapperOld!=null)
			return !Objects.equals(wrapperOld.getShader(), wrapperNew.getShader());
		else
			return false;
	}

	public interface ShaderWrapper
	{
		Identifier getShaderType();

		void setShader(@Nullable Identifier shader);

		@Nullable
		Identifier getShader();

		default ShaderCase getCase()
		{
			var shaderStack = getShader();
			if(shaderStack!=null)
				return ShaderRegistry.getShader(shaderStack, getShaderType());
			else
				return null;
		}
	}

	public static class ShaderWrapper_Item implements ShaderWrapper
	{
		private final ItemStack container;
		private final Identifier shaderType;

		public ShaderWrapper_Item(Identifier type, ItemStack container)
		{
			this.shaderType = type;
			this.container = container;
		}

		@Override
		public Identifier getShaderType()
		{
			return shaderType;
		}

		@Override
		public void setShader(@Nullable Identifier shader)
		{
			if(shader!=null)
				container.set(ATTACHED_SHADER, shader);
			else
				container.remove(ATTACHED_SHADER);
		}

		@Override
		@Nullable
		public Identifier getShader()
		{
			return container.get(ATTACHED_SHADER);
		}
	}

	public static class ShaderWrapper_Direct implements ShaderWrapper
	{
		public static final IAttachmentSerializer<ShaderWrapper_Direct> SERIALIZER = new WrapperSerializer();

		@Nullable
		private Identifier shader = null;
		private final Identifier type;

		public ShaderWrapper_Direct(Identifier type)
		{
			this.type = type;
		}

		public Identifier getShaderType()
		{
			return type;
		}

		@Override
		public void setShader(@Nullable Identifier shader)
		{
			this.shader = shader;
		}

		@Override
		@Nullable
		public Identifier getShader()
		{
			return this.shader;
		}
	}

	public static final class WrapperSerializer implements IAttachmentSerializer<ShaderWrapper_Direct>
	{
		@Override
		public boolean write(ShaderWrapper_Direct attachment, ValueOutput output)
		{
			var shader = attachment.getShader();
			if(shader!=null)
				output.putString("IE:Shader", shader.toString());
			else
				output.putString("IE:NoShader", "");
			output.putString("IE:ShaderType", attachment.getShaderType().toString());
			return true;
		}

		@Override
		public ShaderWrapper_Direct read(IAttachmentHolder holder, ValueInput input)
		{
			ShaderWrapper_Direct wrapper = new ShaderWrapper_Direct(Identifier.parse(input.getStringOr("IE:ShaderType", "")));
			if(input.getStringOr("IE:NoShader", "").isEmpty())
				wrapper.setShader(Identifier.parse(input.getStringOr("IE:Shader", "")));
			return wrapper;
		}
	}

	public static ModelProperty<ShaderCase> MODEL_PROPERTY = new ModelProperty<>();
}
