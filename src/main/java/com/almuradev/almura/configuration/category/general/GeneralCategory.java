/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.general;

import com.almuradev.almura.configuration.AbstractConfigurationCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.LinkedList;
import java.util.List;

@ConfigSerializable
public class GeneralCategory extends AbstractConfigurationCategory {

    @Setting(value = "creative-tab") public final CreativeTabCategory creativeTab = new CreativeTabCategory();
    @Setting public String title = "";
    @Setting public List<String> tooltip = new LinkedList<>();
    @Setting public float hardness = 1f;
    @Setting public float resistance = 1;

}
