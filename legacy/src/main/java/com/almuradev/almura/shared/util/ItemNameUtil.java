/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ItemNameUtil {

    // Items whose getItemStackDisplayName() only works on the client, so we stop asking them
    private static final Set<Item> CLIENT_ONLY_NAMES = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private ItemNameUtil() {
    }

    /**
     * Gets a stack's display name, safely on a dedicated server.
     *
     * <p>Some mods (e.g. Architecture's shapes) build item names with the client-only
     * {@code net.minecraft.client.resources.I18n}, which throws {@link NoClassDefFoundError} on a server.
     * Those items fall back to their custom name or the server-side translation of their key.</p>
     */
    @SuppressWarnings("deprecation")
    public static String getDisplayName(final ItemStack stack) {
        final Item item = stack.getItem();
        if (!CLIENT_ONLY_NAMES.contains(item)) {
            try {
                return stack.getDisplayName();
            } catch (final LinkageError e) {
                CLIENT_ONLY_NAMES.add(item);
            }
        }

        if (stack.hasDisplayName()) {
            return stack.getDisplayName();
        }
        return net.minecraft.util.text.translation.I18n.translateToLocal(stack.getTranslationKey() + ".name").trim();
    }
}
