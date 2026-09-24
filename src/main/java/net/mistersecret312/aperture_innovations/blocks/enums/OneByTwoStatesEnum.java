package net.mistersecret312.aperture_innovations.blocks.enums;

import net.minecraft.util.StringRepresentable;

public enum OneByTwoStatesEnum implements StringRepresentable {
    SINGLE("single"),
    UPPER("upper"),
    LOWER("lower");

    final String state;

    OneByTwoStatesEnum(String state) {
        this.state = state;
    }

    @Override
    public String getSerializedName() {
        return this.state;
    }

    @Override
    public String toString() {
        return this.state;
    }
}