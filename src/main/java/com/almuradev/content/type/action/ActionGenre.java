/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.action;

import com.almuradev.content.type.ContentType;
import com.almuradev.content.type.action.type.blockdestroy.BlockDestroyAction;

/**
 * An enumeration of action types.
 */
@ContentType.MultiType.Name("actions")
public enum ActionGenre implements ContentType.MultiType<ActionContentType, ActionContentType.Builder<ActionContentType>> {
    /**
     * An action representing block destruction.
     */
    BLOCK_DESTROY("block_destroy", BlockDestroyAction.Builder.class);

    /**
     * The id of this action type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    private final String id;
    private final Class<? extends ActionContentType.Builder<? extends ActionContentType>> builder;

    ActionGenre(final String id, final Class<? extends ActionContentType.Builder<? extends ActionContentType>> builder) {
        this.id = id;
        this.builder = builder;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ActionContentType.Builder<ActionContentType>> builder() {
        return (Class<ActionContentType.Builder<ActionContentType>>) this.builder;
    }
}
