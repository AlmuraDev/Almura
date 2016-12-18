/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.general;

import com.almuradev.almura.api.creativetab.CreativeTab;
import com.almuradev.almura.api.creativetab.CreativeTabs;
import com.almuradev.almura.configuration.AbstractConfigurationCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CreativeTabCategory extends AbstractConfigurationCategory {

    @Setting public boolean enabled = false;
    @Setting public CreativeTab tab = CreativeTabs.FOOD;
}
