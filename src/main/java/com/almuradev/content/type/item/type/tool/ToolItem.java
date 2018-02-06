/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.tool;

import com.almuradev.content.component.delegate.Delegate;
import com.almuradev.content.component.delegate.DelegateSet;
import com.almuradev.content.type.item.ContentItemType;
import net.minecraft.block.Block;
import org.spongepowered.api.block.BlockType;

import java.util.Objects;

import javax.annotation.Nullable;

public interface ToolItem extends ContentItemType {
    interface Builder<T extends ToolItem> extends ContentItemType.Builder<T> {
        void tier(final Delegate<Tier> tier);

        void effectiveOn(final DelegateSet<BlockType, Block> effectiveOn);

        abstract class Impl<T extends ToolItem> extends ContentItemType.Builder.Impl<T> implements Builder<T> {
            public Delegate<ToolItem.Tier> tier;
            @Nullable public DelegateSet<BlockType, Block> effectiveOn;

            @Override
            public void tier(final Delegate<ToolItem.Tier> tier) {
                this.tier = tier;
            }

            @Override
            public void effectiveOn(final DelegateSet<BlockType, Block> effectiveOn) {
                this.effectiveOn = effectiveOn;
            }

            public DelegateSet<BlockType, Block> effectiveOn() {
                return Objects.requireNonNull(this.effectiveOn, "effective on");
            }
        }
    }
}
