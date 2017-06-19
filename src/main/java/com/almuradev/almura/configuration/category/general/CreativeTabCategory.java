/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.category.general;

import com.almuradev.almura.configuration.AbstractConfigurationCategory;
import com.almuradev.almura.content.item.group.ItemGroup;
import com.almuradev.almura.content.item.group.ItemGroups;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CreativeTabCategory extends AbstractConfigurationCategory {

    @Setting public boolean enabled = false;
    @Setting public ItemGroup tab = ItemGroups.FOOD;
}
