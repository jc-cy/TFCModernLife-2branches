package com.jccy.tfcmodernlife.common.climate;

import java.util.Locale;

public enum GreenhouseTier
{
    WOOD("wood", 1.3f, 5, 3, 5),
    COPPER("copper", 1.2f, 10, 5, 10),
    IRON("iron", 1.1f, 18, 7, 15),
    STAINLESS_STEEL("stainless_steel", 1.0f, 70, 10, 20);

    private final String id;
    private final float powerMultiplier;
    private final int manualRange;
    private final int autoRange;
    private final int minimumFirmalifeTier;

    GreenhouseTier(String id, float powerMultiplier, int manualRange, int autoRange, int minimumFirmalifeTier)
    {
        this.id = id;
        this.powerMultiplier = powerMultiplier;
        this.manualRange = manualRange;
        this.autoRange = autoRange;
        this.minimumFirmalifeTier = minimumFirmalifeTier;
    }

    public String id()
    {
        return id;
    }

    public float powerMultiplier()
    {
        return powerMultiplier;
    }

    public int manualRange()
    {
        return manualRange;
    }

    public int autoRange()
    {
        return autoRange;
    }

    public int minimumFirmalifeTier()
    {
        return minimumFirmalifeTier;
    }

    public static GreenhouseTier byId(String id)
    {
        for (GreenhouseTier tier : values())
        {
            if (tier.id.equals(id))
            {
                return tier;
            }
        }
        return WOOD;
    }

    public static GreenhouseTier byFirmalifeTier(int firmalifeTier)
    {
        GreenhouseTier result = WOOD;
        for (GreenhouseTier tier : values())
        {
            if (firmalifeTier >= tier.minimumFirmalifeTier)
            {
                result = tier;
            }
        }
        return result;
    }

    public static GreenhouseTier byMatcherDescription(String matcherDescription)
    {
        final String normalized = matcherDescription.toLowerCase(Locale.ROOT);
        if (normalized.contains("stainless_steel"))
        {
            return STAINLESS_STEEL;
        }
        if (normalized.contains("iron"))
        {
            return IRON;
        }
        if (normalized.contains("copper"))
        {
            return COPPER;
        }
        return WOOD;
    }
}
