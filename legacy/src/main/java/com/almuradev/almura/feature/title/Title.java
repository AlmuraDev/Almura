/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public final class Title implements Serializable {

    private final Timestamp created;
    private final UUID creator;
    private final String id, permission;
    private final String content;
    private final boolean isHidden;

    private transient final Text apiContent;

    public Title(final Timestamp created, final UUID creator, final String id, final String permission, final boolean isHidden,
        final String content) {

        this.created = created;
        this.creator = creator;
        this.id = id;
        this.permission = permission;
        this.isHidden = isHidden;
        this.content = content;

        this.apiContent = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(this.content);
    }

    public Timestamp getCreated() {
        return this.created;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public String getId() {
        return this.id;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public String getContent() {
        return this.content;
    }

    public Text asText() {
        return this.apiContent;
    }

    public Title copy() {
        return new Title(this.created, this.creator, this.id, this.permission, this.isHidden, this.content);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Title other = (Title) o;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .addValue(this.id)
            .addValue(this.created)
            .addValue(this.creator)
            .addValue(this.permission)
            .addValue(this.content)
            .toString();
    }
}
