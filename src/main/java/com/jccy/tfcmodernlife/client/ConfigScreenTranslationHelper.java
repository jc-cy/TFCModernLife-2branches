package com.jccy.tfcmodernlife.client;

import com.jccy.tfcmodernlife.TFCModernLife;
import com.jccy.tfcmodernlife.common.ModConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Forge 1.20 没有内置的配置界面，游戏内看到的配置界面来自配置界面模组（Configured / Forge Config Screens）。
 * 这些模组只会显示配置项的路径名，不读取 ForgeConfigSpec 的翻译键，因此由 mixin 把名字换成语言文件里的文案。
 *
 * <p>本类只处理 Forge Config Screens 的分组标题：它的分组标题直接用路径末段生成，没有对应的
 * ForgeConfigSpec 翻译键，所以这里按 {@code tfc_modern_life.config.category.<路径>} 约定查找，
 * 路径就是 {@code push(...)} 里的段名，与语言文件里的键一一对应。
 *
 * <p>这个界面是所有 mod 共用的，所以必须先确认"当前分组是不是本模组配置树里的节点"，否则别的 mod
 * 里同名的分组（例如 power）会被套上我们的中文名。
 */
public final class ConfigScreenTranslationHelper
{
    private static final String CATEGORY_PREFIX = TFCModernLife.MOD_ID + ".config.category.";

    private ConfigScreenTranslationHelper() {}

    @Nullable
    public static Component getCategoryTitle(UnmodifiableConfig category, String path)
    {
        if (!isOwnCategory(category))
        {
            return null;
        }

        final String key = CATEGORY_PREFIX + path;
        return I18n.exists(key) ? Component.translatable(key) : null;
    }

    /**
     * 归属判断用身份比较：Forge Config Screens 交给界面的节点就是 {@code ForgeConfigSpec#getValues()} 的子树
     * （已按 8.0.2 反编译确认），所以只要在本模组 spec 的节点树里找到同一个对象即可，不靠名字猜。
     */
    private static boolean isOwnCategory(UnmodifiableConfig category)
    {
        return containsNode(ModConfig.COMMON_SPEC.getValues(), category);
    }

    private static boolean containsNode(UnmodifiableConfig node, UnmodifiableConfig target)
    {
        if (node == target)
        {
            return true;
        }
        for (Object child : node.valueMap().values())
        {
            if (child instanceof UnmodifiableConfig nested && containsNode(nested, target))
            {
                return true;
            }
        }
        return false;
    }
}
