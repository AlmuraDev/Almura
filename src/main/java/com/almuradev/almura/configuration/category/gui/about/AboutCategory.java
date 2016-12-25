/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.gui.about;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;

@ConfigSerializable
public final class AboutCategory {

    @Setting(value = "entries", comment = "Entries to show on the About screen")
    public List<AboutEntryCategory> entries = new ArrayList<>();
}
