/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public class MultiblockBlockEntityDummy<State extends IMultiblockState>
		extends BlockEntity
		implements IModelOffsetProvider, IMultiblockBE<State>
{
	private final IMultiblockBEHelperDummy<State> helper;

	public MultiblockBlockEntityDummy(
			BlockEntityType<?> type,
			BlockPos worldPosition,
			BlockState blockState,
			MultiblockRegistration<State> multiblock
	)
	{
		super(type, worldPosition, blockState);
		this.helper = IMultiblockBEHelperDummy.MAKE_HELPER.get().makeFor(this, multiblock);
	}

	@Override
	protected void loadAdditional(ValueInput input)
	{
		super.loadAdditional(input);
		helper.load(input.read("ieData", CompoundTag.CODEC).orElseGet(CompoundTag::new), input.lookup());
		requestModelDataUpdate();
	}

	@Override
	protected void saveAdditional(ValueOutput output)
	{
		super.saveAdditional(output);
		CompoundTag tag = new CompoundTag();
		helper.saveAdditional(tag, Provider.create(Stream.empty()));
		output.store("ieData", CompoundTag.CODEC, tag);
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag(Provider provider)
	{
		CompoundTag tag = super.getUpdateTag(provider);
		CompoundTag ieData = new CompoundTag();
		helper.saveAdditional(ieData, provider);
		tag.put("ieData", ieData);
		return tag;
	}

	public void handleUpdateTag(CompoundTag tag, Provider provider)
	{
		helper.handleUpdateTag(getIEData(tag), provider);
		requestModelDataUpdate();
	}

	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider provider)
	{
		helper.onDataPacket(getIEData(pkt.getTag()), provider);
		requestModelDataUpdate();
	}

	private static CompoundTag getIEData(CompoundTag tag)
	{
		if(tag.contains("ieData"))
			return tag.getCompoundOrEmpty("ieData");
		return tag;
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket()
	{
		return helper.getUpdatePacket();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state)
	{
		super.preRemoveSideEffects(pos, state);
		helper.disassemble();
	}

	@Override
	public IMultiblockBEHelperDummy<State> getHelper()
	{
		return helper;
	}

	@Override
	public ModelData getModelData()
	{
		return ModelData.of(Model.SUBMODEL_OFFSET, getModelOffset(getBlockState(), getMultiblockSize()));
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		BlockPos mirroredPosInMB = helper.getPositionInMB();
		if(helper.getMultiblock().mirrorable()&&state.getValue(IEProperties.MIRRORED))
		{
			if(size==null)
				size = getMultiblockSize();
			mirroredPosInMB = new BlockPos(
					size.getX()-mirroredPosInMB.getX()-1,
					mirroredPosInMB.getY(),
					mirroredPosInMB.getZ()
			);
		}
		return mirroredPosInMB.subtract(helper.getMultiblock().masterPosInMB());
	}

	private Vec3i getMultiblockSize()
	{
		Level level = getLevel();
		return helper.getMultiblock().size(level);
	}
}
