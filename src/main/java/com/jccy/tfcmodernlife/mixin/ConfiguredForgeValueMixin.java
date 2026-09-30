package com.jccy.tfcmodernlife.mixin;

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
 * Configured 的 Forge 配置项只显示路径末段，不读 ForgeConfigSpec 的翻译键。
 * 本模组的配置项都用 {@code builder.translation(...)} 指定了键，这里把它接回界面显示。
 */
@Pseudo
@Mixin(targets = "com.mrcrayfish.configured.impl.forge.ForgeValue", remap = false)
public abstract class ConfiguredForgeValueMixin
{
    @Shadow @Final public ForgeConfigSpec.ValueSpec valueSpec;

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true, require = 0)
    private void tfc_modern_life$useTranslatedValueName(CallbackInfoReturnable<String> cir)
    {
        final String translationKey = valueSpec.getTranslationKey();
        if (translationKey != null && I18n.exists(translationKey))
        {
            cir.setReturnValue(I18n.get(translationKey));
        }
    }
}
