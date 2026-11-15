/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.Almura;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * The accessories a player can wear, as 3.5's: one of each at a time, each in the variants of the catalog
 * ({@code assets/almura/accessories/catalog.json}), its textures in the content ({@code textures/accessory/<id>/<variant>.png}).
 */
public enum AccessoryType {
    BRACELET("bracelet", "Bracelet", 0F),
    CAT_HAT("cathat", "Cat Hat", 10.6F),
    EARS("ears", "Ears", 6F),
    HALO("halo", "Halo", 2.5F),
    NOTCH_HAT("notchhat", "Notch Hat", 4.6F),
    SUNGLASSES("sunglasses", "Sunglasses", 0F),
    TAIL("tail", "Tail", 0F),
    TOP_HAT("tophat", "Top Hat", 8.6F),
    WINGS("wings", "Wings", 0F);

    private final String id;
    private final String displayName;
    private final float heightAboveHead;

    AccessoryType(final String id, final String displayName, final float heightAboveHead) {
        this.id = id;
        this.displayName = displayName;
        this.heightAboveHead = heightAboveHead;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    /**
     * @return how far the accessory reaches above the top of the head, in model pixels (the name, title and health bar are raised by it)
     */
    public float heightAboveHead() {
        return this.heightAboveHead;
    }

    /**
     * @return {@code almura.accessory.<id>}, as 3.5's
     */
    public String permissionPath() {
        return "accessory." + this.id;
    }

    /**
     * @return the texture of a variant: {@code almura:textures/accessory/<id>/<variant>.png}, in the content
     */
    public Identifier texture(final String variant) {
        return Identifier.fromNamespaceAndPath(Almura.ID, "textures/accessory/" + this.id + "/" + variant + ".png");
    }

    public static @Nullable AccessoryType byId(final String id) {
        for (final AccessoryType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
