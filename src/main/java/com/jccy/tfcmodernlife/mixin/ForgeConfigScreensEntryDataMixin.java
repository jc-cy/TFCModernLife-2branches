package com.jccy.tfcmodernlife.mixin;

import com.jccy.tfcmodernlife.client.ConfigScreenTranslationHelper;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge Config Screens 的配置项标题已经会读 ForgeConfigSpec 的翻译键，只有分组标题直接用路径生成，
 * 所以这里只替换 CategoryEntryData 的标题（其余子类保持原样）。归属判断见
 * {@link ConfigScreenTranslationHelper#getCategoryTitle}：界面是所有 mod 共用的，不能只看路径名。
 */
@Pseudo
@Mixin(targets = "fuzs.forgeconfigscreens.client.gui.data.EntryData", remap = false)
public abstract class ForgeConfigScreensEntryDataMixin
{
    @Shadow @Final private String path;

    @Inject(method = "getTitle", at = @At("HEAD"), cancellable = true, require = 0)
    private void tfc_modern_life$useTranslatedCategoryTitle(CallbackInfoReturnable<Component> cir)
    {
        if (!(this instanceof ForgeConfigScreensCategoryEntryDataAccess category))
        {
            return;
        }

        final Component translated = ConfigScreenTranslationHelper.getCategoryTitle(category.tfc_modern_life$getConfig(), path);
        if (translated != null)
        {
            cir.setReturnValue(translated);
        }
    }
}
