/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.google.common.base.MoreObjects;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;

import java.time.Instant;
import java.util.UUID;

public final class Page {

    private final String id;
    private final Instant created;
    private final UUID creator;

    private Instant lastModified;
    private UUID lastModifier;
    private String name, content;
    private int index;

    public Page(String id, UUID creator) {
        this(id, creator, Instant.now());
    }

    public Page(String id, UUID creator, Instant created) {
        this.id = id;
        this.creator = creator;
        this.created = created;
    }

    public String getId() {
        return this.id;
    }

    public int getIndex() {
        return index;
    }

    public Page setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Page setName(String name) {
        this.name = name;
        return this;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public Instant getCreated() {
        return this.created;
    }

    public UUID getLastModifier() {
        return this.lastModifier;
    }

    public Page setLastModifier(UUID lastModifier) {
        this.lastModifier = lastModifier;
        return this;
    }

    public Instant getLastModified() {
        return this.lastModified;
    }

    public Page setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getContent() {
        return this.content;
    }

    public Page setContent(String content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && this.id.equals(((Page) o).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public static String asFriendlyText(final String string) {
        return LegacyTexts.replace(string, '&', SpongeTexts.COLOR_CHAR);
    }

    public static String asUglyText(final String string) {
        return LegacyTexts.replace(string, SpongeTexts.COLOR_CHAR, '&');
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("index", this.index)
                .add("name", this.name)
                .add("creator", this.creator)
                .add("created", this.created)
                .add("lastModifier", this.lastModifier)
                .add("lastModified", this.lastModified)
                .add("content", this.content)
                .toString();
    }
}
