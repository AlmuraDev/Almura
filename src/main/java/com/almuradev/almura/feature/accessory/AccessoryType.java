/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.Almura;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * The kinds of accessory a player can wear, one of each at a time. Wearing a kind needs the {@link #permission()}.
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

    /**
     * @return How far the accessory reaches above the top of the head, in model pixels (the name, title and health bar are raised by it)
     */
    public float heightAboveHead() {
        return this.heightAboveHead;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    /**
     * @return almura.accessory.&lt;id&gt;
     */
    public String permission() {
        return "almura.accessory." + this.id;
    }

    /**
     * @return The texture of a variant: assets/almura/textures/accessory/&lt;id&gt;/&lt;variant&gt;.png (content folders)
     */
    public ResourceLocation texture(final String variant) {
        return new ResourceLocation(Almura.ID, "textures/accessory/" + this.id + "/" + variant + ".png");
    }

    @Nullable
    public static AccessoryType byId(final String id) {
        for (final AccessoryType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
