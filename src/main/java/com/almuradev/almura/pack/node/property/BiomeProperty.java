/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.node.property;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeProperty implements IProperty<BiomeGenBase> {

    private final BiomeGenBase biome;
    private final RangeProperty<Double> temperatureRange, humidityRange;

    public BiomeProperty(BiomeGenBase biome, RangeProperty<Double> temperatureRange, RangeProperty<Double> humidityRange) {
        this.biome = biome;
        this.temperatureRange = temperatureRange;
        this.humidityRange = humidityRange;
    }

    @Override
    public BiomeGenBase getSource() {
        return biome;
    }

    public RangeProperty<Double> getTemperatureRange() {
        return temperatureRange;
    }

    public RangeProperty<Double> getHumidityRange() {
        return humidityRange;
    }
}
