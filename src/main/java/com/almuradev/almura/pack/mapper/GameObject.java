/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.mapper;

import net.minecraft.block.Block;

public class GameObject {

    public final String modid;
    public final Object minecraftObject;
    public final String remapped;
    public final Integer data;

    public GameObject(String modid, Object minecraftObject, String remapped, Integer data) {
        this.modid = modid;
        this.minecraftObject = minecraftObject;
        this.remapped = remapped;
        this.data = data;
    }

    public boolean isBlock() {
        return minecraftObject instanceof Block;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && remapped.equals(((GameObject) o).remapped);
    }

    @Override
    public int hashCode() {
        return remapped.hashCode();
    }
}
