package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.common.automation.JarringStationAutomationItemHandler;
import com.jccy.tfcmodernlife.common.climate.CellarPreservationHelper;
import java.lang.reflect.Field;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InventoryBlockEntity.class, remap = false)
public abstract class InventoryBlockEntityMixin
{
    @Unique
    private static final @Nullable Field tfc_modern_life$inventoryField = tfc_modern_life$findInventoryField();

    @Unique
    private JarringStationAutomationItemHandler tfc_modern_life$jarringStationAutomation;

    @Unique
    private LazyOptional<IItemHandler> tfc_modern_life$jarringStationAutomationCapability = LazyOptional.empty();

    @Inject(method = "getCapability", at = @At("HEAD"), cancellable = true)
    private <T> void tfc_modern_life$getAutomationCapability(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir)
    {
        if (side != null && cap == Capabilities.ITEM && tfc_modern_life$isFirmaLifeJarringStation())
        {
            if (!tfc_modern_life$jarringStationAutomationCapability.isPresent())
            {
                final IItemHandlerModifiable inventory = tfc_modern_life$getInventory();
                if (inventory == null)
                {
                    return;
                }
                tfc_modern_life$jarringStationAutomation = new JarringStationAutomationItemHandler((BlockEntity) (Object) this, inventory);
                tfc_modern_life$jarringStationAutomationCapability = LazyOptional.of(() -> tfc_modern_life$jarringStationAutomation);
            }
            cir.setReturnValue(tfc_modern_life$jarringStationAutomationCapability.cast());
        }
    }

    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true)
    private <T> void tfc_modern_life$wrapCellarSidedInventory(Capability<T> cap, @Nullable Direction side, CallbackInfoReturnable<LazyOptional<T>> cir)
    {
        if (cap == Capabilities.ITEM && !tfc_modern_life$isFirmaLifeJarringStation())
        {
            final LazyOptional<IItemHandler> handlers = cir.getReturnValue().cast();
            cir.setReturnValue(handlers
                .lazyMap(handler -> CellarPreservationHelper.wrapSidedInventory((InventoryBlockEntity<?>) (Object) this, handler))
                .cast());
        }
    }

    @Inject(method = "setAndUpdateSlots", at = @At("TAIL"))
    private void tfc_modern_life$syncCellarTraitsOnSlotChange(int slot, CallbackInfo ci)
    {
        CellarPreservationHelper.syncInventoryBlockEntity((InventoryBlockEntity<?>) (Object) this);
    }

    @Inject(method = "ejectInventory", at = @At("HEAD"))
    private void tfc_modern_life$sanitizeDroppedCellarItems(CallbackInfo ci)
    {
        CellarPreservationHelper.sanitizeInventoryForDrop((InventoryBlockEntity<?>) (Object) this);
    }

    @Inject(method = "invalidateCapabilities", at = @At("HEAD"))
    private void tfc_modern_life$invalidateAutomationCapability(CallbackInfo ci)
    {
        tfc_modern_life$jarringStationAutomationCapability.invalidate();
        tfc_modern_life$jarringStationAutomationCapability = LazyOptional.empty();
        tfc_modern_life$jarringStationAutomation = null;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void tfc_modern_life$refreshJarringStationModelData(CompoundTag nbt, CallbackInfo ci)
    {
        if (tfc_modern_life$isFirmaLifeJarringStation())
        {
            final BlockEntity blockEntity = (BlockEntity) (Object) this;
            final Level level = blockEntity.getLevel();
            if (level != null && level.isClientSide)
            {
                blockEntity.requestModelDataUpdate();
                level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Unique
    private boolean tfc_modern_life$isFirmaLifeJarringStation()
    {
        return ((Object) this).getClass().getName().equals("com.eerussianguy.firmalife.common.blockentities.JarringStationBlockEntity");
    }

    @Unique
    private @Nullable IItemHandlerModifiable tfc_modern_life$getInventory()
    {
        if (tfc_modern_life$inventoryField != null)
        {
            try
            {
                final Object value = tfc_modern_life$inventoryField.get(this);
                if (value instanceof IItemHandlerModifiable handler)
                {
                    return handler;
                }
            }
            catch (IllegalAccessException ignored)
            {
            }
        }
        return null;
    }

    @Unique
    private static @Nullable Field tfc_modern_life$findInventoryField()
    {
        try
        {
            final Field field = InventoryBlockEntity.class.getDeclaredField("inventory");
            field.setAccessible(true);
            return field;
        }
        catch (ReflectiveOperationException ignored)
        {
            return null;
        }
    }
}
