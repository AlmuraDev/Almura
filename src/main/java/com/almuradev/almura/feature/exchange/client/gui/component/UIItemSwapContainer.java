/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.feature.exchange.MockOffer;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.container.UISwapContainer;
import net.malisis.core.client.gui.MalisisGui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;

import java.util.function.BiFunction;

@SideOnly(Side.CLIENT)
public class UIItemSwapContainer extends UISwapContainer<MockOffer> {

    private UIItemSwapContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, MockOffer, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        return super.createMiddleContainer(gui);
    }
}
