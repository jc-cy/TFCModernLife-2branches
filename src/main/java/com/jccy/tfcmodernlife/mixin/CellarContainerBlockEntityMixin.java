package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NeoForge resolves block capabilities through a central, ordered provider table that vanilla containers already
 * occupy, so a mod provider added afterwards is never consulted (the previous per-block-entity capability mixin of
 * 1.20 has no 1.21 equivalent). Instead this hooks the {@link net.minecraft.world.Container} methods that both
 * vanilla hoppers and NeoForge's own container-backed item handler wrappers use, which covers hoppers, pipes and
 * manual container interaction alike. Only whitelisted containers inside a working cellar are affected.
 */
@Mixin(BaseContainerBlockEntity.class)
public abstract class CellarContainerBlockEntityMixin
{
    @ModifyVariable(method = "setItem", at = @At("HEAD"), argsOnly = true, require = 0)
    private ItemStack tfcml$tagInsertedCellarStack(ItemStack stack)
    {
        return CellarPreservationHelper.tagInsertedStack((BlockEntity) (Object) this, stack);
    }

    @Inject(method = "removeItem", at = @At("RETURN"), cancellable = true, require = 0)
    private void tfcml$sanitizeRemovedCellarStack(int index, int count, CallbackInfoReturnable<ItemStack> cir)
    {
        final ItemStack removed = cir.getReturnValue();
        if (removed != null && !removed.isEmpty())
        {
            cir.setReturnValue(CellarPreservationHelper.sanitizeExtractedStack((BlockEntity) (Object) this, removed));
        }
    }
}
