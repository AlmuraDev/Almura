/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AboutConfig {

    public static final Text TITLE = Text.of(TextColors.WHITE, "Almura");
    public static final Text STORY = story();
    public static final List<Entry> ENTRIES = ImmutableList.of(
            // NinjaZidane
            new Entry("zidane", UUID.fromString("85271de5-8380-4db5-9f05-ada3b4aa785c"), TextColors.BLUE,3),
            // Kashike
            new Entry("kashike", UUID.fromString("eb121687-8b1a-4944-bd4d-e0a818d9dfe2"), TextColors.GRAY, 2),
            // Dockter
            new Entry("dockter", UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696"), TextColors.GOLD, 3),
            // NinjaGrinch
            new Entry("grinch", UUID.fromString("7c104888-df99-4224-a8ba-2c4e15dbc777"), TextColors.DARK_GREEN, 3),
            // Wifee
            new Entry("wifee", UUID.fromString("5f757396-8bc7-4dff-8b1f-37fd454a86b7"), TextColors.LIGHT_PURPLE, 1),
            // Wolfeyeamd0
            new Entry("chimwolfeye", UUID.fromString("fa1ee43f-8949-41c6-ab61-e50bd864943a"), TextColors.RED, 2),
            // Mumfrey
            new Entry("mumfrey", UUID.fromString("e8e0361e-9b3b-481a-b06a-5c314a6c1ef0"), TextColors.GRAY, 2),
            // KratosKnox
            new Entry("blood", UUID.fromString("87caf570-b1fc-4100-bd95-3e7f1fa2e153"), TextColors.DARK_RED, 2)
    );

    private static Text story() {
        return TextSerializers.FORMATTING_CODE.deserialize(Joiner.on('\n').join(storyLines()));
    }

    private static List<String> storyLines() {
        try {
            return Resources.readLines(AboutConfig.class.getResource("/assets/almura/text/about/story.txt"), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return Collections.emptyList();
        }
    }

    public static final class Entry {

        public final String name;
        final UUID uniqueId;
        public final String description;
        public final TextColor color;
        final List<String> titles;

        Entry(final String id, final UUID uniqueId, final TextColor color, final int titles) {
            this.name = String.format("almura.menu.about.person.%s.name", id);
            this.uniqueId = uniqueId;
            this.description = String.format("almura.menu.about.person.%s.description", id);
            this.color = color;
            if (titles > 0) {
                this.titles = IntStream.range(0, titles)
                        .mapToObj(value -> String.format("almura.menu.about.person.%s.titles.%d", id, value))
                        .collect(Collectors.toList());
            } else {
                this.titles = Collections.emptyList();
            }
        }
    }
}
