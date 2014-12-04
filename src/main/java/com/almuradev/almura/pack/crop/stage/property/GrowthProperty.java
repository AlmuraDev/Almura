/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.RangeSource;

public class GrowthProperty implements IProperty<RangeSource> {

    private final RangeSource source;

    public GrowthProperty(RangeSource source) {
        this.source = source;
    }

    @Override
    public RangeSource getValue() {
        return source;
    }
}
