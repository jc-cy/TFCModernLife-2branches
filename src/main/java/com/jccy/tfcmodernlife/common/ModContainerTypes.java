package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import com.jccy.tfcmodernlife.common.container.RefrigeratorContainer;
import com.jccy.tfcmodernlife.common.container.ThermostaticAirConditionerContainer;
import com.jccy.tfcmodernlife.common.container.ElectricOvenContainer;
import com.jccy.tfcmodernlife.common.container.ElectricSoupPotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModContainerTypes
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TFCModernLife.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricOvenContainer>> ELECTRIC_OVEN = ModContainerTypes.<ElectricOvenBlockEntity, ElectricOvenContainer>registerInventoryBlock("electric_oven", ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY, ElectricOvenContainer::create);
    public static final DeferredHolder<MenuType<?>, MenuType<ElectricSoupPotContainer>> ELECTRIC_SOUP_POT = ModContainerTypes.<ElectricSoupPotBlockEntity, ElectricSoupPotContainer>registerInventoryBlock("electric_soup_pot", ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY, ElectricSoupPotContainer::create);

    public static final DeferredHolder<MenuType<?>, MenuType<RefrigeratorContainer>> REFRIGERATOR = registerBlock("refrigerator", ModBlocks.REFRIGERATOR_BLOCK_ENTITY, RefrigeratorContainer::create);
    public static final DeferredHolder<MenuType<?>, MenuType<ThermostaticAirConditionerContainer>> THERMOSTATIC_AIR_CONDITIONER = registerBlock("thermostatic_air_conditioner", ModBlocks.THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY, ThermostaticAirConditionerContainer::create);

    private ModContainerTypes() {}

    private static <T extends BlockEntity, C extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<C>> registerBlock(String name, DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> type, MenuFactory<T, C> factory)
    {
        return MENUS.register(name, () -> IMenuTypeExtension.create((windowId, playerInventory, buffer) -> {
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

    private static <T extends net.dries007.tfc.common.blockentities.InventoryBlockEntity<?>, C extends net.dries007.tfc.common.container.BlockEntityContainer<T>> DeferredHolder<MenuType<?>, MenuType<C>> registerInventoryBlock(
        String name, Supplier<? extends BlockEntityType<T>> type, net.dries007.tfc.common.container.BlockEntityContainer.Factory<T, C> factory)
    {
        return MENUS.register(name, () -> IMenuTypeExtension.create((windowId, playerInventory, buffer) -> {
            final Level level = playerInventory.player.level();
            final BlockPos pos = buffer.readBlockPos();
            final T entity = level.getBlockEntity(pos, type.get()).orElseThrow();
            return factory.create(entity, playerInventory, windowId);
        }));
    }

    public static void register(IEventBus bus)
    {
        MENUS.register(bus);
    }
}
