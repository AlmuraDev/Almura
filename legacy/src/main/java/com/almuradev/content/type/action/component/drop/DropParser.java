/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action.component.drop;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;

public interface DropParser {

    List<? extends Drop> parse(final ConfigurationNode config);
}
