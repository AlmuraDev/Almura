/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.mixin.client.ConnectScreenAccessor;
import io.netty.channel.ChannelFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The Server Status screen, shown in place of vanilla's connect screen while joining a server: the Almura logo, how far the connection has got
 * and a way back to the server list. Client only: it is reached from {@link com.almuradev.almura.AlmuraClient} alone. Vanilla's connect screen still makes the connection; this screen ticks it, as vanilla's would while it
 * shows, until vanilla moves on to loading the world or to a disconnected screen.
 */
public final class ServerStatusScreen implements Screen {

    private static final SpriteId LOGO = SpriteId.of(Almura.ID, "almura_logo");
    private static final float WIDTH = 200f, LOGO_WIDTH = 60f, LOGO_HEIGHT = 99f;
    private static final int WHITE = 0xFFFFFFFF, GREEN = 0xFF55FF55;
    private static final long UPDATE_MILLIS = 50L;
    // The line of scrambled letters under the status, as 1.12's obfuscated text drew it (Strata draws obfuscated text plainly)
    private static final String MAGIC_LETTERS = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz0123456789";
    private static final int MAGIC_LENGTH = 12;

    // The screen showing, which the client tick keeps the connection of
    private static @Nullable ServerStatusScreen showing;

    private final ConnectScreen connecting;
    private @Nullable Label state, detail, magic;
    private @Nullable Subscription updates;

    ServerStatusScreen(final ConnectScreen connecting) {
        this.connecting = connecting;
    }

    /**
     * @return The vanilla connect screen behind the Server Status screen showing, if one is
     */
    static @Nullable ConnectScreen showingConnection() {
        final ServerStatusScreen screen = ServerStatusScreen.showing;
        return screen == null ? null : screen.connecting;
    }

    @Override
    public Text title() {
        return Text.of("Server Status");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Image logo = factory.image(LOGO).setImageSize(LOGO_WIDTH, LOGO_HEIGHT).setScaling(Image.Scaling.FIT);
        logo.setSize(LOGO_WIDTH, LOGO_HEIGHT);

        this.state = centred(factory.label("Connecting to server...")).setTextScale(1.1f);
        this.detail = centred(factory.label("")).setTextScale(1.1f);
        this.magic = centred(factory.label(scrambled()));

        final Button back = factory.button("Return to Server Menu").onEvent(Button.PRESSED, event -> this.returnToServerList());

        final StackPanel column = factory.stackPanel()
            .setSpacing(3)
            .add(centred(logo).setMargin(0, 2, 0, 2))
            .add(stretched(factory.separator()))
            .add(this.state)
            .add(this.detail)
            .add(this.magic)
            .add(stretched(factory.separator()))
            .add(centred(back));

        final Window window = factory.window(this.title())
            .setClosable(false)
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(column)
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER);

        this.update();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    @Override
    public void opened(final ScreenHandle handle) {
        ServerStatusScreen.showing = this;
        this.updates = handle.every(UPDATE_MILLIS, this::update);
    }

    @Override
    public void closed() {
        if (this.updates != null) {
            this.updates.unsubscribe();
            this.updates = null;
        }
        if (ServerStatusScreen.showing == this) {
            ServerStatusScreen.showing = null;
        }
    }

    @Override
    public boolean closeRequested(final ScreenHandle handle) {
        // Escape goes back to the server list, as the button does
        this.returnToServerList();
        return false;
    }

    /**
     * Shows how far the connection has got: connecting until there is a connection, then vanilla's step of the login ("Logging in...",
     * "Joining world...").
     */
    private void update() {
        final ConnectScreenAccessor accessor = (ConnectScreenAccessor) this.connecting;
        final boolean connected = accessor.almura$connection() != null;
        if (this.state != null) {
            this.state.setText(Text.of(connected ? "Connected." : "Connecting to server..."))
                .setStyle(TextStyle.DEFAULT.withColor(connected ? GREEN : WHITE).withShadow(true));
        }
        if (this.detail != null) {
            this.detail.setText(connected ? Text.of(accessor.almura$status().getString()) : Text.empty());
            this.detail.setVisible(connected);
        }
        if (this.magic != null) {
            this.magic.setText(Text.of(scrambled()));
        }
    }

    /**
     * Cancels the connection as vanilla's Cancel button does, then goes back to the screen the player connected from.
     */
    private void returnToServerList() {
        final ConnectScreenAccessor accessor = (ConnectScreenAccessor) this.connecting;
        // The connector thread holds the same lock while it makes the connection
        synchronized (this.connecting) {
            accessor.almura$setAborted(true);
            final ChannelFuture future = accessor.almura$channelFuture();
            if (future != null) {
                future.cancel(true);
                accessor.almura$setChannelFuture(null);
            }
            final Connection connection = accessor.almura$connection();
            if (connection != null) {
                connection.disconnect(ConnectScreen.ABORT_CONNECTION);
            }
        }
        Minecraft.getInstance().gui.setScreen(accessor.almura$parent());
    }

    private static String scrambled() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final StringBuilder letters = new StringBuilder(MAGIC_LENGTH);
        for (int i = 0; i < MAGIC_LENGTH; i++) {
            letters.append(MAGIC_LETTERS.charAt(random.nextInt(MAGIC_LETTERS.length())));
        }
        return letters.toString();
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    private static <T extends Component<T>> T stretched(final T component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }
}
