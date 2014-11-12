/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.pack.ContentPack;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Map;

public class KeyListener {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if (Keybindings.REFRESH_SHAPES.isPressed()) {
            if (Configuration.IS_DEBUG) {
                Almura.LOGGER.info("Clearing all current shapes and doing a refresh. Please stand-by");
            }

            for (Map.Entry<String, ContentPack> entry : ContentPack.getPacks().entrySet()) {
                try {
                    entry.getValue().reloadIconsAndShape();
                } catch (Exception e) {
                    Almura.LOGGER.error("There was an issue refreshing SMPPack [" + entry.getKey() + "]", e);
                }
            }
        }
    }
}
