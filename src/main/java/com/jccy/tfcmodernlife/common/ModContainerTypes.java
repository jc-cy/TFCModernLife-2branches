package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.container.ElectricOvenContainer;
import com.jccy.tfcmodernlife.common.container.ElectricSoupPotContainer;
import com.jccy.tfcmodernlife.common.container.RefrigeratorContainer;
import com.jccy.tfcmodernlife.common.container.ThermostaticAirConditionerContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModContainerTypes
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TFCModernLife.MOD_ID);

    public static final RegistryObject<MenuType<ElectricOvenContainer>> ELECTRIC_OVEN = ModContainerTypes.<ElectricOvenBlockEntity, ElectricOvenContainer>registerBlock("electric_oven",
        ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY, ElectricOvenContainer::create);

    public static final RegistryObject<MenuType<ElectricSoupPotContainer>> ELECTRIC_SOUP_POT = ModContainerTypes.<ElectricSoupPotBlockEntity, ElectricSoupPotContainer>registerBlock("electric_soup_pot",
        ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY, ElectricSoupPotContainer::create);

    public static final RegistryObject<MenuType<ThermostaticAirConditionerContainer>> THERMOSTATIC_AIR_CONDITIONER = ModContainerTypes.<ThermostaticAirConditionerBlockEntity, ThermostaticAirConditionerContainer>registerBlock("thermostatic_air_conditioner",
        ModBlocks.THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY, ThermostaticAirConditionerContainer::create);

    public static final RegistryObject<MenuType<RefrigeratorContainer>> REFRIGERATOR = ModContainerTypes.<RefrigeratorBlockEntity, RefrigeratorContainer>registerBlock("refrigerator",
        ModBlocks.REFRIGERATOR_BLOCK_ENTITY, RefrigeratorContainer::create);

    private ModContainerTypes() {}

    private static <T extends BlockEntity, C extends AbstractContainerMenu> RegistryObject<MenuType<C>> registerBlock(
        String name, RegistryObject<BlockEntityType<T>> type, MenuFactory<T, C> factory)
    {
        return MENUS.register(name, () -> IForgeMenuType.create((windowId, playerInventory, buffer) -> {
            final Level level = playerInventory.player.level();
            final BlockPos pos = buffer.readBlockPos();
            final T entity = level.getBlockEntity(pos, type.get()).orElseThrow();
            return factory.create(entity, playerInventory, windowId);
        }));
    }

    @FunctionalInterface
    private interface MenuFactory<T extends BlockEntity, C extends AbstractContainerMenu>
    {
        C create(T blockEntity, Inventory playerInventory, int windowId);
    }

    public static void register(IEventBus bus)
    {
        MENUS.register(bus);
    }
}
