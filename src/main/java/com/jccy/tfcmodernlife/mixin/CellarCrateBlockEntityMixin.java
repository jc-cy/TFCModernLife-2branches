package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cellar preservation for Immersive Engineering crates. IE 1.21 exposes its inventory through
 * {@code getInventoryCap()} (registered as the ITEM capability) instead of the 1.20 {@code getCapability}, so the
 * wrapper is applied there; the remaining hooks are unchanged from the 1.20 baseline.
 */
@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity", remap = false)
public abstract class CellarCrateBlockEntityMixin
{
    @Inject(method = "loadAdditional", at = @At("TAIL"), require = 0)
    private void tfcml$syncCellarTraitsOnLoad(CompoundTag tag, HolderLookup.Provider provider, CallbackInfo ci)
    {
        tfcml$syncCrate();
    }

    @Inject(method = "onBEPlaced(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), require = 0)
    private void tfcml$syncCellarTraitsOnPlaced(ItemStack stack, CallbackInfo ci)
    {
        tfcml$syncCrate();
    }

    @Inject(method = "getBlockEntityDrop", at = @At("HEAD"), require = 0)
    private void tfcml$sanitizeCellarTraitsBeforeDrop(LootContext context, Consumer<ItemStack> drop, CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeContainerBlockEntityForDrop((BlockEntity) (Object) this, (Container) (Object) this);
    }

    @Inject(method = "getInventoryCap", at = @At("RETURN"), cancellable = true, require = 0)
    private void tfcml$wrapCellarCrateHandler(CallbackInfoReturnable<IItemHandler> cir)
    {
        cir.setReturnValue(CellarPreservationHelper.wrapBlockEntityItemHandler((BlockEntity) (Object) this, cir.getReturnValue()));
    }

    @Unique
    private void tfcml$syncCrate()
    {
        CellarPreservationHelper.syncContainerBlockEntity((BlockEntity) (Object) this, (Container) (Object) this);
    }
}
