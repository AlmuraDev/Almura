/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.function.Consumer;

public final class ConfigurationNodes {

    private ConfigurationNodes() {
    }

    /**
     * Provide a consumer with the {@link ConfigurationNode#getString() string value} of
     * a configuration node if it is not {@link ConfigurationNode#isVirtual() virtual}.
     *
     * @param node the node
     * @param consumer the consumer
     */
    public static void whenRealString(final ConfigurationNode node, final Consumer<String> consumer) {
        if (!node.isVirtual()) {
            consumer.accept(node.getString());
        }
    }
}
