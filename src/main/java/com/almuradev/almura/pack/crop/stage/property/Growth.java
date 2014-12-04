/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
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
