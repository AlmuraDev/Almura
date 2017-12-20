package com.almuradev.almura.feature.title.network;

import com.almuradev.almura.feature.title.TitleManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import org.spongepowered.api.Platform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.MessageHandler;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;

import java.util.Set;

import javax.inject.Inject;

public final class ServerboundPlayerTitlesRequestPacketHandler implements MessageHandler<ServerboundPlayerTitlesRequestPacket> {

    private final TitleManager manager;
    private final ChannelBinding.IndexedMessageChannel network;

    @Inject
    public ServerboundPlayerTitlesRequestPacketHandler(final TitleManager manager, @ChannelId(NetworkConfig.CHANNEL)
            ChannelBinding.IndexedMessageChannel network) {
        this.manager = manager;
        this.network = network;
    }

    @Override
    public void handleMessage(ServerboundPlayerTitlesRequestPacket message, RemoteConnection connection, Platform.Type side) {
        if (side.isServer()) {
            if (connection instanceof PlayerConnection) {
                final Player player = ((PlayerConnection) connection).getPlayer();
                final Set<Text> titles = this.manager.getTitlesFor(player);

                if (titles.isEmpty()) {
                    // TODO Tell the client they have no titles :'(
                } else {
                    this.network.sendTo(player, new ClientboundPlayerTitlesResponsePacket(titles));
                }
            }
        }
    }
}
