/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.type.item.ContentItemType;

public interface ToolItem extends ContentItemType {
    interface Builder<T extends ToolItem> extends ContentItemType.Builder<T> {
        void tier(final Delegate<Tier> tier);

        abstract class Impl<T extends ToolItem> extends ContentItemType.Builder.Impl<T> implements Builder<T> {
            public Delegate<ToolItem.Tier> tier;

            @Override
            public void tier(final Delegate<ToolItem.Tier> tier) {
                this.tier = tier;
            }
        }
    }
}
