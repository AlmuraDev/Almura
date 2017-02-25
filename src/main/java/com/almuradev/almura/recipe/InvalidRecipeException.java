/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

public class InvalidRecipeException extends Exception {

    static final long serialVersionUID = -1L;

    public InvalidRecipeException(RuntimeException ex) {
        super(ex.getMessage(), ex.getCause());
    }

    public InvalidRecipeException(String message) {
        super(message);
    }

    public InvalidRecipeException(String message, Throwable t) {
        super(message, t);
    }
}
