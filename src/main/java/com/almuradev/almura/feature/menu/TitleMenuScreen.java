/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.multiplayer.ServerListScreen;
import com.almuradev.almura.feature.menu.singleplayer.WorldListScreen;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.neoforged.neoforge.client.gui.modlist.ModListScreen;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;

import java.net.URI;
import java.util.function.Function;

/**
 * Almura's title screen, in place of vanilla's: the Almura logo over Singleplayer, Multiplayer, Options, Mods, About and Quit, the trademark
 * and copyright in the bottom left and links to the shop, the issue tracker and the forums in the bottom right. Vanilla's panorama shows
 * behind it, blurred, as on 1.12.
 */
public final class TitleMenuScreen implements Screen {

    private static final SpriteId ICON_FORUMS = SpriteId.of(Almura.ID, "icon/forums");
    private static final SpriteId ICON_ISSUES = SpriteId.of(Almura.ID, "icon/github");
    private static final SpriteId ICON_SHOP = SpriteId.of(Almura.ID, "icon/shop");

    private static final String URL_FORUMS = "https://almura.enjin.com/forum";
    private static final String URL_ISSUES = "https://github.com/AlmuraDev/Almura/issues";
    private static final String URL_SHOP = "https://almura.enjin.com/shop";

    // 1.12's sizes: long and tiny buttons, square icon buttons, 4 between them
    private static final float BUTTON_LONG = 200f, BUTTON_TINY = 64f, BUTTON_HEIGHT = 20f, ICON_BUTTON = 24f;
    private static final float PADDING = 4f, LOGO_GAP = 10f;
    private static final int QUIT_RED = 0xFFFF5555, LEGAL_YELLOW = 0xFFFFFF55;

    @Override
    public Text title() {
        return Text.translatable("narrator.screen.title");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Button singleplayer = long_(factory.button(Text.translatable("menu.singleplayer")))
            .onEvent(Button.PRESSED, event -> AlmuraScreens.open(new WorldListScreen()));
        // Almura's servers, over this screen; its Other button leads on to vanilla's server list
        final Button multiplayer = long_(factory.button(Text.translatable("menu.multiplayer")))
            .onEvent(Button.PRESSED, event -> AlmuraScreens.open(new ServerListScreen()));

        final Button options = tiny(factory.button(Text.translatable("options.title")))
            .onEvent(Button.PRESSED, event -> open(parent -> new OptionsScreen(parent, Minecraft.getInstance().options)));
        final Button mods = tiny(factory.button(Text.translatable("almura.menu_button.mods")))
            .onEvent(Button.PRESSED, event -> open(ModListScreen::create));
        final Button about = tiny(factory.button(Text.translatable("almura.menu_button.about")))
            .onEvent(Button.PRESSED, event -> AlmuraScreens.open(new AboutScreen()));
        final StackPanel middleRow = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(PADDING).add(options).add(mods).add(about);

        final Button quit = long_(factory.button(Text.translatable("almura.menu_button.quit").color(QUIT_RED)))
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().stop());

        final StackPanel buttons = factory.stackPanel()
            .setSpacing(PADDING)
            .add(singleplayer)
            .add(multiplayer)
            .add(middleRow)
            .add(quit);

        // The logo at the top of a column as tall as the server list's, so it stays in place between the two
        final StackPanel middle = factory.stackPanel()
            .setSpacing(LOGO_GAP)
            .add(centred(AlmuraLogoHeader.create(factory)))
            .add(centred(buttons));
        middle.setHeight(AlmuraLogoHeader.MENU_HEIGHT);
        middle.setAnchor(Anchor.CENTER);

        // The copyright above the trademark, in the bottom left
        final StackPanel legal = factory.stackPanel()
            .setSpacing(PADDING)
            .add(legal(factory, "almura.menu.main.copyright"))
            .add(legal(factory, "almura.menu.main.trademark"));
        legal.setAnchor(Anchor.BOTTOM_LEFT).setMargin(PADDING);

        // Shop, issues and forums from left to right, in the bottom right
        final StackPanel links = factory.stackPanel(Orientation.HORIZONTAL)
            .setSpacing(PADDING)
            .add(link(factory, ICON_SHOP, "almura.menu_button.shop", URL_SHOP))
            .add(link(factory, ICON_ISSUES, "almura.menu_button.issues", URL_ISSUES))
            .add(link(factory, ICON_FORUMS, "almura.menu_button.forums", URL_FORUMS));
        links.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(PADDING);

        return factory.anchorPanel().add(middle).add(legal).add(links).setAnchor(Anchor.FILL);
    }

    @Override
    public boolean closeRequested(final ScreenHandle handle) {
        // Escape does nothing on the title screen, as on 1.12 and vanilla
        return false;
    }

    /**
     * Opens a vanilla screen that comes back here when it closes.
     */
    private static void open(final Function<net.minecraft.client.gui.screens.Screen, net.minecraft.client.gui.screens.Screen> screen) {
        final Minecraft minecraft = Minecraft.getInstance();
        // This screen as vanilla sees it, which the next screen returns to
        final net.minecraft.client.gui.screens.Screen parent = minecraft.gui.screen();
        minecraft.gui.setScreen(screen.apply(parent));
    }

    private static Label legal(final ComponentFactory factory, final String key) {
        return factory.label(Text.translatable(key).color(LEGAL_YELLOW));
    }

    private static Button link(final ComponentFactory factory, final SpriteId icon, final String tooltip, final String url) {
        return factory.button()
            .setIcon(icon)
            .setSize(ICON_BUTTON, ICON_BUTTON)
            .setTooltip(Text.translatable(tooltip))
            .onEvent(Button.PRESSED, event -> Blaze3D.openUri(URI.create(url)));
    }

    private static Button long_(final Button button) {
        return button.setSize(BUTTON_LONG, BUTTON_HEIGHT);
    }

    private static Button tiny(final Button button) {
        return button.setSize(BUTTON_TINY, BUTTON_HEIGHT);
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
