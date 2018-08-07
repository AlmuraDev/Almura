/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

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

    @Test
    public void testBigDecimalSerialization() {
        BigDecimal value = BigDecimal.valueOf(5);
        assertEquals(SerializationUtil.fromBytes(SerializationUtil.toBytes(value)).doubleValue(), value.doubleValue(), 0);

        value = BigDecimal.valueOf(5.5);
        checkState(SerializationUtil.fromBytes(SerializationUtil.toBytes(value)).doubleValue() == value.doubleValue());

        value = BigDecimal.valueOf(5.23598745268957);
        checkState(SerializationUtil.fromBytes(SerializationUtil.toBytes(value)).doubleValue() == value.doubleValue());

        value = BigDecimal.valueOf(Double.MAX_VALUE);
        checkState(SerializationUtil.fromBytes(SerializationUtil.toBytes(value)).doubleValue() == value.doubleValue());
    }
}
