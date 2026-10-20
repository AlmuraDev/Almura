/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload;

import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Bridge from the world load mixin to {@link ServerWorldLoadManager}.
 */
public final class WorldLoadHooks {

    @Nullable private static volatile Consumer<String> listener;

    static void setListener(@Nullable final Consumer<String> listener) {
        WorldLoadHooks.listener = listener;
    }

    /**
     * Called when a world's save handler is created, the first step of loading a world (before its chunk generator is built and its
     * spawn area prepared).
     *
     * @param worldFolderName The world's folder name, which is its name
     */
    public static void onSaveHandlerCreated(final String worldFolderName) {
        final Consumer<String> current = listener;
        if (current != null) {
            current.accept(worldFolderName);
        }
    }

    private WorldLoadHooks() {
    }
}
