/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.registry;

import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public interface ContentBuilder<C extends CatalogedContent> {

    String string(final StringType type);

    void id(final String namespace, final String sap, final String id);

    C build();

    enum StringType {
        NAMESPACE,
        NAME,
        SEMI_ABSOLUTE_PATH,
        TRANSLATION
    }

    abstract class Impl<C extends CatalogedContent> implements ContentBuilder<C> {

        private String namespace;
        // The internal id is the path relative from the content type root and without the json file extension
        // Example:
        // Path        : content/block/normal/barrel/raspberry_seed_barrel.json
        // Internal Id : normal/barrel/raspberry_seed_barrel
        private String sap;
        // Example: almura:raspberry_seed_barrel
        protected String id;
        // Example: raspberry_seed_barrel
        protected String name;

        @Override
        public String string(final StringType type) {
            switch (type) {
                case NAMESPACE:
                    return this.namespace;
                case SEMI_ABSOLUTE_PATH:
                    return this.sap;
                case TRANSLATION:
                    return this.namespace + '.' + this.sap.replace('/', '.');
                case NAME:
                    return this.name;
                default:
                    return this.id;
            }
        }

        @Override
        public final void id(final String namespace, final String sap, final String id) {
            this.namespace = namespace;
            this.sap = sap;
            this.id = namespace + ':' + id;
            this.name = id;
        }

        @OverridingMethodsMustInvokeSuper
        public void fill(final IForgeRegistryEntry.Impl entry) {
            entry.setRegistryName(this.sap);
        }
    }
}
