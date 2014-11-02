package com.almuradev.almura.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.smp.SMPPack;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

import java.util.Map;

public class KeyListener {
    public KeyListener() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onKeyEvent(InputEvent.KeyInputEvent event) {
        if (Keybindings.REFRESH_SHAPES.isPressed()) {
            for (Map.Entry<String, SMPPack> entry : SMPPack.getPacks().entrySet()) {
                try {
                    entry.getValue().refresh();
                } catch (Exception e) {
                    Almura.LOGGER.error("There was an issue refreshing SMPPack [" + entry.getKey() + "]", e);
                }
            }
        }
    }
}
