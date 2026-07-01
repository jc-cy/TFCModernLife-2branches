package com.jccy.tfcmodernlife.common.climate;

public enum CellarTier
{
    SEALED_BRICK("sealed_brick", 1.1f, -15),
    STAINLESS_STEEL_REINFORCED("stainless_steel_reinforced", 1.0f, -30);

    private final String id;
    private final float powerMultiplier;
    private final int minimumTemperature;

    CellarTier(String id, float powerMultiplier, int minimumTemperature)
    {
        this.id = id;
        this.powerMultiplier = powerMultiplier;
        this.minimumTemperature = minimumTemperature;
    }

    public String id()
    {
        return id;
    }

    public float powerMultiplier()
    {
        return ClimateControlConfig.getCellarPowerMultiplier(this);
    }

    public float defaultPowerMultiplier()
    {
        return powerMultiplier;
    }

    public int minimumTemperature()
    {
        return minimumTemperature;
    }

    public static CellarTier byId(String id)
    {
        for (CellarTier tier : values())
        {
            if (tier.id.equals(id))
            {
                return tier;
            }
        }
        return SEALED_BRICK;
    }

    public static CellarTier byConfigId(String id)
    {
        for (CellarTier tier : values())
        {
            if (tier.id.equals(id))
            {
                return tier;
            }
        }
        return null;
    }

    public static CellarTier max(CellarTier first, CellarTier second)
    {
        return first.ordinal() >= second.ordinal() ? first : second;
    }
}
