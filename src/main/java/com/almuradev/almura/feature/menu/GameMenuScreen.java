/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WorldOptionsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;

import java.net.URI;
import java.util.function.Function;

/**
 * Almura's in-game menu (Escape), in place of vanilla's, as on 1.12: the logo, Back to Game, a row of icons (the shop, the live map, the
 * server's statistics, advancements, the forums, the world's options, the game's options) and Quit in red.
 */
final class GameMenuScreen implements Screen {

    private static final SpriteId LOGO = SpriteId.of(Almura.ID, "almura_logo");
    private static final SpriteId ICON_SHOP = SpriteId.of(Almura.ID, "icon/shop");
    private static final SpriteId ICON_MAP = SpriteId.of(Almura.ID, "icon/map");
    private static final SpriteId ICON_STATISTICS = SpriteId.of(Almura.ID, "icon/statistics");
    private static final SpriteId ICON_ADVANCEMENTS = SpriteId.of(Almura.ID, "icon/advancements");
    private static final SpriteId ICON_FORUMS = SpriteId.of(Almura.ID, "icon/forums");
    private static final SpriteId ICON_WORLD_OPTIONS = SpriteId.of(Almura.ID, "icon/lan");
    private static final SpriteId ICON_OPTIONS = SpriteId.of(Almura.ID, "icon/options");

    private static final String URL_SHOP = "https://almura.enjin.com/shop";
    private static final String URL_MAP = "http://srv1.almuramc.com:8123";
    private static final String URL_STATISTICS = "http://srv1.almuramc.com:8080";
    private static final String URL_FORUMS = "https://almura.enjin.com/forum";

    // 1.12's sizes
    private static final float LOGO_WIDTH = 60f, LOGO_HEIGHT = 99f, BUTTON_LONG = 192f, BUTTON_SHORT = 98f, BUTTON_HEIGHT = 20f;
    private static final float ICON_BUTTON = 24f, PADDING = 4f, LOGO_GAP = 10f, QUIT_GAP = 25f;
    private static final int QUIT_RED = 0xFFFF5555;

    @Override
    public Text title() {
        return Text.translatable("menu.game");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Image logo = factory.image(LOGO).setImageSize(LOGO_WIDTH, LOGO_HEIGHT).setScaling(Image.Scaling.FIT);
        logo.setSize(LOGO_WIDTH, LOGO_HEIGHT);

        final Button back = factory.button(Text.translatable("menu.returnToGame"))
            .setSize(BUTTON_LONG, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        final Minecraft minecraft = Minecraft.getInstance();
        final StackPanel icons = factory.stackPanel(Orientation.HORIZONTAL)
            .setSpacing(PADDING)
            .add(link(factory, ICON_SHOP, Text.translatable("almura.menu_button.shop"), URL_SHOP))
            .add(link(factory, ICON_MAP, Text.translatable("item.minecraft.map"), URL_MAP))
            .add(link(factory, ICON_STATISTICS, Text.translatable("gui.stats"), URL_STATISTICS))
            .add(icon(factory, ICON_ADVANCEMENTS, Text.translatable("gui.advancements"))
                .setEnabled(minecraft.player != null)
                .onEvent(Button.PRESSED, event -> open(parent -> new AdvancementsScreen(minecraft.player.connection.getAdvancements(), parent))))
            .add(link(factory, ICON_FORUMS, Text.translatable("almura.menu_button.forums"), URL_FORUMS))
            // Sharing to LAN, on 1.12; the world's options, where 26.3 moved it
            .add(icon(factory, ICON_WORLD_OPTIONS, Text.translatable("options.worldOptions.button"))
                .setEnabled(minecraft.level != null)
                .onEvent(Button.PRESSED, event -> open(parent -> new WorldOptionsScreen(parent, minecraft.level))))
            .add(icon(factory, ICON_OPTIONS, Text.translatable("menu.options"))
                .onEvent(Button.PRESSED, event -> open(parent -> new OptionsScreen(parent, minecraft.options))));

        final Button quit = factory.button(Text.translatable("almura.menu_button.quit").color(QUIT_RED))
            .setSize(BUTTON_SHORT, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> quit());

        final StackPanel column = factory.stackPanel()
            .add(centred(logo))
            .add(centred(back).setMargin(0f, LOGO_GAP, 0f, 0f))
            .add(centred(icons).setMargin(0f, PADDING, 0f, 0f))
            .add(centred(quit).setMargin(0f, QUIT_GAP, 0f, 0f));
        column.setAnchor(Anchor.CENTER);
        return factory.anchorPanel().add(column).setAnchor(Anchor.FILL);
    }

    /**
     * Leaves the world as vanilla's menu does (saving it, playing on this computer, and asking first about a report being written), then
     * goes to the title screen, as on 1.12, where vanilla would show its server list after a server.
     */
    private static void quit() {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getReportingContext().draftReportHandled(minecraft, minecraft.gui.screen(), () -> {
            minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
            if (!(minecraft.gui.screen() instanceof TitleScreen)) {
                minecraft.gui.setScreen(new TitleScreen());
            }
        }, true);
    }

    /**
     * Opens a vanilla screen that comes back here when it closes.
     */
    private static void open(final Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> screen) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(screen.apply(minecraft.gui.screen()));
    }

    private static Button link(final ComponentFactory factory, final SpriteId sprite, final Text tooltip, final String url) {
        return icon(factory, sprite, tooltip).onEvent(Button.PRESSED, event -> Blaze3D.openUri(URI.create(url)));
    }

    private static Button icon(final ComponentFactory factory, final SpriteId sprite, final Text tooltip) {
        return factory.button()
            .setIcon(sprite)
            .setSize(ICON_BUTTON, ICON_BUTTON)
            .setTooltip(tooltip);
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
