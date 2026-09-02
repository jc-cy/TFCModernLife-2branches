package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import com.jccy.tfcmodernlife.common.climate.CellarCompoundContainerAccess;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CompoundContainer.class)
public abstract class CellarCompoundContainerMixin implements CellarCompoundContainerAccess
{
    @Shadow @Final private Container container1;
    @Shadow @Final private Container container2;

    @Override
    public Container tfcml$getContainer1()
    {
        return container1;
    }

    @Override
    public Container tfcml$getContainer2()
    {
        return container2;
    }

    @Inject(method = "setChanged", at = @At("TAIL"))
    private void tfc_modern_life$syncCompoundChestCellarTraits(CallbackInfo ci)
    {
        tfc_modern_life$syncContainer(container1);
        tfc_modern_life$syncContainer(container2);
    }

    private void tfc_modern_life$syncContainer(Container container)
    {
        if (container instanceof BlockEntity blockEntity)
        {
            CellarPreservationHelper.syncContainerBlockEntity(blockEntity, container);
        }
    }
}
