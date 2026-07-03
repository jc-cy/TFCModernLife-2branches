package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import java.util.function.Consumer;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WoodenCrateBlockEntity.class, remap = false)
public abstract class CellarImmersiveEngineeringCrateBlockEntityMixin
{
    @Inject(method = {"loadAdditional", "m_142466_"}, at = @At("TAIL"), require = 0)
    private void tfc_modern_life$syncCellarTraitsOnLoad(CompoundTag tag, CallbackInfo ci)
    {
        tfc_modern_life$syncCrate();
    }

    @Inject(method = "onBEPlaced(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), require = 0)
    private void tfc_modern_life$syncCellarTraitsOnPlaced(ItemStack stack, CallbackInfo ci)
    {
        tfc_modern_life$syncCrate();
    }

    @Inject(method = "getBlockEntityDrop", at = @At("HEAD"), require = 0)
    private void tfc_modern_life$sanitizeCellarTraitsBeforeDrop(LootContext context, Consumer<ItemStack> drop, CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeContainerBlockEntityForDrop((BlockEntity) (Object) this, (Container) (Object) this);
    }

    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true)
    private <T> void tfc_modern_life$wrapCellarCrateCapability(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir)
    {
        if (cap == Capabilities.ITEM)
        {
            final BlockEntity owner = (BlockEntity) (Object) this;
            final LazyOptional<IItemHandler> handlers = cir.getReturnValue().cast();
            cir.setReturnValue(handlers
                .lazyMap(handler -> CellarPreservationHelper.wrapBlockEntityItemHandler(owner, handler))
                .cast());
        }
    }

    private void tfc_modern_life$syncCrate()
    {
        CellarPreservationHelper.syncContainerBlockEntity((BlockEntity) (Object) this, (Container) (Object) this);
    }
}
