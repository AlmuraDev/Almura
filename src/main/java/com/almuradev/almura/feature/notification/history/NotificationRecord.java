/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.notification.history;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.time.Instant;

import javax.annotation.Nullable;

/**
 * A notification the client received, as kept in the notification history.
 */
@SideOnly(Side.CLIENT)
public final class NotificationRecord {

    public static final String TYPE_POPUP = "popup";
    public static final String TYPE_WINDOW = "window";

    private final Instant received;
    private final String type;
    private final String server;
    /** Title with legacy formatting codes. */
    private final String title;
    /** Message with legacy formatting codes. */
    private final String message;

    public NotificationRecord(final Instant received, final String type, final String server, final String title, final String message) {
        this.received = received;
        this.type = type;
        this.server = server;
        this.title = title;
        this.message = message;
    }

    public Instant getReceived() {
        return this.received;
    }

    public String getType() {
        return this.type;
    }

    public String getServer() {
        return this.server;
    }

    public String getTitle() {
        return this.title;
    }

    public String getMessage() {
        return this.message;
    }

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
     * @return the record, or null if the line isn't a valid record
     */
    @Nullable
    static NotificationRecord fromJson(final JsonObject json) {
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
