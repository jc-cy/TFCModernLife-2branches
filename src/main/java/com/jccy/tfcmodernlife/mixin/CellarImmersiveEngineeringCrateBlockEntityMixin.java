package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateBlockEntity;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    private void tfc_modern_life$syncCrate()
    {
        CellarPreservationHelper.syncContainerBlockEntity((BlockEntity) (Object) this, (Container) (Object) this);
    }
}
