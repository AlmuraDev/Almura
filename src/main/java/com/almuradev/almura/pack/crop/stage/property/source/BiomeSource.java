/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property.source;

import net.minecraftforge.common.BiomeManager.BiomeType;

public class BiomeSource implements ISource<BiomeType> {

    private final BiomeType type;
    private final RangeSource temperatureRange, humidityRange;

    public BiomeSource(BiomeType type, RangeSource temperatureRange, RangeSource humidityRange) {
        this.type = type;
        this.temperatureRange = temperatureRange;
        this.humidityRange = humidityRange;
    }

    @Override
    public BiomeType getSource() {
        return type;
    }

    public RangeSource getTemperatureRange() {
        return temperatureRange;
    }

    public RangeSource getHumidityRange() {
        return humidityRange;
    }
}
