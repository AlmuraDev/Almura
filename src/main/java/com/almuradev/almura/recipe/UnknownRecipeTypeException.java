/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.recipe;

public class UnknownRecipeTypeException extends Exception {

    static final long serialVersionUID = -1L;

    public UnknownRecipeTypeException(String message) {
        super(message);
    }

    public UnknownRecipeTypeException(String message, Throwable t) {
        super(message, t);
    }
}
