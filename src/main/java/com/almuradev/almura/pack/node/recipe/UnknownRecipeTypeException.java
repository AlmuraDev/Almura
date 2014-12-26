package com.almuradev.almura.pack.node.recipe;

public class UnknownRecipeTypeException extends Exception {
    static final long serialVersionUID = -1L;

    public UnknownRecipeTypeException(String message) {
        super(message);
    }

    public UnknownRecipeTypeException(String message, Throwable t) {
        super(message, t);
    }
}
