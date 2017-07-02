/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.interfaces;

import org.spongepowered.api.text.Text;

public interface IMixinEntityPlayer {

    /**
     * Gets the prefix of the player's name.
     * @return The prefix.
     */
    Text getPrefix();

    /**
     * Sets the prefix of the player's name.
     * @param prefix The prefix.
     */
    void setPrefix(Text prefix);

    /**
     * Gets the suffix of the player's name.
     * @return The suffix.
     */
    Text getSuffix();

    /**
     * Sets the suffix of the player's name.
     * @param suffix The suffix.
     */
    void setSuffix(Text suffix);
}
