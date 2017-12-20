package com.almuradev.almura.feature.title;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesPacket;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.typesafe.config.ConfigRenderOptions;
import net.kyori.membrane.facet.Activatable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class TitleManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private static final String CONFIG_NAME = "title.conf";
    private static final String CONFIG_HEADER = "Almura title configuration\n\nFor further assistance, join #almura on EsperNet.";
    private static final String TITLES = "titles";
    private final Game game;
    private final PluginContainer container;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final Path configRoot;
    private final Map<String, Text> titles = new LinkedHashMap<>();

    private final Map<UUID, String> selectedTitles = new HashMap<>();

    @Inject
    public TitleManager(final Game game, final PluginContainer container, Logger logger, @ChannelId(NetworkConfig.CHANNEL) final
    ChannelBinding.IndexedMessageChannel network, @ConfigDir(sharedRoot = false) final Path configRoot) {
        this.game = game;
        this.container = container;
        this.logger = logger;
        this.network = network;
        this.configRoot = configRoot;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.game.getCommandManager().register(this.container, TitleCommands.generateRootCommand(), "almura");
    }

    @Listener
    public void onServerStarting(GameStartingServerEvent event) {
        this.titles.clear();

        try {
            this.loadTitles();
            this.logger.info("Loaded {} title(s)", this.titles.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnectedToServerEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.selectedTitles.clear();
    }

    @Listener
    public void onClientConnectionEventJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        Task.builder()
                .async()
                .delayTicks(40)
                .execute(() -> this.createPlayerTitlesPacket().ifPresent((packet) -> {
                    // Send joining player everyone's title (including itself)
                    this.network.sendTo(player, packet);
                })).submit(this.container);

        // TODO Selected Title concept and allowing clients to choose title to display
        // Send joining player's title to everyone
        this.getTitlesFor(player).stream().findFirst().ifPresent((title) ->
                this.game.getServer().getOnlinePlayers().stream().filter((p) -> !p.getUniqueId().equals(player.getUniqueId())).forEach((p) ->
                        this.network.sendTo(p, this.createAddPlayerTitlePacket(player.getUniqueId(), title))));
    }

    @Listener
    public void onClientConnectionEventDisconnect(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        this.network.sendToAll(this.createRemovePlayerTitlePacket(player.getUniqueId()));
    }

    public Map<String, Text> getTitles() {
        return Collections.unmodifiableMap(this.titles);
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    public String getTitleForRender(UUID uniqueId) {
        checkNotNull(uniqueId);

        final String title = this.selectedTitles.get(uniqueId);
        if (title == null) {
            return null;
        }

        return title;
    }

    public Set<Text> getTitlesFor(Player player) {
        checkNotNull(player);

        final Set<Text> playerTitles = new LinkedHashSet<>();

        this.titles.forEach((permission, title) -> {
            if (player.hasPermission(permission)) {
                playerTitles.add(title);
            }
        });

        return playerTitles;
    }

    public void refreshTitles() {
        final ClientboundPlayerTitlesPacket packet = this.createPlayerTitlesPacket().orElse(null);

        if (packet != null) {
            this.game.getServer().getOnlinePlayers().forEach((player) -> this.network.sendTo(player, packet));
        }

    }

    public boolean loadTitles() throws IOException {

        this.titles.clear();

        final Path titlePath = this.configRoot.resolve(CONFIG_NAME);
        boolean exists = this.createConfigIfNeeded(titlePath);

        final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(titlePath);
        if (!exists) {
            loader.save(loader.createEmptyNode());
        }

        final ConfigurationNode root = loader.load();

        final ConfigurationNode titleNode = root.getNode(TITLES);
        if (!titleNode.isVirtual()) {
            titleNode.getChildrenMap().forEach((permission, node) -> {
                final String title = node.getString("");
                if (!title.isEmpty()) {
                    this.titles.put(permission.toString(), TextSerializers.LEGACY_FORMATTING_CODE.deserialize(title));
                }
            });
        }

        return !this.titles.isEmpty();
    }

    private boolean createConfigIfNeeded(Path path) throws IOException {
        checkNotNull(path);

        boolean exists = true;
        // Ensure our file exists
        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            exists = false;
        }

        return exists;
    }

    private ConfigurationLoader<CommentedConfigurationNode> createLoader(Path path) {
        checkNotNull(path);

        return HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(ConfigurationOptions.defaults().setHeader(CONFIG_HEADER))
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .build();
    }

    private Optional<ClientboundPlayerTitlesPacket> createPlayerTitlesPacket() {
        if (this.titles.isEmpty()) {
            return Optional.empty();
        }

        final Map<UUID, Text> playerTitles = new HashMap<>();

        this.game.getServer().getOnlinePlayers().forEach((player -> {
            for (Map.Entry<String, Text> titleEntry : this.titles.entrySet()) {
                final String permission = titleEntry.getKey();
                final Text title = titleEntry.getValue();

                if (player.hasPermission(permission)) {
                    playerTitles.put(player.getUniqueId(), title);
                    break;
                }
            }
        }));

        return Optional.of(new ClientboundPlayerTitlesPacket(Collections.unmodifiableMap(playerTitles)));
    }

    private ClientboundPlayerTitlePacket createAddPlayerTitlePacket(UUID uniqueId, Text title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        return new ClientboundPlayerTitlePacket(uniqueId, title);
    }

    private ClientboundPlayerTitlePacket createRemovePlayerTitlePacket(UUID uniqueId) {
        checkNotNull(uniqueId);

        return new ClientboundPlayerTitlePacket(uniqueId);
    }

    @SideOnly(Side.CLIENT)
    public void putSelectedTitles(Map<UUID, String> titles) {
        this.selectedTitles.clear();

        this.selectedTitles.putAll(titles);
    }

    @SideOnly(Side.CLIENT)
    public void putSelectedTitle(UUID uniqueId, String title) {
        checkNotNull(uniqueId);
        checkNotNull(title);

        this.selectedTitles.put(uniqueId, title);
    }

    @SideOnly(Side.CLIENT)
    public void removeSelectedTitle(UUID uniqueId) {
        checkNotNull(uniqueId);

        this.selectedTitles.remove(uniqueId);
    }
}
