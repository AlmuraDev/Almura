/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraftforge.common.BiomeManager.BiomeType;

public class BiomeProperty implements IProperty<BiomeType> {

    private final BiomeType type;
    private final RangeProperty temperatureRange, humidityRange;

    public BiomeProperty(BiomeType type, RangeProperty temperatureRange, RangeProperty humidityRange) {
        this.type = type;
        this.temperatureRange = temperatureRange;
        this.humidityRange = humidityRange;
    }

    @Override
    public BiomeType getSource() {
        return type;
    }

    public RangeProperty getTemperatureRange() {
        return temperatureRange;
    }

    public RangeProperty getHumidityRange() {
        return humidityRange;
    }
}
