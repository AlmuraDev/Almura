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
    BRACELET("bracelet", "Bracelet"),
    CAT_HAT("cathat", "Cat Hat"),
    EARS("ears", "Ears"),
    HALO("halo", "Halo"),
    NOTCH_HAT("notchhat", "Notch Hat"),
    SUNGLASSES("sunglasses", "Sunglasses"),
    TAIL("tail", "Tail"),
    TOP_HAT("tophat", "Top Hat"),
    WINGS("wings", "Wings");

    private final String id;
    private final String displayName;

    AccessoryType(final String id, final String displayName) {
        this.id = id;
        this.displayName = displayName;
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
