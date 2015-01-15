/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

public class DuplicateRecipeException extends Exception {

    static final long serialVersionUID = -1L;

    public DuplicateRecipeException(String message) {
        super(message);
    }

    public DuplicateRecipeException(String message, Throwable t) {
        super(message, t);
    }
}
