/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.type.Accessory;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogType;

public interface AccessoryType extends CatalogType {

    Class<? extends Accessory> getAccessoryClass();

    ResourceLocation[] getTextureLayers();
}
