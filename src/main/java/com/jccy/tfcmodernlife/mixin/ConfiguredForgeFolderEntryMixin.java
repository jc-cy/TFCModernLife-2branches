package com.jccy.tfcmodernlife.mixin;

import java.util.List;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Configured 的 Forge 配置分组名同样只显示路径末段。分组键由 {@code builder.translation(...).push(...)} 写进
 * {@code ForgeConfigSpec.getLevelTranslationKey(...)}，这里把它接回界面显示。
 */
@Pseudo
@Mixin(targets = "com.mrcrayfish.configured.impl.forge.ForgeFolderEntry", remap = false)
public abstract class ConfiguredForgeFolderEntryMixin
{
    @Shadow @Final protected List<String> path;
    @Shadow @Final protected ForgeConfigSpec spec;

    @Inject(method = "getEntryName", at = @At("HEAD"), cancellable = true, require = 0)
    private void tfc_modern_life$useTranslatedFolderName(CallbackInfoReturnable<String> cir)
    {
        final String translationKey = spec.getLevelTranslationKey(path);
        if (translationKey != null && I18n.exists(translationKey))
        {
            cir.setReturnValue(I18n.get(translationKey));
        }
    }
}
