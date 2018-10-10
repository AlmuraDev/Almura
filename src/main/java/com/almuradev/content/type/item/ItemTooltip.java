/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ItemTooltip {
    void render(final List<String> target);

    class Impl implements ItemTooltip {
        private final Item item;
        @Nullable private String translation;
        private boolean empty;

        public Impl(final Item item) {
            this.item = item;
        }

        @Override
        public void render(final List<String> target) {
            if (this.empty) {
                return;
            }
            if (this.translation == null) {
                this.translation = this.item.getTranslationKey() + ".tooltip";
                this.empty = !I18n.hasKey(this.translation);
                if (this.empty) {
                    return;
                }
            }
            final String string = I18n.format(this.translation);
            Collections.addAll(target, string.split("\n"));
        }
    }
}
