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
import com.almuradev.almura.mixin.client.DisconnectedScreenAccessor;
import com.almuradev.almura.mixin.client.JoinMultiplayerScreenAccessor;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftText;

/**
 * Almura's disconnected screen, as on 1.12: a "Server Status" window with the fallen Steve, "Disconnected from Server", the reason in pink
 * between two lines, and the button back to the server list. Vanilla's buttons for a server's bug report link and a saved error report
 * show above it when there is one.
 */
final class DisconnectedMenuScreen implements Screen {

    private static final SpriteId DEAD_STEVE = SpriteId.of(Almura.ID, "dead_steve");
    private static final float WIDTH = 200f, STEVE = 59f, BUTTON_HEIGHT = 20f;
    // 1.12's colour for the reason
    private static final int REASON = 0xFFF7C8CB;

    private final DisconnectedScreen vanilla;

    DisconnectedMenuScreen(final DisconnectedScreen vanilla) {
        this.vanilla = vanilla;
    }

    @Override
    public Text title() {
        return Text.of("Server Status");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final DisconnectionDetails details = ((DisconnectedScreenAccessor) this.vanilla).almura$details();

        final Image steve = factory.image(DEAD_STEVE).setImageSize(STEVE, STEVE).setScaling(Image.Scaling.FIT);
        steve.setSize(STEVE, STEVE);
        final Label heading = factory.label("Disconnected from Server").setTextScale(1.1f);
        final Label reason = factory.label(details.reason().getString().isEmpty() ? Text.of("Unknown Reason") : MinecraftText.of(details.reason()))
            .setTextScale(0.7f)
            .setWrap(true)
            .setTextAlignment(HorizontalAlignment.CENTER)
            .setStyle(TextStyle.DEFAULT.withColor(REASON).withShadow(true));

        final StackPanel column = factory.stackPanel()
            .setSpacing(3f)
            .add(centred(steve).setMargin(0f, 2f, 0f, 2f))
            .add(centred(heading))
            .add(stretched(factory.separator()))
            .add(stretched(reason).setMargin(4f, 1f, 4f, 1f))
            .add(stretched(factory.separator()));

        // A server's link to report the problem, and the folder of a report the game saved, as vanilla offers them
        details.bugReportLink().ifPresent(link -> column.add(centred(factory.button(net.minecraft.network.chat.Component.translatable(
                "gui.report_to_server").getString())
            .setHeight(BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> ConfirmLinkScreen.confirmLinkNow(Minecraft.getInstance().gui.screen(), link, false)))));
        details.report().ifPresent(report -> column.add(centred(factory.button(net.minecraft.network.chat.Component.translatable(
                "gui.open_report_dir").getString())
            .setHeight(BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Blaze3D.openPath(report.getParent())))));

        // As far below the line above as the window's edge is below it
        column.add(centred(factory.button(this.buttonText())
            .setHeight(BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> this.goBack()))
            .setMargin(0f, -1f, 0f, 0f));

        final Window window = factory.window(this.title())
            .setClosable(false)
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            // Across the whole window, so the column's centred items sit in the window's middle
            .add(stretched(column))
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    @Override
    public boolean closeRequested(final ScreenHandle handle) {
        // Escape goes back, as the button does (vanilla's screen ignores it), as on 1.12
        this.goBack();
        return false;
    }

    /**
     * 1.12's words for going back to the server list; vanilla's own for anywhere else (the title screen, the world list).
     */
    private Text buttonText() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.allowsMultiplayer()) {
            return Text.translatable("gui.toTitle");
        }
        final net.minecraft.network.chat.Component text = ((DisconnectedScreenAccessor) this.vanilla).almura$buttonText();
        if (text.getContents() instanceof final TranslatableContents translatable && translatable.getKey().equals("gui.toMenu")) {
            return Text.of("Return to Server Menu");
        }
        return MinecraftText.of(text);
    }

    /**
     * Back where vanilla's button goes: the screen the player connected from. Kicked while playing, vanilla goes to its own server list over
     * the title screen; Almura's server list shows there instead, as on 1.12.
     */
    private void goBack() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.allowsMultiplayer()) {
            minecraft.gui.setScreen(new TitleScreen());
            return;
        }
        final net.minecraft.client.gui.screens.Screen parent = ((DisconnectedScreenAccessor) this.vanilla).almura$parent();
        if (parent instanceof final JoinMultiplayerScreen list && ((JoinMultiplayerScreenAccessor) list).almura$lastScreen() instanceof TitleScreen) {
            // The title screen first, which Almura's replaces on the next frame, then the server list above it
            minecraft.gui.setScreen(new TitleScreen());
            minecraft.schedule(() -> AlmuraScreens.open(new ServerListScreen()));
            return;
        }
        minecraft.gui.setScreen(parent);
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    private static <T extends Component<T>> T stretched(final T component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }
}
