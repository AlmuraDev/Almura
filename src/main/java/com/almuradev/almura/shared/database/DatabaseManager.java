/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import com.google.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nullable;
import javax.sql.DataSource;

@Singleton
@WitnessScope.Sponge
public final class DatabaseManager implements Witness {

  private final PluginContainer container;
  private final DatabaseConfiguration configuration;
  private final ServiceManager manager;

  @Nullable private DataSource dataSource;
  @Nullable private DataSource connectionSource;

  DatabaseManager(final PluginContainer container, final ServiceManager manager, final DatabaseConfiguration configuration) {
    this.container = container;
    this.manager = manager;
    this.configuration = configuration;
  }

  public DataSource getOrCreateDataSource(final boolean includeSchema) throws SQLException {
    boolean existingDataSource = includeSchema ? this.dataSource != null : this.connectionSource != null;

    if (existingDataSource) {
      Connection connection = null;

      try {
        if (includeSchema) {
          connection = this.dataSource.getConnection();
        } else {
          connection = this.connectionSource.getConnection();
        }
      } catch (SQLException ignored) {
        existingDataSource = false;
      } finally {
        if (connection != null) {
          connection.close();
        }
      }
    }

    if (!existingDataSource) {
      if (includeSchema) {
        this.dataSource = this.manager.provideUnchecked(SqlService.class).getDataSource(this.container, this.configuration.getConnectionString());
      } else {
        this.connectionSource = this.manager.provideUnchecked(SqlService.class).getDataSource(this.container, this.configuration.getConnectionStringWithoutSchema());
      }
    }

    return includeSchema ? this.dataSource : this.connectionSource;
  }

  public DSLContext createContext(final boolean includeSchema) throws SQLException {
      final DataSource dataSource = this.getOrCreateDataSource(includeSchema);

      return DSL.using(dataSource, this.configuration.getDialect(), new Settings().withRenderSchema(false).withRenderCatalog(false));
  }

  public DatabaseConfiguration getConfiguration() {
    return this.configuration;
  }
}
