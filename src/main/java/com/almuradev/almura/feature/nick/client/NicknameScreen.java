/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.nick.client;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.notification.PopupNotification;
import com.almuradev.almura.feature.notification.client.ClientNotifications;
import com.almuradev.almura.feature.title.client.PlayerPreview;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.inspirenxe.strata.minecraft.MinecraftText;

import java.util.List;
import java.util.regex.Pattern;

/**
 * The Nicknames window, as 3.5's {@code NicknameGUI}: the nickname to set (with colours from a list), Reset and Remove at the left, the player
 * wearing it at the right as it is typed, and Apply and Close. Typed with {@code &} colour codes, as the Guide's editor.
 */
final class NicknameScreen implements Screen {

    private static final float WIDTH = 400f, HEIGHT = 225f, LEFT_WIDTH = 217f, LEFT_HEIGHT = 90f, RIGHT_WIDTH = 175f, BUTTON_HEIGHT = 16f;
    private static final float MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);
    // 3.5's NickUtil
    private static final Pattern ALLOWED = Pattern.compile("[a-zA-Z0-9_§]+");
    // 3.5's colours
    private static final List<String> COLOURS = List.of("§1Dark Blue", "§9Blue", "§3Dark Aqua", "§bAqua", "§4Dark Red", "§cRed", "§eYellow",
        "§6Gold", "§2Dark Green", "§aGreen", "§5Dark Purple", "§dLight Purple", "§fWhite", "§7Gray", "§8Dark Gray", "§0Black");

    private final boolean canChange, admin;
    // As the player came in, without 3.5's ~ and trailing reset
    // The name Reset goes back to: the nickname the window opened with, or the player's own once it is removed
    private String original;

    NicknameScreen(final boolean canChange, final boolean admin) {
        this.canChange = canChange;
        this.admin = admin;
        final Minecraft minecraft = Minecraft.getInstance();
        String current = minecraft.player == null ? "" : ClientNicks.nicknameFor(minecraft.player.getUUID()) == null
            ? minecraft.player.getGameProfile().name() : ClientNicks.nicknameFor(minecraft.player.getUUID());
        if (current.startsWith("~")) {
            current = current.substring(1);
        }
        if (current.endsWith("§r")) {
            current = current.substring(0, current.length() - 2);
        }
        this.original = current;
    }

    @Override
    public Text title() {
        return Text.of("Nicknames");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final TextBox nickname = factory.textBox().setMaxLength(48).setText(toEditor(this.original));
        nickname.setSize(150f, BUTTON_HEIGHT);
        // The player's name tag follows what is typed, as 3.5's
        nickname.onEvent(TextInput.EDITED, event -> ClientNicks.setPreview(fromEditor(nickname.text())));
        ClientNicks.setPreview(this.original);
        final ComboBox<String> colours = factory.<String>comboBox(MinecraftText::of);
        colours.setItems(COLOURS);
        colours.select(0);
        colours.setSize(100f, BUTTON_HEIGHT);
        final Button addColour = button(factory, "Add", 40f, () -> colours.selected().ifPresent(colour -> {
            nickname.setText(nickname.text() + "&" + colour.charAt(1));
            ClientNicks.setPreview(fromEditor(nickname.text()));
        }));
        final Button reset = button(factory, "Reset", 40f, () -> {
            nickname.setText(toEditor(this.original));
            ClientNicks.setPreview(this.original);
        });
        final Button remove = button(factory, "Remove", 50f, () -> {
            ClientNotifications.show(new PopupNotification("Nickname", "Removing Nickname from server....", 2));
            ClientNicks.change("");
            // The window stays open, showing the player's own name
            final String name = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getGameProfile().name();
            this.original = name;
            nickname.setText(name);
            ClientNicks.setPreview(name);
        }).setEnabled(this.canChange);
        final Button apply = button(factory, "Apply", 40f, () -> this.apply(nickname.text())).setEnabled(this.canChange);
        final Button close = button(factory, "Close", 40f, this::close);

        final AnchorPanel left = pane(factory)
            .add(factory.label(Text.of("Enter desired nickname:").color(WHITE)).setTextScale(1.1f).setAnchor(Anchor.TOP_LEFT).setMargin(4f, 5f, 0f, 0f))
            .add(nickname.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 18f, 0f, 0f))
            .add(factory.label(Text.of("Add colors:").color(WHITE)).setTextScale(1.1f).setAnchor(Anchor.TOP_LEFT).setMargin(4f, 37f, 0f, 0f))
            .add(colours.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 49f, 0f, 0f))
            .add(addColour.setAnchor(Anchor.TOP_LEFT).setMargin(107f, 49f, 0f, 0f))
            .add(reset.setAnchor(Anchor.TOP_LEFT).setMargin(150f, 49f, 0f, 0f))
            .add(remove.setAnchor(Anchor.TOP_LEFT).setMargin(140f, 67f, 0f, 0f));
        final AnchorPanel right = pane(factory)
            .add(PlayerPreview.create(factory, RIGHT_WIDTH - 10f, HEIGHT - 50f, 60, NicknameScreen::shownName).setAnchor(Anchor.TOP_CENTER));
        final float bottom = BUTTON_HEIGHT + 4f;
        final AnchorPanel content = factory.anchorPanel()
            .add(left.setAnchor(Anchor.TOP_LEFT).setSize(LEFT_WIDTH, LEFT_HEIGHT))
            .add(right.setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, bottom).setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.STRETCH)
                .setWidth(RIGHT_WIDTH))
            .add(factory.label(Text.of("Please follow nickname rules.").color(WHITE)).setTextScale(1.1f).setAnchor(Anchor.BOTTOM_LEFT)
                .setMargin(3f, 0f, 0f, 4f))
            .add(apply.setAnchor(Anchor.BOTTOM_RIGHT).setMargin(0f, 0f, 44f, 0f))
            .add(close.setAnchor(Anchor.BOTTOM_RIGHT));
        if (this.admin) {
            content.add(factory.label(Text.of("Administrator").color(GRAY)).setTextScale(0.8f).setAnchor(Anchor.TOP_LEFT)
                .setMargin(4f, LEFT_HEIGHT + 4f, 0f, 0f));
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window, this::close);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * Checks the nickname as 3.5's window did, then sends it.
     */
    private void apply(final String typed) {
        final String nickname = fromEditor(typed.trim());
        final String plain = nickname.replaceAll("§.", "");
        if (nickname.isEmpty()) {
            error("Cannot have blank nickname!");
        } else if (plain.length() <= 3) {
            error("Cannot have nickname < 4 characters!");
        } else if (plain.length() > 20) {
            error("Cannot have nickname > 20 characters!");
        } else if (!ALLOWED.matcher(nickname).matches()) {
            error("Invalid Character in Nickname!");
        } else {
            ClientNotifications.show(new PopupNotification("Nickname", "Updating Nickname on server....", 2));
            ClientNicks.change(nickname);
            // The window stays open; Reset now goes back to the nickname just applied
            this.original = nickname;
        }
    }

    private void close() {
        ClientNicks.setPreview(null);
        Minecraft.getInstance().gui.screen().onClose();
    }

    private static void error(final String message) {
        ClientNotifications.show(new PopupNotification("Error", message, 5));
    }

    private static net.minecraft.network.chat.Component shownName() {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return net.minecraft.network.chat.Component.empty();
        }
        final String nickname = ClientNicks.nicknameFor(minecraft.player.getUUID());
        return nickname == null ? minecraft.player.getName() : net.minecraft.network.chat.Component.literal(nickname);
    }

    private static String toEditor(final String text) {
        return text.replace('§', '&');
    }

    private static String fromEditor(final String text) {
        return text.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");
    }

    private static AnchorPanel pane(final ComponentFactory factory) {
        return factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(3f);
    }

    private static Button button(final ComponentFactory factory, final String text, final float width, final Runnable press) {
        return factory.button(Text.of(text)).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }
}
