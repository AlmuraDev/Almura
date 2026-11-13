/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.mixin.client.LevelLoadingScreenAccessor;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.display.ProgressBar;
import org.inspirenxe.strata.api.event.Subscription;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.jspecify.annotations.Nullable;

/**
 * The world loading screen, shown in place of vanilla's while joining a server or opening a world: the Almura logo, "Loading world..." and,
 * while the world is being made, how far it has got. Vanilla's screen still decides when the world is ready and closes it; see
 * {@link WorldLoading}.
 */
public final class WorldLoadingScreen implements Screen {

    private static final SpriteId LOGO = SpriteId.of(Almura.ID, "almura_logo");
    private static final SpriteId PANEL = SpriteId.of(Almura.ID, "theme/window");
    private static final float WIDTH = 200f, LOGO_WIDTH = 60f, LOGO_HEIGHT = 99f, PROGRESS_WIDTH = 180f, PROGRESS_HEIGHT = 4f;
    private static final long UPDATE_MILLIS = 50L;

    // The vanilla screen showing this one, with vanilla's loading screen behind it
    private @Nullable WorldLoadingHost host;
    private @Nullable ProgressBar progress;
    private @Nullable Subscription updates;
    private double smoothed;

    void setHost(final WorldLoadingHost host) {
        this.host = host;
    }

    @Override
    public Text title() {
        return Text.translatable("multiplayer.downloadingTerrain");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Image logo = factory.image(LOGO).setImageSize(LOGO_WIDTH, LOGO_HEIGHT).setScaling(Image.Scaling.FIT);
        logo.setSize(LOGO_WIDTH, LOGO_HEIGHT);

        final Label message = factory.label(Text.translatable("almura.menu.loading_world")).setTextScale(1.1f);

        final ProgressBar progress = factory.progressBar();
        progress.setRange(0.0, 1.0);
        progress.setSize(PROGRESS_WIDTH, PROGRESS_HEIGHT);
        progress.setVisible(false);
        this.progress = progress;

        final StackPanel column = factory.stackPanel()
            .setSpacing(4)
            .add(centred(logo).setMargin(0, 2, 0, 2))
            .add(factory.separator().setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(centred(message))
            .add(centred(progress));
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP).setMargin(3, 4, 3, 5);

        // A window without a title bar, as on 1.12: the theme's window panel (a nine-slice, so it keeps its border) stretched behind the column
        final Image background = factory.image(PANEL).setImageSize(1, 1).setScaling(Image.Scaling.STRETCH);
        final var panel = factory.anchorPanel()
            .add(background.setAnchor(Anchor.FILL))
            .add(column);
        panel.setWidth(WIDTH);
        panel.setAnchor(Anchor.CENTER);

        return factory.anchorPanel().add(panel).setAnchor(Anchor.FILL);
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

    @Override
    public boolean closeRequested(final ScreenHandle handle) {
        // Like vanilla's, it can't be closed: the world is still loading
        return false;
    }

    /**
     * Shows how far the world has got while it is being made (opening a world on this computer); joining a server has no such progress.
     */
    private void update() {
        final ProgressBar progress = this.progress;
        if (progress == null || this.host == null) {
            return;
        }
        final LevelLoadTracker tracker = ((LevelLoadingScreenAccessor) this.host.loading()).almura$loadTracker();
        if (tracker.hasProgress()) {
            // Eased towards the server's figure, as vanilla's bar is
            this.smoothed += (tracker.serverProgress() - this.smoothed) * 0.2;
            progress.setValue(this.smoothed).setVisible(true);
        } else {
            progress.setVisible(false);
        }
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
