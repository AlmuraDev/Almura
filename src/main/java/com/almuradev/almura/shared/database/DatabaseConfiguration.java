/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import org.jooq.SQLDialect;

public interface DatabaseConfiguration {

    SQLDialect getDialect();

    String getDatabase();

    String getPath();

    String getServer();

    int getPort();

    String getUsername();

    String getPassword();

    String getConnectionString();

    String getConnectionStringWithoutSchema();
}
