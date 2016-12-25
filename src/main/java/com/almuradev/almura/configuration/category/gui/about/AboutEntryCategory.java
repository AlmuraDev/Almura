/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.gui.about;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.text.format.TextColor;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public final class AboutEntryCategory {

    @Setting(value = "color")
    public TextColor color;

    @Setting(value = "name")
    public String name;

    @Setting(value = "description")
    public String description;

    @Setting(value = "uuid")
    public String uniqueId;

    @Setting(value = "titles")
    public List<String> titles;

    public AboutEntryCategory() {
    }

    public AboutEntryCategory(TextColor color, String name, String description, String uniqueId, String... titles) {
        this.name = name;
        this.description = description;
        this.uniqueId = uniqueId;
        this.titles = Arrays.asList(titles);
        this.color = color;
    }
}
