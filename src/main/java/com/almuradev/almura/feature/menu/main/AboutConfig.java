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

import java.util.Arrays;
import java.util.List;

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
            new Entry(
                    TextColors.BLUE,
                    "almura.menu.about.zidane.name",
                    "almura.menu.about.zidane.description",
                    "85271de5-8380-4db5-9f05-ada3b4aa785c",
                    "almura.menu.about.zidane.titles.1",
                    "almura.menu.about.zidane.titles.2",
                    "almura.menu.about.zidane.titles.3"
            ),
            // mcsnetworks
            new Entry(
                    TextColors.GOLD,
                    "almura.menu.about.dockter.name",
                    "almura.menu.about.dockter.description",
                    "bcbce24c-20fc-4914-8f49-5aaed0cd3696",
                    "almura.menu.about.dockter.titles.1",
                    "almura.menu.about.dockter.titles.2",
                    "almura.menu.about.dockter.titles.3"
            ),
            // NinjaGrinch
            new Entry(
                    TextColors.DARK_GREEN,
                    "almura.menu.about.grinch.name",
                    "almura.menu.about.grinch.description",
                    "7c104888-df99-4224-a8ba-2c4e15dbc777",
                    "almura.menu.about.grinch.titles.1",
                    "almura.menu.about.grinch.titles.2",
                    "almura.menu.about.grinch.titles.3"
            ),
            // Wifee
            new Entry(
                    TextColors.LIGHT_PURPLE,
                    "almura.menu.about.wifee.name",
                    "almura.menu.about.wifee.description",
                    "5f757396-8bc7-4dff-8b1f-37fd454a86b7",
                    "almura.menu.about.wifee.titles.1"
            ),
            // Wolfeyeamd0
            new Entry(
                    TextColors.RED,
                    "almura.menu.about.wolfeye.name",
                    "almura.menu.about.wolfeye.description",
                    "33f9598e-9890-4f76-90ff-12cd73ca1e3c",
                    "almura.menu.about.wolfeye.titles.1",
                    "almura.menu.about.wolfeye.titles.2"
            ),
            // Mumfrey
            new Entry(
                    TextColors.GRAY,
                    "almura.menu.about.mumfrey.name",
                    "almura.menu.about.mumfrey.description",
                    "e8e0361e-9b3b-481a-b06a-5c314a6c1ef0",
                    "almura.menu.about.mumfrey.titles.1",
                    "almura.menu.about.mumfrey.titles.2"
            ),
            // KratosKnox
            new Entry(
                    TextColors.DARK_RED,
                    "almura.menu.about.blood.name",
                    "almura.menu.about.blood.description",
                    "87caf570-b1fc-4100-bd95-3e7f1fa2e153",
                    "almura.menu.about.blood.titles.1",
                    "almura.menu.about.blood.titles.2"
            )
    );

    class Entry {

        public final TextColor color;
        public final String name;
        public final String description;
        final String uniqueId;
        final List<String> titles;

        Entry(TextColor color, String name, String description, String uniqueId, String... titles) {
            this.name = name;
            this.description = description;
            this.uniqueId = uniqueId;
            this.titles = Arrays.asList(titles);
            this.color = color;
        }
    }
}
