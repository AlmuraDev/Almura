/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.configuration.type;

import com.almuradev.almura.configuration.AbstractConfiguration;
import com.almuradev.almura.configuration.category.general.GeneralCategory;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class BlockConfiguration extends AbstractConfiguration {

    @Setting public final GeneralCategory general = new GeneralCategory();
}
