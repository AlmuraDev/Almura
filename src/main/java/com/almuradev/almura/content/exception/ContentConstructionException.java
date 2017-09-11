/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
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
