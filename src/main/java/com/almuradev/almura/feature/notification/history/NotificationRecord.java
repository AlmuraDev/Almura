/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * A notification as kept in the history: when it came, its kind (popup or window), the server it came from, and its title and message with
 * their colour codes.
 */
public record NotificationRecord(Instant received, String type, String server, String title, String message) {

    public static final String TYPE_POPUP = "popup";
    public static final String TYPE_WINDOW = "window";

    JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("received", this.received.toString());
        json.addProperty("type", this.type);
        json.addProperty("server", this.server);
        json.addProperty("title", this.title);
        json.addProperty("message", this.message);
        return json;
    }

    /**
     * @return The record, or null if the line isn't one (the file is 1.12's format, so a history carried over still reads)
     */
    static @Nullable NotificationRecord fromJson(final JsonObject json) {
        if (!json.has("received") || !json.has("message")) {
            return null;
        }
        return new NotificationRecord(
            Instant.parse(json.get("received").getAsString()),
            json.has("type") ? json.get("type").getAsString() : TYPE_POPUP,
            json.has("server") ? json.get("server").getAsString() : "",
            json.has("title") ? json.get("title").getAsString() : "",
            json.get("message").getAsString());
    }
}
