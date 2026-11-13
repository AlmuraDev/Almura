/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.menu.figure.FigureSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.ScrollPanel;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The About page, opened from the title screen, as on 1.12: Almura and the people behind it in a list on the left, the selected one's story
 * or description and titles beside it, Dockter_Almura beside the story, the versions playing in the bottom left and Done at the bottom.
 */
final class AboutScreen implements Screen {

    private static final SpriteId ALMURA_MAN = SpriteId.of(Almura.ID, "almura_man");
    private static final String STORY = "/assets/almura/text/about/story.txt";

    // 1.12's sizes
    private static final float LIST_WIDTH = 125f, ROW_HEIGHT = 38f, FACE = 32f, MAN_WIDTH = 23f, MAN_HEIGHT = 32f, GAP = 4f;
    private static final float TOP = 30f, BOTTOM = 40f, FIGURE_MAX_HEIGHT = 230f, FIGURE_GAP = 8f, DONE_WIDTH = 98f, DONE_HEIGHT = 20f;
    private static final int WHITE = 0xFFFFFFFF;

    // Almura's colours for each person, as 1.12's text colours
    private static final int BLUE = 0xFF5555FF, GRAY = 0xFFAAAAAA, GOLD = 0xFFFFAA00, DARK_GREEN = 0xFF00AA00, LIGHT_PURPLE = 0xFFFF55FF,
        DARK_RED = 0xFFAA0000;

    private static final List<Person> PEOPLE = List.of(
        new Person("zidane", UUID.fromString("85271de5-8380-4db5-9f05-ada3b4aa785c"), BLUE, 3),
        new Person("kashike", UUID.fromString("eb121687-8b1a-4944-bd4d-e0a818d9dfe2"), GRAY, 2),
        new Person("dockter", UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696"), GOLD, 3),
        new Person("grinch", UUID.fromString("7c104888-df99-4224-a8ba-2c4e15dbc777"), DARK_GREEN, 3),
        new Person("wifee", UUID.fromString("5f757396-8bc7-4dff-8b1f-37fd454a86b7"), LIGHT_PURPLE, 1),
        new Person("mumfrey", UUID.fromString("e8e0361e-9b3b-481a-b06a-5c314a6c1ef0"), GRAY, 2),
        new Person("blood", UUID.fromString("87caf570-b1fc-4100-bd95-3e7f1fa2e153"), DARK_RED, 2));

    private @Nullable Label content;
    private @Nullable ScrollPanel contentScroll;
    private @Nullable Component<?> figure;

    @Override
    public Text title() {
        return Text.translatable("almura.menu_button.about");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final List<Entry> entries = new ArrayList<>();
        final Entry story = new Entry(null, Text.of("Almura").color(WHITE), MinecraftText.of(story()));
        entries.add(story);
        for (final Person person : PEOPLE) {
            entries.add(new Entry(person, Text.translatable(person.key("name")).color(person.color), person.details()));
        }

        final ListView<Entry> list = factory.<Entry>listView(entry -> row(factory, entry));
        list.setRowHeight(ROW_HEIGHT);
        list.setItems(entries);
        list.setWidth(LIST_WIDTH);
        list.onEvent(Selection.SELECTED, event -> list.selected().ifPresent(this::show));

        this.content = factory.label("").setWrap(true);
        this.contentScroll = factory.scrollPanel();
        this.contentScroll.add(this.content.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));

        // Dockter_Almura beside the story, as tall as the page allows up to 1.12's size
        final float pageHeight = Math.max(0f, Minecraft.getInstance().getWindow().getGuiScaledHeight() - TOP - BOTTOM);
        final float figureHeight = Math.min(FIGURE_MAX_HEIGHT, pageHeight - 4f);
        final float figureWidth = (float) Math.ceil(figureHeight * 0.62f);
        this.figure = FigureSlot.create(factory, figureHeight, 0f);

        final Label title = factory.label(this.title());
        final Button done = factory.button(Text.translatable("gui.done"))
            .setSize(DONE_WIDTH, DONE_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        final AnchorPanel page = factory.anchorPanel()
            .add(title.setAnchor(Anchor.TOP_CENTER).setMargin(0f, 10f, 0f, 0f))
            .add(list.setAnchor(Anchor.FILL).setMargin(GAP, TOP, 0f, BOTTOM).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH))
            .add(this.contentScroll.setAnchor(Anchor.FILL).setMargin(GAP + LIST_WIDTH + GAP, TOP, GAP + figureWidth + FIGURE_GAP, BOTTOM))
            .add(this.figure.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, TOP, GAP, 0f))
            .add(versions(factory).setAnchor(Anchor.BOTTOM_LEFT).setMargin(GAP, 0f, 0f, GAP))
            .add(done.setAnchor(Anchor.BOTTOM_CENTER).setMargin(0f, 0f, 0f, 15f));

        list.selectItem(story);
        this.show(story);
        return page.setAnchor(Anchor.FILL);
    }

    private void show(final Entry entry) {
        if (this.content == null || this.contentScroll == null || this.figure == null) {
            return;
        }
        this.content.setText(entry.content);
        this.contentScroll.setScroll(0f, 0f);
        this.figure.setVisible(entry.person == null);
    }

    /**
     * A list row: Almura's man, or the person's face, then the name.
     */
    private static Component<?> row(final ComponentFactory factory, final Entry entry) {
        final Image image;
        final float padding;
        if (entry.person == null) {
            image = factory.image(ALMURA_MAN).setImageSize(MAN_WIDTH, MAN_HEIGHT).setScaling(Image.Scaling.FIT);
            image.setSize(MAN_WIDTH, MAN_HEIGHT).setMargin(5f, 0f, 0f, 0f);
            padding = 8f;
        } else {
            image = factory.image(MinecraftVisuals.playerFace(ResolvableProfile.createUnresolved(entry.person.uniqueId)));
            image.setImageSize(FACE, FACE).setSize(FACE, FACE).setMargin(2f, 0f, 0f, 0f);
            padding = 4f;
        }
        final StackPanel line = factory.stackPanel(org.inspirenxe.strata.api.layout.Orientation.HORIZONTAL).setSpacing(padding)
            .add(image.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE))
            .add(factory.label(entry.title).setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE));
        // A press anywhere on the row selects it, the face and name included
        line.setHitTestVisible(false);
        return factory.anchorPanel().add(line.setAnchor(Anchor.MIDDLE_LEFT)).setAnchor(Anchor.FILL);
    }

    /**
     * The versions playing, in the bottom left: Sponge's when it is installed, NeoForge's and Almura's.
     */
    private static StackPanel versions(final ComponentFactory factory) {
        final StackPanel lines = factory.stackPanel().setSpacing(1f);
        final String sponge = version("sponge");
        if (!sponge.isEmpty()) {
            lines.add(factory.label(Text.of("Sponge: " + sponge).color(WHITE)));
        }
        lines.add(factory.label(Text.of("NeoForge: " + version("neoforge")).color(WHITE)));
        lines.add(factory.label(Text.of("Almura 4.0 - " + version(Almura.ID)).color(WHITE)));
        return lines;
    }

    private static String version(final String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(info -> info.getVersion().toString()).orElse("");
    }

    /**
     * Almura's story, from the jar, with its &amp; colour codes.
     */
    private static String story() {
        try (InputStream in = AboutScreen.class.getResourceAsStream(STORY)) {
            if (in == null) {
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r", "").replace('&', '§');
        } catch (final IOException e) {
            return "";
        }
    }

    private record Person(String id, UUID uniqueId, int color, int titles) {

        String key(final String part) {
            return "almura.menu.about.person." + this.id + "." + part;
        }

        /**
         * The description, then the person's titles under a bold heading.
         */
        Text details() {
            final List<Text> parts = new ArrayList<>();
            parts.add(Text.translatable(this.key("description")).color(WHITE));
            parts.add(Text.of("\n\n"));
            parts.add(Text.translatable("almura.menu.about.titles").color(WHITE).bold());
            for (int i = 0; i < this.titles; i++) {
                parts.add(Text.of("\n  • ").color(WHITE));
                parts.add(Text.translatable(this.key("titles." + i)).color(WHITE));
            }
            return Text.concat(parts);
        }
    }

    private record Entry(@Nullable Person person, Text title, Text content) {
    }
}
