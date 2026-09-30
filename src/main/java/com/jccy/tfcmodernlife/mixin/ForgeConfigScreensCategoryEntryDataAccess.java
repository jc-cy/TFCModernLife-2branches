package com.jccy.tfcmodernlife.mixin;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 只为拿到 Forge Config Screens 分组条目持有的配置节点（{@code EntryData$CategoryEntryData#config}），
 * 供 {@code ForgeConfigScreensEntryDataMixin} 判断"这个分组是不是本模组的配置"。
 */
@Pseudo
@Mixin(targets = "fuzs.forgeconfigscreens.client.gui.data.EntryData$CategoryEntryData", remap = false)
public interface ForgeConfigScreensCategoryEntryDataAccess
{
    @Accessor("config")
    UnmodifiableConfig tfc_modern_life$getConfig();
}
