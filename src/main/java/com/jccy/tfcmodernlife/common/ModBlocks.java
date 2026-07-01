package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.block.ElectricOvenBlock;
import com.jccy.tfcmodernlife.common.block.ElectricSoupPotBlock;
import com.jccy.tfcmodernlife.common.block.RefrigeratorBlock;
import com.jccy.tfcmodernlife.common.block.ThermostaticAirConditionerBlock;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TFCModernLife.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TFCModernLife.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TFCModernLife.MOD_ID);

    public static final RegistryObject<ElectricOvenBlock> ELECTRIC_OVEN = BLOCKS.register("electric_oven", ElectricOvenBlock::new);
    public static final RegistryObject<ElectricSoupPotBlock> ELECTRIC_SOUP_POT = BLOCKS.register("electric_soup_pot", ElectricSoupPotBlock::new);
    public static final RegistryObject<ThermostaticAirConditionerBlock> THERMOSTATIC_AIR_CONDITIONER = BLOCKS.register("thermostatic_air_conditioner", ThermostaticAirConditionerBlock::new);
    public static final RegistryObject<RefrigeratorBlock> REFRIGERATOR = BLOCKS.register("refrigerator", RefrigeratorBlock::new);
    public static final RegistryObject<Block> STAINLESS_STEEL_REINFORCED_SEALED_BRICKS = BLOCKS.register("stainless_steel_reinforced_sealed_bricks",
        () -> new Block(reinforcedSealedBrickProperties()));
    public static final RegistryObject<DoorBlock> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR = BLOCKS.register("stainless_steel_reinforced_sealed_brick_door",
        () -> new DoorBlock(reinforcedSealedBrickProperties().noOcclusion(), BlockSetType.OAK));
    public static final RegistryObject<TrapDoorBlock> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR = BLOCKS.register("stainless_steel_reinforced_sealed_brick_trapdoor",
        () -> new TrapDoorBlock(reinforcedSealedBrickProperties().noOcclusion(), BlockSetType.OAK));

    public static final RegistryObject<BlockItem> ELECTRIC_OVEN_ITEM = ITEMS.register("electric_oven",
        () -> new BlockItem(ELECTRIC_OVEN.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> ELECTRIC_SOUP_POT_ITEM = ITEMS.register("electric_soup_pot",
        () -> new BlockItem(ELECTRIC_SOUP_POT.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> THERMOSTATIC_AIR_CONDITIONER_ITEM = ITEMS.register("thermostatic_air_conditioner",
        () -> new BlockItem(THERMOSTATIC_AIR_CONDITIONER.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> REFRIGERATOR_ITEM = ITEMS.register("refrigerator",
        () -> new BlockItem(REFRIGERATOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICKS_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_bricks",
        () -> new BlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICKS.get(), new Item.Properties()));
    public static final RegistryObject<DoubleHighBlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_brick_door",
        () -> new DoubleHighBlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICK_DOOR.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR_ITEM = ITEMS.register("stainless_steel_reinforced_sealed_brick_trapdoor",
        () -> new BlockItem(STAINLESS_STEEL_REINFORCED_SEALED_BRICK_TRAPDOOR.get(), new Item.Properties()));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<ElectricOvenBlockEntity>> ELECTRIC_OVEN_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_oven",
        () -> BlockEntityType.Builder.of(ElectricOvenBlockEntity::new, ELECTRIC_OVEN.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<ElectricSoupPotBlockEntity>> ELECTRIC_SOUP_POT_BLOCK_ENTITY = BLOCK_ENTITIES.register("electric_soup_pot",
        () -> BlockEntityType.Builder.of(ElectricSoupPotBlockEntity::new, ELECTRIC_SOUP_POT.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<ThermostaticAirConditionerBlockEntity>> THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY = BLOCK_ENTITIES.register("thermostatic_air_conditioner",
        () -> BlockEntityType.Builder.of(ThermostaticAirConditionerBlockEntity::new, THERMOSTATIC_AIR_CONDITIONER.get()).build(null));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<RefrigeratorBlockEntity>> REFRIGERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("refrigerator",
        () -> BlockEntityType.Builder.of(RefrigeratorBlockEntity::new, REFRIGERATOR.get()).build(null));

    private ModBlocks() {}

    private static BlockBehaviour.Properties reinforcedSealedBrickProperties()
    {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0f, 10.0f)
            .requiresCorrectToolForDrops();
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
