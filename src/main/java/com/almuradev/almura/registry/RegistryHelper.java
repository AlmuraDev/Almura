/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.registry;

import com.almuradev.almura.asm.mixin.interfaces.IMixinSetCatalogTypeId;
import org.spongepowered.common.SpongeImplHooks;

import java.util.Locale;
import java.util.Map;

public interface RegistryHelper {

    default String withDomain(String id) {
        id = id.toLowerCase(Locale.ENGLISH);
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return id;
    }

    default <I, C> void registerSetId(final Map<String, I> map, String id, final C group) {
        final String name = id;
        id = SpongeImplHooks.getModIdFromClass(group.getClass()) + ':' + id;
        map.put(id, (I) group);
        ((IMixinSetCatalogTypeId) group).setId(id, name);
    }
}
