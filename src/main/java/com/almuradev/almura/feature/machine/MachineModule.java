/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.feature.machine.processing.ProcessingBlock;
import com.almuradev.almura.feature.machine.processing.client.MachinePartsClient;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class MachineModule extends AbstractModule implements CommonBinder {

    // Purpose: furnace-like machines, powered by IndustrialCraft 2 EU when it's installed.

    @Override
    protected void configure() {
        this.facet().add(MachineFeature.class);
        this.requestStaticInjection(ProcessingBlock.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SideOnly(Side.CLIENT)
                protected void configure() {
                    // The machines' spinning parts
                    this.facet().add(MachinePartsClient.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
