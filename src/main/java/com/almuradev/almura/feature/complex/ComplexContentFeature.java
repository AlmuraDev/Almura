/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex;

import com.almuradev.almura.feature.complex.item.wand.LightRepairWand;
import com.almuradev.almura.shared.event.Witness;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class ComplexContentFeature implements Witness {

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new LightRepairWand());
        //event.getRegistry().register(new FarmersAlmanacItem()); // Todo: Hook up here.
    }
}
