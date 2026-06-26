package com.jccy.tfcmodernlife.common.block;

import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import java.util.List;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ThermostaticAirConditionerBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<ClimateControlMode> MODE = EnumProperty.create("mode", ClimateControlMode.class);
    private static final VoxelShape LOWER_SHAPE = Block.box(2, 0, 2, 14, 16, 14);
    private static final VoxelShape UPPER_SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public ThermostaticAirConditionerBlock()
    {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0f)
            .noOcclusion()
            .lightLevel(state -> state.getValue(MODE) == ClimateControlMode.HEAT ? 10 : 0));
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HALF, DoubleBlockHalf.LOWER)
            .setValue(OPEN, false)
            .setValue(MODE, ClimateControlMode.IDLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, HALF, OPEN, MODE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        final BlockPos pos = context.getClickedPos();
        final Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context))
        {
            return null;
        }
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof ThermostaticAirConditionerBlockEntity entity)
        {
            entity.load(stack.getOrCreateTagElement("BlockEntityTag"));
            entity.refreshStructure(true);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        final DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP))
        {
            return neighborState.is(this) && neighborState.getValue(HALF) != half ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ThermostaticAirConditionerBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (level.isClientSide() || state.getValue(HALF) != DoubleBlockHalf.LOWER)
        {
            return null;
        }
        return type == ModBlocks.THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY.get()
            ? (lvl, pos, blockState, be) -> ThermostaticAirConditionerBlockEntity.serverTick(lvl, pos, blockState, (ThermostaticAirConditionerBlockEntity) be)
            : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        final BlockPos entityPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(entityPos) instanceof ThermostaticAirConditionerBlockEntity entity && player instanceof ServerPlayer serverPlayer)
        {
            Helpers.openScreen(serverPlayer, entity, entityPos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder)
    {
        final List<ItemStack> drops = super.getDrops(state, builder);
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER)
        {
            final BlockEntity blockEntity = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof ThermostaticAirConditionerBlockEntity entity)
            {
                for (ItemStack drop : drops)
                {
                    if (drop.is(ModBlocks.THERMOSTATIC_AIR_CONDITIONER_ITEM.get()))
                    {
                        entity.saveEnergyToItem(drop);
                    }
                }
            }
        }
        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!state.is(newState.getBlock()))
        {
            final DoubleBlockHalf half = state.getValue(HALF);
            final BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            final BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half)
            {
                level.removeBlock(otherPos, false);
            }
            if (half == DoubleBlockHalf.LOWER && level.getBlockEntity(pos) instanceof ThermostaticAirConditionerBlockEntity entity)
            {
                if (!level.isClientSide())
                {
                    entity.onRemovedByBlock();
                }
                entity.invalidateCaps();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static void setMode(Level level, BlockPos pos, BlockState state, boolean open, ClimateControlMode mode)
    {
        final BlockState currentState = level.getBlockState(pos);
        if (!currentState.is(state.getBlock()) || currentState.getValue(HALF) != DoubleBlockHalf.LOWER)
        {
            return;
        }
        final BlockState lower = currentState.setValue(OPEN, open).setValue(MODE, mode);
        final BlockState upper = level.getBlockState(pos.above());
        level.setBlock(pos, lower, Block.UPDATE_ALL);
        if (upper.is(state.getBlock()) && upper.getValue(HALF) == DoubleBlockHalf.UPPER)
        {
            level.setBlock(pos.above(), upper.setValue(OPEN, open).setValue(MODE, mode), Block.UPDATE_ALL);
        }
    }
}
