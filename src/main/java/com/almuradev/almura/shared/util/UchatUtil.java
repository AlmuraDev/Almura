package com.almuradev.almura.shared.util;

//import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;

public final class UchatUtil {
    public static void relayMessageToDiscord(String message) {
        if (Sponge.getPluginManager().isLoaded("uchat")) {
            //UChat.get().getUCJDA().sendTellToDiscord(message);
        }
    }
}


