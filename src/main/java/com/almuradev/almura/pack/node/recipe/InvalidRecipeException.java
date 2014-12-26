package com.almuradev.almura.pack.node.recipe;

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
