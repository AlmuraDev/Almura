/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import com.almuradev.almura.Almura;
import com.almuradev.core.event.Witness;
import com.google.common.base.Charsets;
import org.jooq.DSLContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.inject.Inject;

public final class DatabaseInstaller implements Witness {

    private final DatabaseManager manager;

    @Inject
    public DatabaseInstaller(final DatabaseManager manager) {
        this.manager = manager;
    }

    @Listener(order = Order.FIRST)
    public void onGameStartingServer(final GameStartingServerEvent event) {
        try (final DSLContext context = this.manager.createContext(false)) {
            context.createSchemaIfNotExists(this.manager.getConfiguration().getDatabase()).execute();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        try (final DSLContext context = this.manager.createContext(true)) {
            this.setupTablesForFeature(context, "axs");
            this.setupTablesForFeature(context, "store");
            this.setupTablesForFeature(context, "title");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setupTablesForFeature(final DSLContext context, final String feature) throws Exception {
        FileSystem fileSystem = null;

        try {
            final URI uri = Almura.class.getResource("/db/" + feature + "/database.sql").toURI();

            Path path;

            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath("/db/" + feature + "/database.sql");
            } else {
                path = Paths.get(uri);
            }

            if (path != null) {
                final byte[] encoded;
                encoded = Files.readAllBytes(path);
                final String sql = new String(encoded, Charsets.UTF_8);
                context.execute(sql);
            }
        } finally {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }
}
