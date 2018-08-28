/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static org.junit.Assert.assertEquals;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;

import java.io.IOException;

public final class SerializationUtilTest {

    @Test
    public void testResourceLocationSerialization() {
        final ResourceLocation location = new ResourceLocation("test1", "test2");
        final String serialized = SerializationUtil.toString(location);

        assertEquals("test1:test2", serialized);

        final ResourceLocation location2 = SerializationUtil.fromString(serialized);

        assertEquals(location, location2);
    }

    @Test
    public void testCompoundSerialization() throws IOException {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("hi", false);

        assertEquals(compound, SerializationUtil.compoundFromBytes(SerializationUtil.toBytes(compound)));

        compound = new NBTTagCompound();
        final NBTTagList list = new NBTTagList();
        final NBTTagCompound listElement = new NBTTagCompound();
        listElement.setBoolean("test", false);
        list.appendTag(listElement);
        compound.setTag("test", list);

        assertEquals(compound, SerializationUtil.compoundFromBytes(SerializationUtil.toBytes(compound)));
    }
}
