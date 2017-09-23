/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.apply;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;

@FunctionalInterface
public interface ApplyParser {

    List<Apply> parse(final ConfigurationNode config);
}
