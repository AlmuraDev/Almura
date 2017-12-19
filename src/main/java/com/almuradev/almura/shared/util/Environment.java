/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.util;

/**
 * The active environment.
 */
public enum Environment {
    /**
     * The development environment.
     */
    DEVELOPMENT,
    /**
     * The production environment.
     */
    PRODUCTION;

    /**
     * The current environment, defauling to {@code PRODUCTION}.
     */
    public static Environment environment = Environment.PRODUCTION;

    /**
     * Gets the current environment.
     *
     * @return the environment
     */
    public static Environment get() {
        return environment;
    }
}
