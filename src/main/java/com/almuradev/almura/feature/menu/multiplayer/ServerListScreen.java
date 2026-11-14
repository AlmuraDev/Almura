/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.multiplayer;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.AlmuraLogoHeader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Activatable;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Almura's servers, shown in place of the title screen's buttons: each with how many are playing or what state it is in, its MOTD as a
 * tooltip, and Join, Back and Other (vanilla's server list) beneath. The servers are asked how they are every three seconds, as on 1.12.
 */
public final class ServerListScreen implements Screen {

    // Almura's servers; Development is left out of the list while it is offline
    private static final List<Server> SERVERS = List.of(
        new Server("almura.menu.server.live.name", "srv1.almuramc.com", 25565, false),
        new Server("almura.menu.server.dev.name", "dev.almuramc.com", 25565, true));

    private static final SpriteId WHITE = SpriteId.of(Almura.ID, "theme/white");
    private static final long QUERY_EVERY_MILLIS = 3000L;
    private static final float WIDTH = 200f, LIST_WIDTH = 198f, ROW_HEIGHT = 24f, BUTTON_HEIGHT = 20f, BUTTON_SHORT = 98f;
    private static final float LOGO_GAP = 9f, GAP = 2f, LIST_PADDING = 4f, INDICATOR_WIDTH = 5f;

    // Queries wait on the network, so they run off the client thread, one round at a time
    private static final ExecutorService QUERIES = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "Almura server list query");
        thread.setDaemon(true);
        return thread;
    });

    private @Nullable ListView<Server> list;
    private @Nullable Button join;
    private @Nullable Subscription updates;
    private boolean querying;

    @Override
    public Text title() {
        return Text.translatable("menu.multiplayer");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final ListView<Server> list = factory.<Server>listView(server -> row(factory, server));
        list.setWidth(LIST_WIDTH);
        list.setRowHeight(ROW_HEIGHT);
        list.setActivateOnClick(false);
        list.onEvent(Selection.SELECTED, event -> this.updateButtons());
        // Double-click or Enter on a server joins it
        list.onEvent(Activatable.ACTIVATED, event -> this.join());
        this.list = list;

        final Button join = factory.button(Text.translatable("almura.menu_button.join"))
            .setSize(WIDTH, BUTTON_HEIGHT)
            .setEnabled(false)
            .onEvent(Button.PRESSED, event -> this.join());
        this.join = join;
        final Button back = factory.button(Text.translatable("gui.back"))
            .setSize(BUTTON_SHORT, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        final Button other = factory.button(Text.translatable("almura.menu_button.other"))
            .setSize(BUTTON_SHORT, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> {
                final Minecraft minecraft = Minecraft.getInstance();
                minecraft.gui.setScreen(new JoinMultiplayerScreen(minecraft.gui.screen()));
            });
        final AnchorPanel lowerRow = factory.anchorPanel()
            .add(back.setAnchor(Anchor.TOP_LEFT))
            .add(other.setAnchor(Anchor.TOP_RIGHT));
        lowerRow.setSize(WIDTH, BUTTON_HEIGHT);

        final StackPanel below = factory.stackPanel().setSpacing(GAP).add(centred(list)).add(join).add(lowerRow);

        // The logo where the title screen has it: the top of a column as tall as the title screen's
        final StackPanel middle = factory.stackPanel()
            .setSpacing(LOGO_GAP)
            .add(centred(AlmuraLogoHeader.create(factory)))
            .add(centred(below));
        middle.setHeight(AlmuraLogoHeader.MENU_HEIGHT);
        middle.setAnchor(Anchor.CENTER);

        this.refresh();
        return factory.anchorPanel().add(middle).setAnchor(Anchor.FILL);
    }

    @Override
    public void opened(final ScreenHandle handle) {
        this.query();
        this.updates = handle.every(QUERY_EVERY_MILLIS, this::query);
    }

    @Override
    public void closed() {
        if (this.updates != null) {
            this.updates.unsubscribe();
            this.updates = null;
        }
    }

    /**
     * Asks every server how it is, off the client thread, then shows the answers.
     */
    private void query() {
        if (this.querying) {
            return;
        }
        this.querying = true;
        CompletableFuture.runAsync(() -> SERVERS.forEach(Server::query), QUERIES)
            .whenComplete((ignored, error) -> Minecraft.getInstance().execute(() -> {
                this.querying = false;
                this.refresh();
            }));
    }

    /**
     * Shows the servers as last asked: the ones that can be hidden only while they answer, keeping the selection, the first selected to start.
     */
    private void refresh() {
        final ListView<Server> list = this.list;
        if (list == null) {
            return;
        }
        final Server selected = list.selected().orElse(null);
        final List<Server> shown = SERVERS.stream().filter(Server::shown).toList();
        list.setItems(shown);
        list.setHeight(shown.size() * ROW_HEIGHT + LIST_PADDING);
        if (selected != null && shown.contains(selected)) {
            list.selectItem(selected);
        } else if (!shown.isEmpty()) {
            list.selectItem(shown.getFirst());
        }
        this.updateButtons();
    }

    private void updateButtons() {
        if (this.join != null && this.list != null) {
            this.join.setEnabled(this.list.selected().map(server -> server.status.joinable).orElse(false));
        }
    }

    /**
     * Joins the selected server, through vanilla's connection and Almura's Server Status screen.
     */
    private void join() {
        final Server server = this.list == null ? null : this.list.selected().orElse(null);
        if (server == null || !server.status.joinable) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final String ip = server.host + ":" + server.port;
        final ServerData data = new ServerData(net.minecraft.client.resources.language.I18n.get(server.nameKey), ip, ServerData.Type.OTHER);
        ConnectScreen.startConnecting(minecraft.gui.screen(), minecraft, ServerAddress.parseString(ip), data, false, null);
    }

    /**
     * A server's row: its name on the left, its player count or state on the right, and its state's colour along the right edge.
     */
    private static Component<?> row(final ComponentFactory factory, final Server server) {
        final Status status = server.status;
        final Label name = factory.label(Text.translatable(server.nameKey).color(0xFFFFFFFF));
        final Label state = factory.label(status == Status.ONLINE
            ? Text.of(server.players + "/" + server.maxPlayers).color(0xFFFFFFFF)
            : Text.translatable(status.key).color(status.color));
        final Image indicator = factory.image(WHITE).setTint(status.color).setScaling(Image.Scaling.STRETCH);
        indicator.setSize(INDICATOR_WIDTH, ROW_HEIGHT - 4f);

        final AnchorPanel row = factory.anchorPanel()
            .add(name.setAnchor(Anchor.MIDDLE_LEFT).setMargin(4f, 0f, 0f, 0f))
            .add(state.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, INDICATOR_WIDTH + 6f, 0f))
            .add(indicator.setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f, 0f));
        // Across the whole row the list gives it, so the name and the state sit at its two ends
        row.setAnchor(Anchor.FILL);
        if (server.motd != null && !server.motd.isBlank()) {
            row.setTooltip(Text.of(server.motd));
        }
        return row;
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    /**
     * How a server is: its translation, colour and whether it can be joined. Locked (whitelisted) servers can, for those on the whitelist.
     */
    enum Status {
        OFFLINE("almura.multiplayer.status.offline", 0xFFFF5555, false),
        ONLINE("almura.multiplayer.status.online", 0xFF55FF55, true),
        RESTARTING("almura.multiplayer.status.restarting", 0xFFFFFFAA, false),
        UPDATING("almura.multiplayer.status.updating", 0xFFAA00AA, false),
        LOCKED("almura.multiplayer.status.locked", 0xFFAAAAAA, true);

        final String key;
        final int color;
        final boolean joinable;

        Status(final String key, final int color, final boolean joinable) {
            this.key = key;
            this.color = color;
            this.joinable = joinable;
        }
    }

    /**
     * One of Almura's servers and how it was when last asked. Asked off the client thread and read on it; each answer is written as a whole
     * before the list is shown again.
     */
    static final class Server {

        final String nameKey, host;
        final int port;
        final boolean canHide;
        private final ServerQuery query;
        volatile Status status = Status.UPDATING;
        volatile int players, maxPlayers;
        volatile @Nullable String motd;

        Server(final String nameKey, final String host, final int port, final boolean canHide) {
            this.nameKey = nameKey;
            this.host = host;
            this.port = port;
            this.canHide = canHide;
            this.query = new ServerQuery(host, port);
        }

        boolean shown() {
            return !this.canHide || (this.status != Status.OFFLINE && this.status != Status.UPDATING);
        }

        /**
         * Asks the server how it is: offline when it takes no connection, restarting when it doesn't answer the query, updating until it
         * reports its player limit, locked when whitelisted.
         */
        void query() {
            if (!this.query.ping()) {
                this.status = Status.OFFLINE;
                return;
            }
            final Map<String, String> values = this.query.query();
            if (values == null) {
                this.status = Status.RESTARTING;
                this.players = 0;
                this.maxPlayers = 0;
                return;
            }
            this.players = parse(values.get("numplayers"));
            this.maxPlayers = parse(values.get("maxplayers"));
            this.motd = values.get("hostname");
            if (this.maxPlayers <= 0) {
                this.status = Status.UPDATING;
            } else {
                this.status = "true".equals(values.get("whitelist")) ? Status.LOCKED : Status.ONLINE;
            }
        }

        private static int parse(final @Nullable String number) {
            try {
                return number == null ? 0 : Integer.parseInt(number.trim());
            } catch (final NumberFormatException ignored) {
                return 0;
            }
        }
    }
}
