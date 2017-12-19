/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content;

import com.almuradev.content.loader.ContentLoader;
import com.almuradev.content.registry.CatalogedContent;
import com.almuradev.content.registry.ContentBuilder;
import com.almuradev.content.type.action.ActionContentTypeLoader;
import com.almuradev.content.type.action.ActionGenre;
import com.almuradev.content.type.block.BlockContentTypeLoader;
import com.almuradev.content.type.block.BlockGenre;
import com.almuradev.content.type.blocksoundgroup.BlockSoundGroupContentTypeLoader;
import com.almuradev.content.type.item.ItemContentTypeLoader;
import com.almuradev.content.type.item.ItemGenre;
import com.almuradev.content.type.itemgroup.ItemGroupContentTypeLoader;
import com.almuradev.content.type.material.MaterialContentTypeLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An enumeration of content types.
 */
public enum ContentType {
    /**
     * A content type representing an action.
     *
     * @see ActionGenre
     */
    ACTION("action", ActionContentTypeLoader.class),
    /**
     * A content type representing a block.
     *
     * @see BlockGenre
     */
    BLOCK("block", BlockContentTypeLoader.class),
    /**
     * A content type representing a block sound group.
     */
    BLOCK_SOUND_GROUP("block_sound_group", BlockSoundGroupContentTypeLoader.class),
    /**
     * A content type representing an item.
     *
     * @see ItemGenre
     */
    ITEM("item", ItemContentTypeLoader.class),
    /**
     * A content type representing an item group.
     */
    ITEM_GROUP("item_group", ItemGroupContentTypeLoader.class),
    /**
     * A content type representing a material.
     */
    MATERIAL("material", MaterialContentTypeLoader.class);

    /**
     * The id of this content type.
     *
     * <p>The id is used for identification and loading.</p>
     */
    public final String id;
    /**
     * The loader for this content type.
     */
    public final Class<? extends ContentLoader> loader;

    ContentType(final String id, final Class<? extends ContentLoader> loader) {
        this.id = id;
        this.loader = loader;
    }

    /**
     * A marker interface for a content type with multiple children types.
     */
    public interface MultiType<C extends CatalogedContent, B extends ContentBuilder<C>> {

        /**
         * Gets the id of this type.
         *
         * @return the id
         */
        String id();

        /**
         * Gets the builder class of this type.
         *
         * @return the builder class
         */
        Class<B> builder();

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface Name {

            String value();
        }
    }
}
