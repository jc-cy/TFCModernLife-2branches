package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.dries007.tfc.common.blockentities.TFCBlockEntity.class)
public abstract class TFCBlockEntityMixin
{
    @Inject(method = "onLoadAdditional", at = @At("TAIL"))
    private void tfcml$registerClimateStation(CallbackInfo ci)
    {
        if ((Object) this instanceof InventoryBlockEntity<?> inventory)
        {
            CellarPreservationHelper.syncInventoryBlockEntity(inventory);
        }
        if ((Object) this instanceof ClimateStationAccess station && (Object) this instanceof BlockEntity blockEntity)
        {
            ClimateStationRegistry.register(blockEntity, station);
            CellarPreservationHelper.syncTrackedInventories(station);
        }
    }

    @Inject(method = "onUnloadAdditional", at = @At("TAIL"))
    private void tfcml$unregisterClimateStation(CallbackInfo ci)
    {
        if ((Object) this instanceof ClimateStationAccess station && (Object) this instanceof BlockEntity blockEntity)
        {
            ClimateStationRegistry.unregister(blockEntity, station);
        }
    }
}
