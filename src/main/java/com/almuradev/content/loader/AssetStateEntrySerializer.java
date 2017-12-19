/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

final class AssetStateEntrySerializer implements JsonDeserializer<AssetStateEntry>, JsonSerializer<AssetStateEntry> {

    @Override
    public AssetStateEntry deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject object = (JsonObject) json;
        final String id = object.getAsJsonPrimitive("id").getAsString();
        final boolean enabled = !object.has("enabled") || object.getAsJsonPrimitive("enabled").getAsBoolean();
        return new AssetStateEntry(id, enabled);
    }

    @Override
    public JsonElement serialize(final AssetStateEntry src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject object = new JsonObject();
        object.addProperty("id", src.id);
        object.addProperty("enabled", src.enabled);
        return object;
    }
}
