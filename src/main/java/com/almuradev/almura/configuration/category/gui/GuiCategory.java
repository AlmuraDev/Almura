/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.gui;

import com.almuradev.almura.configuration.category.gui.about.AboutCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public final class GuiCategory {
    @Setting public final AboutCategory about = new AboutCategory();
}
