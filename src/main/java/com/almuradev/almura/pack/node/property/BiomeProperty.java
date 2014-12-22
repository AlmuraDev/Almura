/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraftforge.common.BiomeManager.BiomeType;

public class BiomeProperty implements IProperty<BiomeType> {

    private final BiomeType type;
    private final RangeProperty<Double> temperatureRange, humidityRange;

    public BiomeProperty(BiomeType type, RangeProperty<Double> temperatureRange, RangeProperty<Double> humidityRange) {
        this.type = type;
        this.temperatureRange = temperatureRange;
        this.humidityRange = humidityRange;
    }

    @Override
    public BiomeType getSource() {
        return type;
    }

    public RangeProperty<Double> getTemperatureRange() {
        return temperatureRange;
    }

    public RangeProperty<Double> getHumidityRange() {
        return humidityRange;
    }
}
