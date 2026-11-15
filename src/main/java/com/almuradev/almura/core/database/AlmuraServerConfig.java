/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.database;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The server's settings, {@code config/almura/server.toml}: for now 3.5's {@code server.conf} database block, which Almura's database
 * ({@link AlmuraDatabase}) is opened with.
 */
public final class AlmuraServerConfig {

    /**
     * The SQL database: H2 (a file, the default) or PostgreSQL (a server), as 3.5's.
     */
    public enum Dialect {
        H2,
        POSTGRES
    }

    public static final String FILE = "almura/server.toml";

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<Dialect> DIALECT;
    public static final ModConfigSpec.ConfigValue<String> PATH;
    public static final ModConfigSpec.ConfigValue<String> HOST;
    public static final ModConfigSpec.IntValue PORT;
    public static final ModConfigSpec.ConfigValue<String> DATABASE;
    public static final ModConfigSpec.ConfigValue<String> USERNAME;
    public static final ModConfigSpec.ConfigValue<String> PASSWORD;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("database");
        DIALECT = builder.comment("The SQL language to use: H2 (a file) or POSTGRES (a database server)").defineEnum("dialect", Dialect.H2);
        PATH = builder.comment("For H2: the database file, without its extension; relative to the server's folder")
            .define("path", "./config/almura/database/almura");
        HOST = builder.comment("For POSTGRES: the host name or address of the database server").define("host", "localhost");
        PORT = builder.comment("For POSTGRES: the port the database server listens on").defineInRange("port", 5432, 1, 65535);
        DATABASE = builder.comment("For POSTGRES: the database to use").define("database", "almura");
        USERNAME = builder.comment("The user to connect as (H2 too: its default is sa)").define("username", "sa");
        PASSWORD = builder.comment("The user's password (empty for none)").define("password", "");
        builder.pop();
        SPEC = builder.build();
    }

    private AlmuraServerConfig() {
    }
}
