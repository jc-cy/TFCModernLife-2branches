package com.jccy.tfcmodernlife.common.block;

import com.jccy.tfcmodernlife.common.ModBlocks;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RefrigeratorBlock extends Block implements EntityBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public RefrigeratorBlock()
    {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0f)
            .noOcclusion());
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        if (level.getBlockEntity(pos) instanceof RefrigeratorBlockEntity entity)
        {
            entity.load(stack.getOrCreateTagElement("BlockEntityTag"));
            entity.refreshStructure(true);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new RefrigeratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        if (type != ModBlocks.REFRIGERATOR_BLOCK_ENTITY.get())
        {
            return null;
        }
        return level.isClientSide()
            ? (lvl, pos, blockState, be) -> RefrigeratorBlockEntity.clientTick(lvl, pos, blockState, (RefrigeratorBlockEntity) be)
            : (lvl, pos, blockState, be) -> RefrigeratorBlockEntity.serverTick(lvl, pos, blockState, (RefrigeratorBlockEntity) be);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof RefrigeratorBlockEntity entity && player instanceof ServerPlayer serverPlayer)
        {
            Helpers.openScreen(serverPlayer, entity, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder)
    {
        final List<ItemStack> drops = super.getDrops(state, builder);
        final BlockEntity blockEntity = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof RefrigeratorBlockEntity entity)
        {
            for (ItemStack drop : drops)
            {
                if (drop.is(ModBlocks.REFRIGERATOR_ITEM.get()))
                {
                    entity.saveEnergyToItem(drop);
                }
            }
        }
        return drops;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof RefrigeratorBlockEntity entity)
        {
            if (!level.isClientSide())
            {
                entity.onRemovedByBlock();
            }
            entity.invalidateCaps();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static void setActive(Level level, BlockPos pos, BlockState state, boolean active)
    {
        final BlockState currentState = level.getBlockState(pos);
        if (currentState.is(state.getBlock()) && currentState.getValue(ACTIVE) != active)
        {
            level.setBlock(pos, currentState.setValue(ACTIVE, active), Block.UPDATE_ALL);
        }
    }
}
