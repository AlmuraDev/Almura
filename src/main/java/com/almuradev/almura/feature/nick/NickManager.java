package com.almuradev.almura.feature.nick;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.shared.event.Witness;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickManager implements Witness {

    public static NickManager instance = new NickManager();

    private static Map<UUID, Text> nicksById = new HashMap<>();

    public void putAll(Map<UUID, Text> nicksById) {
        checkNotNull(nicksById);

        NickManager.nicksById.clear();

        NickManager.nicksById.putAll(nicksById);
    }

    public void put(UUID uniqueId, Text nick) {
        checkNotNull(uniqueId);
        checkNotNull(nick);

        nicksById.put(uniqueId, nick);
    }

    @SubscribeEvent
    public void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        final UUID uniqueId = player.getUniqueID();

        final Text nick = nicksById.get(uniqueId);

        if (nick != null) {
            event.setDisplayname(TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick));
        }
    }

}
