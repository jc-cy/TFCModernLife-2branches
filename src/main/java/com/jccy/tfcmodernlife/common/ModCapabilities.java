package com.jccy.tfcmodernlife.common;

import com.jccy.tfcmodernlife.common.blockentity.RefrigeratorBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ThermostaticAirConditionerBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricOvenBlockEntity;
import com.jccy.tfcmodernlife.common.blockentity.ElectricSoupPotBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities
{
    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlocks.ELECTRIC_OVEN_BLOCK_ENTITY.get(), (be, side) -> side == null ? be.getInventory() : be.getAutomationInventory());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY.get(), (be, side) -> side == null ? be.getInventory().getItemHandler() : be.getAutomationInventory());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlocks.ELECTRIC_SOUP_POT_BLOCK_ENTITY.get(), ElectricSoupPotBlockEntity::getSidedFluidInventory);
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlocks.REFRIGERATOR_BLOCK_ENTITY.get(),
            (be, side) -> be.getEnergyStorage()
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlocks.THERMOSTATIC_AIR_CONDITIONER_BLOCK_ENTITY.get(),
            (be, side) -> be.getEnergyStorage()
        );
    }
}
