/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface AboutConfig {

    Text TITLE = Text.of(TextColors.WHITE, "Almura");
    Text STORY = Text.of(
            TextColors.WHITE, "Almura was created and is maintained by the AlmuraDev Team (https://www.github.com/AlmuraDev/)",
            Text.NEW_LINE, Text.NEW_LINE,
            TextStyles.BOLD, "The Beginning...",
            TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
            "Almura 1.0 was conceived on June 1st, 2011. It was built around Spoutcraft (1.7.3 beta). As the technology changed we were",
            " forced to abandon the Spoutcraft ecosystem in favor of the Forge one. ", TextColors.AQUA, "Zidane", TextColors.RESET,
            " our lead developer for AlmuraDev finally put his foot down and said enough is enough which began our six ",
            "month journey to migrate the features we had relied on in Spoutcraft to our new client.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura 2.0 - Rediscovered was initially conceived on September 4th, 2014.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura has a number of changes from a typical Minecraft setup that gives it an extremely unique and customizable "
                    + "experience.",
            Text.NEW_LINE,
            "  • ASM through Mixin.", Text.NEW_LINE,
            "  • YAML/JSON content loading system for items, blocks, crops, trees and more.", Text.NEW_LINE,
            "  • Client/Server security system.", Text.NEW_LINE,
            "  • Customized GUI for a more unique experience.", Text.NEW_LINE,
            "  • Information guide system displayed in-game.", Text.NEW_LINE,
            "  • Player accessory system for hats, wings, capes, earrings and more.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura 2.5 - Origins was introduced in October of 2016", Text.NEW_LINE,
            "  • Removed some server conflicting mods and started a world cleanup.", Text.NEW_LINE,
            "  • Added SGcraft and CustomFurniture Mods", Text.NEW_LINE,
            "  • Introduced a custom cache system. ", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura 3.0 Resurrected had its first alpha build on 9/19/2017. ", Text.NEW_LINE,
            "  • Brand new server concept running on SpongeForge w/Minecraft 1.12.1 and above. ", Text.NEW_LINE,
            "  • New Plugins, new player HUD, new everything! ", Text.NEW_LINE,
            " ");
    List<Entry> ENTRIES = ImmutableList.of(
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

    class Entry {

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
