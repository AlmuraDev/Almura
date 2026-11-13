/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.server.config.category;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.database.DatabaseConfiguration;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.jooq.SQLDialect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

@ConfigSerializable
public final class DatabaseCategory implements DatabaseConfiguration {

    @Setting(value = "dialect", comment = "The specific SQL language you want to use. Available options are [H2, POSTGRES].")
    private SQLDialect dialect = SQLDialect.H2;

    @Setting
    private String database = "dev";

    @Setting(value = "path", comment = "For flat-file database formats, such as H2, this specifies where the file is at. Must be a complete file path"
        + ". Can be left empty if using a database engine. For H2, do not specify file extension.")
    private String path = String.format("./config/%s/h2", Almura.ID);

    @Setting(value = "server", comment = "For hosted database formats, such as POSTGRES, this specifies the hostname where the database engine is "
        + "located. Can also be an IP address. Can be left empty if using a flat-file database.")
    private String server = "localhost";

    @Setting(value = "port", comment = "For hosted database formats, such as POSTGRES, this specifies the port that the database engine is hosted "
        + "on. Can be left empty if using a flat-file database.")
    private int port = 8082;

    @Setting(value = "username", comment = "For hosted database formats, such as POSTGRES, this specifies the username to use to connect to the "
        + "database engine. This username must have rights to the database specified. Can be left empty if using a flat-file database.")
    private String username = "sa";

    @Setting(value = "password", comment = "For hosted database formats, such as POSTGRES, this specifies the password to use to connect to the "
        + "database engine. Can be left empty if using a flat-file database.")
    private String password = "";

    @Nullable private String connectionStringNoSchema;
    @Nullable private String connectionString;

    @Override
    public SQLDialect getDialect() {
        return this.dialect;
    }

    @Override
    public String getDatabase() {
        return this.database;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getServer() {
        return this.server;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getConnectionString() {
        if (this.connectionString == null) {
            this.buildConnectionString();
        }

        return this.connectionString;
    }

    @Override
    public String getConnectionStringWithoutSchema() {
        if (this.connectionStringNoSchema == null) {
            this.buildConnectionString();
        }

        return this.connectionStringNoSchema;
    }

    private void buildConnectionString() {
        Path actualPath;

        if (this.dialect == SQLDialect.H2 || dialect == SQLDialect.POSTGRES) {

            if (this.dialect == SQLDialect.H2) {
                actualPath = Paths.get(this.path);

                Path fsPath = actualPath;

                if (Files.notExists(fsPath)) {
                    fsPath = Paths.get(fsPath.toString() + ".mv.db");
                }

                if (Files.notExists(fsPath)) {
                    try {
                        Files.createFile(fsPath);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create H2 database file! Path: " + fsPath);
                    }
                }
                this.connectionStringNoSchema = String.format("jdbc:%s:%s;%s;%s", this.dialect.getName().toLowerCase(), actualPath.toAbsolutePath(),
                    "DATABASE_TO_UPPER=FALSE", "AUTO_SERVER=TRUE");

                this.connectionString =
                    String.format("jdbc:%s:%s;%s;%s;%s", this.dialect.getName().toLowerCase(), actualPath.toAbsolutePath(), "SCHEMA=" +
                        this.database, "DATABASE_TO_UPPER=FALSE", "AUTO_SERVER=TRUE");
            } else {
                this.connectionStringNoSchema = String.format("jdbc:%s://%s:%d", this.dialect.getName().toLowerCase(), this.server, this.port);
                this.connectionString =
                    String.format("jdbc:%s://%s:%d/%s", this.dialect.getName().toLowerCase(), this.server, this.port, this.database);
            }
        } else {
            throw new UnsupportedOperationException("Only H2 or POSTGRES are currently supported!");
        }
    }
}
