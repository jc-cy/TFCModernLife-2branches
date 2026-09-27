package com.jccy.tfcmodernlife.common.block;

import net.minecraft.util.StringRepresentable;

public enum ClimateControlMode implements StringRepresentable
{
    IDLE("idle"),
    COLD("cold"),
    HEAT("heat");

    private final String name;

    ClimateControlMode(String name)
    {
        this.name = name;
    }

    @Override
    public String getSerializedName()
    {
        return name;
    }
}
