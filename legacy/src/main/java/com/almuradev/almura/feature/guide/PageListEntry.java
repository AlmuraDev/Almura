/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class PageListEntry {

    private final String id, name;
    // Player guides: who it belongs to and whether that is the player the listing was sent to
    private final boolean playerGuide, mine;
    private final String ownerName;

    public PageListEntry(final String id, final String name) {
        this(id, name, false, false, "");
    }

    public PageListEntry(final String id, final String name, final boolean playerGuide, final boolean mine, final String ownerName) {
        this.id = id;
        this.name = name;
        this.playerGuide = playerGuide;
        this.mine = mine;
        this.ownerName = ownerName;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean isPlayerGuide() {
        return this.playerGuide;
    }

    public boolean isMine() {
        return this.mine;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PageListEntry that = (PageListEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("name", this.name)
                .add("playerGuide", this.playerGuide)
                .add("mine", this.mine)
                .add("ownerName", this.ownerName)
                .toString();
    }
}
