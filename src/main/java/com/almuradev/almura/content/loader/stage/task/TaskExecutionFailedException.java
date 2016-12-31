/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

public class TaskExecutionFailedException extends Exception {

    public TaskExecutionFailedException(String message) {
        super(message);
    }

    public TaskExecutionFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
