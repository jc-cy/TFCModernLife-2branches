package com.jccy.tfcmodernlife.common.climate;

import net.minecraft.world.Container;

/**
 * Exposes the two halves of a vanilla {@link net.minecraft.world.CompoundContainer} (double chest) so the
 * cellar code can resolve the real block entity behind a combined container slot.
 */
public interface CellarCompoundContainerAccess
{
    Container tfcml$getContainer1();

    Container tfcml$getContainer2();
}
