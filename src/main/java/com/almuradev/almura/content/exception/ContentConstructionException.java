/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.exception;

public class ContentConstructionException extends RuntimeException {

    public ContentConstructionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    // We don't care about the stacktrace of this exception - we wrap the real cause.
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
