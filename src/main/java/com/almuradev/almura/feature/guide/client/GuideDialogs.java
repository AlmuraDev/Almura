/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.client;

import com.almuradev.almura.feature.guide.network.GuideMessages.Abilities;
import com.almuradev.almura.feature.guide.network.GuideMessages.ChangeType;
import com.almuradev.almura.feature.guide.network.GuideMessages.ClientboundPage;
import com.almuradev.almura.feature.guide.network.GuideMessages.ServerboundChange;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextArea;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.minecraft.MinecraftText;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The guide's dialogs, over its window, as 3.5's screens: Create a page, Details for a page, a link's web address, and the question before
 * removing a page. Client only.
 */
final class GuideDialogs {

    private static final float WIDTH = 150f, DETAILS_WIDTH = 190f, LINK_WIDTH = 220f, BUTTON_WIDTH = 50f, BUTTON_HEIGHT = 16f;
    private static final int RED = 0xFFFF5555, GRAY = 0xFFAAAAAA;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private GuideDialogs() {
    }

    private static void send(final ServerboundChange change) {
        ClientPacketDistributor.sendToServer(change);
    }

    /**
     * 3.5's Create a page: a server page's ID (a-z, 0-9, _ and -), index and name, or with Player guide (or for a player who may only add
     * their own) just the name.
     */
    static void create(final ComponentFactory factory, final Abilities abilities) {
        final TextBox id = filtered(factory.textBox(), "[^a-z0-9_-]", 64, true);
        final TextBox index = filtered(factory.textBox(), "[^0-9]", 9, false).setText("0");
        final TextBox name = factory.textBox().setMaxLength(GuideMessages.MAX_NAME);
        final Label idLabel = factory.label("ID"), indexLabel = factory.label("Index");
        final CheckBox player = factory.checkBox("Player guide");
        player.setTooltip(Text.of("Only you and guide admins will see it"));
        final boolean choose = abilities.add() && abilities.addPlayer();
        player.setVisible(choose);

        final Window window = window(factory, "Create a page", WIDTH);
        final Runnable mode = () -> {
            final boolean playerGuide = !abilities.add() || player.state() == CheckState.CHECKED;
            window.setTitle(Text.of(playerGuide ? "Create my guide" : "Create a page"));
            for (final Component<?> serverOnly : new Component<?>[] {id, index, idLabel, indexLabel}) {
                serverOnly.setVisible(!playerGuide);
            }
        };
        player.onEvent(CheckBox.CHANGED, event -> mode.run());
        mode.run();

        final Overlay[] overlay = new Overlay[1];
        final Runnable save = () -> {
            final boolean playerGuide = !abilities.add() || player.state() == CheckState.CHECKED;
            if (GuideText.stripped(name.text()).isBlank() || !playerGuide && (id.text().isEmpty() || index.text().isEmpty())) {
                return;
            }
            send(new ServerboundChange(ChangeType.ADD, playerGuide ? "" : id.text(), playerGuide ? 0 : Integer.parseInt(index.text()),
                GuideEditor.fromEditor(name.text()), "", playerGuide));
            overlay[0].close();
        };
        final StackPanel column = column(factory)
            .add(stretch(player))
            .add(stretch(idLabel)).add(stretch(id))
            .add(stretch(indexLabel)).add(stretch(index))
            .add(stretch(factory.label("Name"))).add(stretch(name))
            .add(buttons(factory, save, () -> overlay[0].close()));
        for (final TextBox field : new TextBox[] {id, index, name}) {
            field.onEvent(TextBox.SUBMITTED, event -> save.run());
        }
        overlay[0] = factory.overlays().dialog(window.add(column));
    }

    /**
     * 3.5's Details for a page: its ID, index (a server page's) and name to change, and who made and last changed it, when.
     */
    static void details(final ComponentFactory factory, final ClientboundPage page) {
        final TextBox index = filtered(factory.textBox(), "[^0-9]", 9, false).setText(Integer.toString(page.index()));
        index.setReadOnly(page.playerGuide());
        final TextBox name = factory.textBox().setMaxLength(GuideMessages.MAX_NAME * 2).setText(GuideEditor.toEditor(page.name()));
        final Overlay[] overlay = new Overlay[1];
        final Runnable save = () -> {
            if (GuideText.stripped(name.text()).isBlank() || index.text().isEmpty()) {
                return;
            }
            send(new ServerboundChange(ChangeType.MODIFY, page.id(), Integer.parseInt(index.text()), GuideEditor.fromEditor(name.text()), page.content(),
                page.playerGuide()));
            overlay[0].close();
        };
        final StackPanel column = column(factory)
            .add(stretch(factory.label("ID"))).add(stretch(readOnly(factory, page.id())))
            .add(stretch(factory.label("Index"))).add(stretch(index))
            .add(stretch(factory.label("Name"))).add(stretch(name))
            .add(stretch(factory.label(Text.of("Shows as: ").color(GRAY).append(MinecraftText.of(page.name())))))
            .add(stretch(factory.label("Creator"))).add(stretch(readOnly(factory, page.creator())))
            .add(stretch(factory.label("Created"))).add(stretch(readOnly(factory, DATE.format(Instant.ofEpochMilli(page.created())))))
            .add(stretch(factory.label("Last Modifier"))).add(stretch(readOnly(factory, page.modifier())))
            .add(stretch(factory.label("Last Modified"))).add(stretch(readOnly(factory, DATE.format(Instant.ofEpochMilli(page.modified())))))
            .add(buttons(factory, save, () -> overlay[0].close()));
        name.onEvent(TextBox.SUBMITTED, event -> save.run());
        overlay[0] = factory.overlays().dialog(window(factory, "Details for", DETAILS_WIDTH).add(column));
    }

    /**
     * 3.5's Create Link: the selected text and its web address (http or https).
     */
    static void link(final ComponentFactory factory, final TextArea area) {
        if (!GuideEditor.canLink(area)) {
            return;
        }
        final int start = Math.min(area.selectionStart(), area.selectionEnd()), end = Math.max(area.selectionStart(), area.selectionEnd());
        link(factory, area, start, end, area.selectedText(), "", () -> { });
    }

    /**
     * 3.5's Create Link, or Edit Link for one with an address: the text from {@code start} to {@code end} of the area becomes a link to
     * the address typed (http or https); {@code after} runs once it has.
     */
    static void link(final ComponentFactory factory, final TextArea area, final int start, final int end, final String text,
        final String current, final Runnable after) {
        final TextBox address = factory.textBox().setText(current);
        final Label error = factory.label(Text.of("Enter a web address (http or https).").color(RED));
        error.setVisible(false);
        final Overlay[] overlay = new Overlay[1];
        final Runnable save = () -> {
            final String typed = GuideText.stripped(address.text()).trim();
            if (GuideText.webAddress(typed) == null) {
                error.setVisible(true);
                return;
            }
            final String link = "[" + text + "](" + typed + ")";
            final String all = area.text();
            area.setText(all.substring(0, start) + link + all.substring(end));
            area.select(start + link.length(), start + link.length());
            overlay[0].close();
            after.run();
        };
        address.onEvent(TextBox.SUBMITTED, event -> save.run());
        final StackPanel column = column(factory)
            .add(stretch(factory.label(Text.of("Text: ").color(GRAY).append(MinecraftText.of(GuideEditor.fromEditor(text)))).setWrap(true)))
            .add(stretch(factory.label("Web address:"))).add(stretch(address))
            .add(stretch(error))
            .add(buttons(factory, save, () -> overlay[0].close()));
        overlay[0] = factory.overlays().dialog(window(factory, current.isEmpty() ? "Create Link" : "Edit Link", LINK_WIDTH).add(column));
    }

    /**
     * 3.5's question before removing a page.
     */
    static void remove(final ComponentFactory factory, final String id, final boolean playerGuide) {
        factory.overlays().dialog(factory.messageBox("Guide", "Do you wish to delete the selected guide?", MessageBox.Buttons.YES_NO)
            .onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    send(new ServerboundChange(ChangeType.REMOVE, id, 0, "", "", playerGuide));
                }
            }));
    }

    /**
     * A question whose answer goes to {@code answer}: Yes, No or Cancel.
     */
    static void ask(final ComponentFactory factory, final String message, final Consumer<MessageBox.Result> answer) {
        factory.overlays().dialog(factory.messageBox("Guide", message, MessageBox.Buttons.YES_NO_CANCEL)
            .onEvent(MessageBox.ANSWERED, event -> answer.accept(event.value())));
    }

    private static Window window(final ComponentFactory factory, final String title, final float width) {
        return factory.window(Text.of(title))
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .setWidth(width);
    }

    private static StackPanel column(final ComponentFactory factory) {
        final StackPanel column = factory.stackPanel().setSpacing(2f);
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        return column;
    }

    private static <C extends Component<C>> C stretch(final C component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    private static TextBox readOnly(final ComponentFactory factory, final String text) {
        return factory.textBox().setText(text).setReadOnly(true);
    }

    /**
     * A text box that drops what doesn't match, lower-cased if asked.
     */
    private static TextBox filtered(final TextBox box, final String drop, final int maxLength, final boolean lowerCase) {
        box.setMaxLength(maxLength);
        box.onEvent(TextInput.EDIT, event -> {
            final String value = lowerCase ? event.value().toLowerCase(Locale.ROOT) : event.value();
            event.setValue(value.replaceAll(drop, ""));
        });
        return box;
    }

    /**
     * Save and Cancel, at the bottom right.
     */
    private static StackPanel buttons(final ComponentFactory factory, final Runnable save, final Runnable cancel) {
        return factory.stackPanel(Orientation.HORIZONTAL).setSpacing(4f)
            .add(factory.button(Text.of("Save")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> save.run()))
            .add(factory.button(Text.of("Cancel")).setSize(BUTTON_WIDTH, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> cancel.run()))
            .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP).setMargin(0f, 4f, 0f, 0f);
    }
}
