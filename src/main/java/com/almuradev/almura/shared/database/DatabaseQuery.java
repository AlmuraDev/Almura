/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.database;

import org.jooq.DSLContext;
import org.jooq.Query;

@FunctionalInterface
public interface DatabaseQuery<Q extends Query> {

  Q build(DSLContext context);
}
