/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractParsedBlockBuilder<C extends ContentBlock, D extends BlockStateDefinition, B extends BlockStateDefinition.Builder<D>> extends AbstractBlockBuilder<C, D, B> {
    public AbstractParsedBlockBuilder(final BlockGenre genre) {
        super(genre);
    }

    protected abstract Pattern pattern();

    protected abstract String describePattern();

    @Override
    protected B createDefinitionBuilder(final String id) {
        final Matcher matcher = this.pattern().matcher(id);
        if (!matcher.matches()) {
            throw new IllegalStateException(String.format("Definition does not follow state naming conventions (%s): %s", this.describePattern(), id));
        }
        return this.createDefinitionBuilder(id, matcher);
    }

    protected abstract B createDefinitionBuilder(final String id, final Matcher matcher);
}
