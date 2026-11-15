/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.database;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.database.AlmuraServerConfig.Dialect;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.h2.Driver;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Almura's SQL database, as 3.5's: H2 in a file (the default) or PostgreSQL, set in {@code config/almura/server.toml}
 * ({@link AlmuraServerConfig}). Every query runs on the one database thread, in the order asked, over one connection opened when first
 * needed (and again if it drops); what follows a query runs back on the server's thread.
 * <p>
 * A feature's tables are made by {@link #install}, from {@code /almura/database/<feature>.sql} in the jar: statements written for both H2
 * and PostgreSQL, separated by semicolons (none inside a string; {@code --} comments are dropped first).
 * <p>
 * The drivers are called directly, not through {@code DriverManager}, which doesn't see drivers in a mod's jar.
 */
public final class AlmuraDatabase {

    /**
     * Work on the database's connection.
     */
    @FunctionalInterface
    public interface Work<T> {

        T run(Connection connection) throws SQLException;
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "Almura Database");
        thread.setDaemon(true);
        return thread;
    });

    // Only touched on the database's thread
    private static @Nullable Connection connection;

    private AlmuraDatabase() {
    }

    /**
     * Runs work on the database's thread.
     */
    public static <T> CompletableFuture<T> submit(final Work<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return work.run(connection());
            } catch (final SQLException e) {
                throw new CompletionException(e);
            }
        }, EXECUTOR);
    }

    /**
     * Runs work on the database's thread, then {@code then} with its result on the server's thread. A failure is logged as not being able to
     * do {@code what}, and {@code then} doesn't run.
     */
    public static <T> void run(final String what, final Work<T> work, final Consumer<T> then) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        submit(work).whenComplete((result, error) -> {
            if (error != null) {
                Almura.LOGGER.error("Almura's database couldn't {}", what, error instanceof CompletionException ? error.getCause() : error);
                return;
            }
            if (server != null) {
                server.execute(() -> then.accept(result));
            }
        });
    }

    /**
     * Runs a change on the database's thread; a failure is logged as not being able to do {@code what}.
     */
    public static void change(final String what, final Work<?> work) {
        run(what, work, result -> { });
    }

    /**
     * Makes a feature's tables, if they aren't there yet.
     */
    public static CompletableFuture<Void> install(final String feature) {
        return submit(connection -> {
            final String resource = "/almura/database/" + feature + ".sql";
            final String script;
            try (InputStream in = AlmuraDatabase.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new SQLException("No " + resource + " in Almura's jar");
                }
                script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new SQLException("Couldn't read " + resource, e);
            }
            // Comments dropped first, so a semicolon in one doesn't split a statement
            final String statements = script.replaceAll("(?m)--.*$", "");
            try (Statement statement = connection.createStatement()) {
                for (final String sql : statements.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
            }
            return null;
        });
    }

    /**
     * Closes the connection, once what was asked before has run (the server stopping).
     */
    public static void close() {
        EXECUTOR.execute(() -> {
            if (connection != null) {
                try {
                    connection.close();
                } catch (final SQLException e) {
                    Almura.LOGGER.warn("Couldn't close Almura's database", e);
                }
                connection = null;
            }
        });
    }

    private static Connection connection() throws SQLException {
        if (connection != null && connection.isValid(2)) {
            return connection;
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException ignored) {
                // Already gone
            }
        }
        connection = open();
        return connection;
    }

    private static Connection open() throws SQLException {
        final Dialect dialect = AlmuraServerConfig.DIALECT.get();
        final Properties properties = new Properties();
        properties.setProperty("user", AlmuraServerConfig.USERNAME.get());
        properties.setProperty("password", AlmuraServerConfig.PASSWORD.get());
        final Connection opened;
        final String where;
        if (dialect == Dialect.POSTGRES) {
            where = "postgresql://" + AlmuraServerConfig.HOST.get() + ":" + AlmuraServerConfig.PORT.get() + "/" + AlmuraServerConfig.DATABASE.get();
            opened = new org.postgresql.Driver().connect("jdbc:" + where, properties);
        } else {
            final Path file = Path.of(AlmuraServerConfig.PATH.get()).toAbsolutePath().normalize();
            try {
                Files.createDirectories(file.getParent());
            } catch (final IOException e) {
                throw new SQLException("Couldn't make the folder for " + file, e);
            }
            where = file.toString();
            opened = new Driver().connect("jdbc:h2:file:" + where, properties);
        }
        if (opened == null) {
            throw new SQLException("The " + dialect + " driver didn't take " + where);
        }
        Almura.LOGGER.info("Opened Almura's {} database at {}", dialect, where);
        return opened;
    }
}
