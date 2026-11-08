/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEFluids.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends FlowingFluid
{
	private static FluidEntry entryStatic;
	protected final FluidEntry entry;

	public static IEFluid makeFluid(Function<FluidEntry, ? extends IEFluid> make, IEFluids.FluidEntry entry)
	{
		entryStatic = entry;
		IEFluid result = make.apply(entry);
		entryStatic = null;
		return result;
	}

	public IEFluid(IEFluids.FluidEntry entry)
	{
		this.entry = entry;
	}

	@Nonnull
	public Item getBucket()
	{
		return entry.getBucket();
	}

	protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isSame(fluidIn);
	}

	public boolean isSame(@Nonnull Fluid fluidIn)
	{
		return fluidIn==entry.getStill()||fluidIn==entry.getFlowing();
	}

	public int getTickDelay(LevelReader p_205569_1_)
	{
		// viscosity delta to water (1000)
		int dW = this.getFlowing().getFluidType().getViscosity()-Fluids.WATER.getFluidType().getViscosity();
		// dW for water & lava is 5000, difference in tick delay is 25 -> 0.005 as a modifier
		double v = Math.round(5 + dW*0.005);
		return Math.max(2, (int)v);
	}

	protected float getExplosionResistance()
	{
		return 100;
	}

	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
	{
		super.createFluidStateDefinition(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).properties())
			builder.add(p);
	}

	protected BlockState createLegacyBlock(FluidState state)
	{
		BlockState result = entry.getBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
		for(Property<?> prop : entry.properties())
			result = IEFluidBlock.withCopiedValue(prop, result, state);
		return result;
	}

	public boolean isSource(FluidState state)
	{
		return state.getType()==entry.getStill();
	}

	public int getAmount(FluidState state)
	{
		if(isSource(state))
			return 8;
		else
			return state.getValue(LEVEL);
	}

	public FluidType getFluidType()
	{
		return entry.type().value();
	}

	@Nonnull
	public Fluid getFlowing()
	{
		return entry.getFlowing();
	}

	@Nonnull
	public Fluid getSource()
	{
		return entry.getStill();
	}

	public boolean canConvertToSource(Level level)
	{
		return false;
	}

	protected boolean canConvertToSource(ServerLevel level)
	{
		return false;
	}

	protected void beforeDestroyingBlock(LevelAccessor iWorld, BlockPos blockPos, BlockState blockState)
	{

	}

	protected int getSlopeFindDistance(LevelReader iWorldReader)
	{
		return 4;
	}

	protected int getDropOff(LevelReader iWorldReader)
	{
		return 1;
	}

	public static Consumer<FluidType.Properties> createBuilder(int density, int viscosity)
	{
		return builder -> builder.viscosity(viscosity).density(density);
	}

	public static class Flowing extends IEFluid
	{
		public Flowing(IEFluids.FluidEntry entry)
		{
			super(entry);
		}

		protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}
	}

	public static class EntityFluidSerializer implements EntityDataSerializer<FluidStack>
	{
		public StreamCodec<? super RegistryFriendlyByteBuf, FluidStack> codec()
		{
			return FluidStack.STREAM_CODEC;
		}

		@Nonnull
		public FluidStack copy(FluidStack value)
		{
			return value.copy();
		}
	}

	public static final DispenseItemBehavior BUCKET_DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior()
	{
		private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

		public ItemStack execute(BlockSource source, ItemStack stack)
		{
			BucketItem bucketitem = (BucketItem)stack.getItem();
			BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
			Level world = source.level();
			if(bucketitem.emptyContents(null, world, blockpos, null))
			{
				bucketitem.checkExtraContent(null, world, stack, blockpos);
				return new ItemStack(Items.BUCKET);
			}
			else
				return this.defaultBehavior.dispense(source, stack);
		}
	};
}
