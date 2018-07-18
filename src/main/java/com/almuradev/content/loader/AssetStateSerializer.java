/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

final class AssetStateSerializer implements JsonDeserializer<AssetState>, JsonSerializer<AssetState> {
    @Override
    public AssetState deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject object = (JsonObject) json;
        final Map<String, AssetStateEntry> entries = this.deserializeEntries(object.getAsJsonArray("entries"), context);
        return new AssetState(entries);
    }

    private Map<String, AssetStateEntry> deserializeEntries(final JsonArray array, final JsonDeserializationContext context) {
        final Map<String, AssetStateEntry> entries = new LinkedHashMap<>();
        for (final JsonElement element : array) {
            final AssetStateEntry entry = context.deserialize(element, AssetStateEntry.class);
            entries.put(entry.id, entry);
        }

        final ListIterator<AssetStateEntry> it = new ArrayList<>(entries.values()).listIterator();
        while (it.hasNext()) {
            final AssetStateEntry previous = it.hasPrevious() ? it.previous() : null;
            final AssetStateEntry current = it.next();
            final AssetStateEntry next = it.hasNext() ? it.next() : null;
            if (previous != null) {
                previous.next = current;
            }
            current.next = next;
            if (next != null) {
                next.previous = current;
            }
        }

        return entries;
    }

    @Override
    public JsonElement serialize(final AssetState src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        final JsonArray entries = new JsonArray();
        for (final AssetStateEntry entry : src.entries.values()) {
            entries.add(context.serialize(entry));
        }
        object.add("entries", entries);
        return object;
    }
}
