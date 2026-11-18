/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldload.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 3.5's "Loading World" popup, shown while the server loads a world: which world, that the server may pause, and how long it has been.
 * Closed by the player, or by {@link ClientWorldLoad} once the load finishes.
 */
final class WorldLoadNoticeScreen implements Screen {

    private static final float WIDTH = 220f, CLOSE_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    private static final int WHITE = 0xFFFFFFFF, GREY = 0xFFAAAAAA, GOLD = 0xFFFFAA00;
    private static final long UPDATE_MILLIS = 250L;

    private @Nullable Label world;
    private @Nullable Label elapsed;
    private @Nullable Subscription updates;
    private String shownWorlds = "";
    private long shownSeconds = -1;

    @Override
    public Text title() {
        return Text.of("Loading World");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.world = factory.label(Text.of("Loading world...").color(GOLD));
        this.elapsed = factory.label(Text.of("Please wait...").color(GREY));

        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        final AnchorPanel bar = factory.anchorPanel().setHeight(BUTTON_HEIGHT);
        bar.add(close.setAnchor(Anchor.MIDDLE_RIGHT));

        final StackPanel column = factory.stackPanel().setSpacing(4f)
            .add(centred(this.world))
            .add(centred(factory.label(Text.of("The server may pause until it's ready.").color(WHITE))))
            .add(centred(this.elapsed))
            .add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(bar.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));

        final Window window = InGameScreens.closesScreen(factory.window(this.title())
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER));
        this.update();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    @Override
    public void opened(final ScreenHandle handle) {
        this.updates = handle.every(UPDATE_MILLIS, this::update);
    }

    @Override
    public void closed() {
        if (this.updates != null) {
            this.updates.unsubscribe();
            this.updates = null;
        }
    }

    private void update() {
        if (this.world == null || this.elapsed == null) {
            return;
        }
        final List<String> worlds = ClientWorldLoad.worlds();
        final String names = String.join(", ", worlds);
        if (!names.equals(this.shownWorlds)) {
            this.shownWorlds = names;
            this.world.setText(Text.of(worlds.isEmpty() ? "Loading world..." : "Loading world: " + names).color(GOLD));
        }
        final long seconds = Math.max(0L, (System.currentTimeMillis() - ClientWorldLoad.startedAt()) / 1000L);
        if (seconds != this.shownSeconds) {
            this.shownSeconds = seconds;
            this.elapsed.setText(Text.of("Please wait... (" + seconds + "s)").color(GREY));
        }
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
