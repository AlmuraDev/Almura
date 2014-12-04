/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop.stage.property;

import com.almuradev.almura.pack.crop.stage.property.source.BiomeSource;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SoilProperty implements IProperty<BiomeProperty> {

    private final Block soil;
    private final BiomeProperty value;

    public SoilProperty(Block soil, BiomeProperty value) {
        this.soil = soil;
        this.value = value;
    }

    @Override
    public BiomeProperty getValue() {
        return value;
    }

    public Block getSoil() {
        return soil;
    }
}
