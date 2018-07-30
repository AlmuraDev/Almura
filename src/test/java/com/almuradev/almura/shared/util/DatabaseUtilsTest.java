package com.almuradev.almura.shared.util;

import static org.junit.Assert.assertEquals;

import com.almuradev.almura.shared.database.DatabaseUtils;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;

public final class DatabaseUtilsTest {

    @Test
    public void testResourceLocationSerialization() {
        final ResourceLocation location = new ResourceLocation("test1", "test2");
        final String serialized = DatabaseUtils.toString(location);

        assertEquals("test1:test2", serialized);

        final ResourceLocation location2 = DatabaseUtils.fromString(serialized);

        assertEquals(location, location2);
    }
}
