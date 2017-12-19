package com.almuradev.almura.feature.nick;

import static com.google.common.base.Preconditions.checkNotNull;

<<<<<<< HEAD
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameChangeMappingPacket;
import com.almuradev.almura.feature.nick.network.ClientboundNucleusNameMappingsPacket;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.text.SpongeTexts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public final class NickManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final ChannelBinding.IndexedMessageChannel network;

    // Client-only
    private final Map<UUID, Text> clientNicks = new HashMap<>();

    @Inject
    private NickManager(final Game game, @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.network = network;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    // Fires only on client
    @SubscribeEvent
    public void onClientConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.clientNicks.clear();
    }

    // Fires only on server
    @Listener
    public void onClientConnectionEventJoin(ClientConnectionEvent.Join event) {
        Sponge.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {
            service.getNickname(event.getTargetEntity()).ifPresent((nick) -> {
                // Trigger Forge event so mod's know of the nickname
                ((EntityPlayerMP) event.getTargetEntity()).refreshDisplayName();
            });

            Task.builder().async().execute(t -> {
                // Send everyone's nicknames to the joining player
                this.network.sendTo(event.getTargetEntity(), getMappingMessage(service, event.getTargetEntity()));
            }).submit(Almura.instance.container);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        final EntityPlayer player = event.getEntityPlayer();

        if (Sponge.getPlatform().getExecutionType().isServer()) {
            Sponge.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> service.getNickname((User) (Object) player)
                    .ifPresent((nick) -> event.setDisplayname(SpongeTexts.toLegacy(nick))));
        } else {
            // Set the client nick for the event based on what the server synchronized
            final Text nick = this.clientNicks.get(player.getUniqueID());
            if (nick != null) {
                event.setDisplayname(TextSerializers.LEGACY_FORMATTING_CODE.serialize(nick));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerNameFormatPost(PlayerEvent.NameFormat event) {

        final EntityPlayer player = event.getEntityPlayer();

        if (this.game.getPlatform().getExecutionType().isServer()) {
            Sponge.getServiceManager().provide(NucleusNicknameService.class).ifPresent((service) -> {
                final Text oldNick = service.getNickname((Player) event.getEntityPlayer()).orElse(Text.of(player.getName()));
                final Text newNick = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(event.getDisplayname());

                // Update Nucleus
                if (!oldNick.equals(newNick)) {
                    try {
                        service.setNickname((User) player, newNick);
                    } catch (NicknameException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Schedule client update
                    Task.builder().async().delayTicks(20).execute(t -> this.network.sendToAll(getMappingMessage((Player) player, newNick)))
                            .submit(Almura.instance.container);
                }
            });
        } else {

            this.updateClientInformation(player.getUniqueID(), Text.of(event.getDisplayname()));
        }
    }

    // Fires only on server
    @Listener(order = Order.POST)
    public void onPlayerChangeNickname(NucleusChangeNicknameEvent event, @Getter("getTargetUser") Player player) {
        final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
        final ITextComponent mcOldText = mcPlayer.getDisplayName();
        final ITextComponent mcNewText = SpongeTexts.toComponent(event.getNewNickname());

        if (!mcOldText.equals(mcNewText)) {
            Task.builder().delayTicks(1).execute(((EntityPlayerMP) player)::refreshDisplayName).submit(Almura.instance.container);
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateClientInformation(UUID uniqueId, Text nick) {
        // Update Player List
        for (final NetworkPlayerInfo networkPlayerInfo : Minecraft.getMinecraft().player.connection.getPlayerInfoMap()) {
            if (networkPlayerInfo.getGameProfile().getId().equals(uniqueId)) {
                networkPlayerInfo.setDisplayName(SpongeTexts.toComponent(nick));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void putAll(Map<UUID, Text> nicksById) {
        checkNotNull(nicksById);

        this.clientNicks.putAll(nicksById);
    }

    @SideOnly(Side.CLIENT)
    public void put(UUID uniqueId, Text nick) {
        checkNotNull(uniqueId);
        checkNotNull(nick);

        this.clientNicks.put(uniqueId, nick);
    }

    private ClientboundNucleusNameChangeMappingPacket getMappingMessage(Player player, Text nick) {
        return new ClientboundNucleusNameChangeMappingPacket(player.getUniqueId(), nick);
    }

    private ClientboundNucleusNameMappingsPacket getMappingMessage(NucleusNicknameService service, Player toIgnore) {
        return new ClientboundNucleusNameMappingsPacket(
                Sponge.getServer().getOnlinePlayers().stream().filter((player) -> !player.getUniqueId().equals(toIgnore.getUniqueId())).collect
                        (Collectors.toMap(Identifiable::getUniqueId, v -> service.getNickname(v).orElseGet(() -> Text.of(v.getName()))
                ))
        );
    }
}
