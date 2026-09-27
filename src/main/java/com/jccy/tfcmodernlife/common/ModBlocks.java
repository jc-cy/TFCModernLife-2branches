package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.block.ReinforcedSealedBricksBlock;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.item.EnergyPreservingBlockItem;
import com.jccy.tfcmodernlife.common.block.ElectricOvenBlock;
import com.jccy.tfcmodernlife.common.block.ElectricSoupPotBlock;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TFCModernLife.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TFCModernLife.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TFCModernLife.MOD_ID);

    public static final DeferredHolder<Block, ElectricOvenBlock> ELECTRIC_OVEN = BLOCKS.register("electric_oven", ElectricOvenBlock::new);
    public static final DeferredHolder<Block, ElectricSoupPotBlock> ELECTRIC_SOUP_POT = BLOCKS.register("electric_soup_pot", ElectricSoupPotBlock::new);

    public static final DeferredHolder<Item, BlockItem> ELECTRIC_OVEN_ITEM = ITEMS.register("electric_oven",
        () -> new BlockItem(ELECTRIC_OVEN.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> ELECTRIC_SOUP_POT_ITEM = ITEMS.register("electric_soup_pot",
        () -> new BlockItem(ELECTRIC_SOUP_POT.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricOvenBlockEntity>> ELECTRIC_OVEN_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_oven",
        () -> BlockEntityType.Builder.of(ElectricOvenBlockEntity::new, ELECTRIC_OVEN.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricSoupPotBlockEntity>> ELECTRIC_SOUP_POT_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_soup_pot",
        () -> BlockEntityType.Builder.of(ElectricSoupPotBlockEntity::new, ELECTRIC_SOUP_POT.get()).build(null));

    public static final DeferredHolder<Block, ThermostaticAirConditionerBlock> THERMOSTATIC_AIR_CONDITIONER = BLOCKS.register("thermostatic_air_conditioner", ThermostaticAirConditionerBlock::new);
    public static final DeferredHolder<Block, RefrigeratorBlock> REFRIGERATOR = BLOCKS.register("refrigerator", RefrigeratorBlock::new);
    public static final DeferredHolder<Block, ReinforcedSealedBricksBlock> STAINLESS_STEEL_REINFORCED_SEALED_BRICKS = BLOCKS.register("stainless_steel_reinforced_sealed_bricks",
        () -> new ReinforcedSealedBricksBlock(reinforcedSealedBrickProperties()));
    public static final DeferredHolder<Block, DoorBlock> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR = BLOCKS.register("stainless_steel_reinforced_sealed_brick_door",
        () -> new DoorBlock(BlockSetType.OAK, reinforcedSealedBrickProperties().noOcclusion()));
    public static final DeferredHolder<Block, TrapDoorBlock> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR = BLOCKS.register("stainless_steel_reinforced_sealed_brick_trapdoor",
        () -> new TrapDoorBlock(BlockSetType.OAK, reinforcedSealedBrickProperties().noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> THERMOSTATIC_AIR_CONDITIONER_ITEM = ITEMS.register("thermostatic_air_conditioner",
        () -> new EnergyPreservingBlockItem(THERMOSTATIC_AIR_CONDITIONER.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> REFRIGERATOR_ITEM = ITEMS.register("refrigerator",
        () -> new EnergyPreservingBlockItem(REFRIGERATOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICKS_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_bricks",
        () -> new BlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICKS.get(), new Item.Properties()));
    public static final DeferredHolder<Item, DoubleHighBlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_brick_door",
        () -> new DoubleHighBlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_brick_trapdoor",
        () -> new BlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermostaticAirConditionerBlockEntity>> THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY = BLOCK_ENTITIES.register("thermostatic_air_conditioner",
        () -> BlockEntityType.Builder.of(ThermostaticAirConditionerBlockEntity::new, THERMOSTATIC_AIR_CONDITIONER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefrigeratorBlockEntity>> REFRIGERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("refrigerator",
        () -> BlockEntityType.Builder.of(RefrigeratorBlockEntity::new, REFRIGERATOR.get()).build(null));

    private ModBlocks() {}

    private static BlockBehaviour.Properties reinforcedSealedBrickProperties()
    {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f, 10.0f);
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
