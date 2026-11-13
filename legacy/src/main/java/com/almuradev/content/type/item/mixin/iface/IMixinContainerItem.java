/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.mixin.iface;

import com.almuradev.content.type.item.ItemContainer;

import javax.annotation.Nullable;

public interface IMixinContainerItem {

    void container(@Nullable ItemContainer container);
}
