/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.ui.component;

import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * A multiple line label sized to its text (lines split on new lines, never wrapped).
 */
@SideOnly(Side.CLIENT)
public class UIExpandingLabel extends UILabel {

    @SuppressWarnings("deprecation")
    public UIExpandingLabel(Text text) {
        this(TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
    }

    public UIExpandingLabel(String text) {
        // A UILabel sizes itself to its content until given a size
        super(text, true);
    }

    /**
     * Gets the width of the widest line.
     *
     * @return the width
     */
    public int getContentWidth() {
        return this.contentSize().width();
    }
}
