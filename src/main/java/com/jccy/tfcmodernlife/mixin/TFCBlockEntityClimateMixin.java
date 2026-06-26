package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.ClimateStationAccess;
import com.jccy.tfcmodernlife.common.climate.ClimateStationRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.dries007.tfc.common.blockentities.TFCBlockEntity.class, remap = false)
public abstract class TFCBlockEntityClimateMixin
{
    @Inject(method = "onLoadAdditional", at = @At("TAIL"))
    private void tfcml$registerClimateStation(CallbackInfo ci)
    {
        if ((Object) this instanceof ClimateStationAccess station && (Object) this instanceof BlockEntity blockEntity)
        {
            ClimateStationRegistry.register(blockEntity, station);
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
