/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud.screen.origin.component.panel;

import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.Fonts;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.renderer.icon.provider.GuiIconProvider;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

@SideOnly(Side.CLIENT)
@SuppressWarnings("all")
public abstract class AbstractPanel extends UIPanel {

    protected final Minecraft client = Minecraft.getMinecraft();

    protected AbstractPanel(final MalisisGui gui, final int width, final int height) {
        super(gui, width, height);
        this.iconProvider = new GuiIconProvider(GuiConfig.Icon.VANILLA_CONTAINER_INVENTORY_ADVANCEMENT);
    }

    protected void drawProperty(final String key, final String value, final int x, final int y) {
        final Text text = Text.of(TextColors.WHITE, key, ": ", TextColors.GRAY, value);
        this.drawText(text, x, y);
    }

    protected void drawText(final Text text, final int x, final int y) {
        this.renderer.drawText(Fonts.MINECRAFT, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), x, y, this.zIndex, FontColors.WHITE_FO);
    }
}
