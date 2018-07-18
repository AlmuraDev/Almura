/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

final class AssetStateEntry {
    final String id;
    final boolean enabled;
    State state = State.NEW;
    @Nullable AssetStateEntry previous;
    @Nullable AssetStateEntry next;
    @SuppressWarnings("NullableProblems")
    JarSearchEntry source;

    AssetStateEntry(final String id, final boolean enabled) {
        this.id = id;
        this.enabled = enabled;
    }

    Path resolve(final Path path) {
        return path.resolve(this.id + ".jar");
    }

    boolean is(final State state) {
        return this.state == state;
    }

    void to(final State state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("enabled", this.enabled)
                .add("state", this.state)
                .add("previous", this.previous != null ? this.previous.id : null)
                .add("next", this.next != null ? this.next.id : null)
                .toString();
    }

    enum State {
        NEW,
        EXTRACTED,
        ROLLED_BACK;

        static final Map<String, State> STATES = Arrays.stream(values()).collect(Collectors.toMap(input -> input.name().toUpperCase(Locale.ENGLISH), Function.identity()));
    }
}
