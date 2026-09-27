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

/**
 * Double chests (both TFC and vanilla) expose a vanilla {@link CompoundContainer} to their menu, so
 * the per-block-entity cellar sync never sees the combined container. Sync both halves whenever the
 * combined container changes.
 */
@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin implements CellarCompoundContainerAccess
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
    private void tfcml$syncCompoundChestCellarTraits(CallbackInfo ci)
    {
        tfcml$syncContainer(container1);
        tfcml$syncContainer(container2);
    }

    private void tfcml$syncContainer(Container container)
    {
        if (container instanceof BlockEntity blockEntity)
        {
            CellarPreservationHelper.syncContainerBlockEntity(blockEntity, container);
        }
    }
}
