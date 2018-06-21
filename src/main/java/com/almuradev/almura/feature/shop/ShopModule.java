/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.shop;

import com.almuradev.almura.feature.shop.gui.ModifyItemsGUI;
import com.almuradev.almura.feature.shop.gui.ShopGUI;
import com.almuradev.almura.feature.shop.gui.ShopListGUI;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;

public final class ShopModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.requestStaticInjection(ShopGUI.class);
                    this.requestStaticInjection(ShopListGUI.class);
                    this.requestStaticInjection(ModifyItemsGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
