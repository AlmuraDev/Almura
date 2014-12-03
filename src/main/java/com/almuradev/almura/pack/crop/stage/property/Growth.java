package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.IProperty;

public class Growth implements IProperty {
    public final double minChance;
    public final double maxChance;

    public Growth(double minChance, double maxChance) {
        this.minChance = minChance;
        this.maxChance = maxChance;
    }
}
