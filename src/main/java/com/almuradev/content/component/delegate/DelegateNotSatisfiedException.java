/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.component.delegate;

public class DelegateNotSatisfiedException extends RuntimeException {
    public DelegateNotSatisfiedException() {
    }

    public DelegateNotSatisfiedException(String message) {
        super(message);
    }
}
