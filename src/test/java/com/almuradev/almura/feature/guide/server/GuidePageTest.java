/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

/**
 * 3.5's guide pages read as they are.
 */
class GuidePageTest {

    @Test
    void readsA35Page() {
        final ServerGuide.Page page = ServerGuide.read("rules", """
            {
                "content" : "§aGeneral§f: be &cnice&f.\\nSee [the map](map.almuramc.com).",
                "created" : { "creator" : "bcbce24c-20fc-4914-8f49-5aaed0cd3696", "time" : "2019-01-02T03:04:05Z" },
                "index" : 4,
                "last_modified" : { "modifier" : "not a uuid", "time" : "2020-05-06T07:08:09Z" },
                "name" : "§9Welcome - Rules"
            }
            """);
        assertEquals("rules", page.id());
        assertEquals(4, page.index());
        assertEquals("§9Welcome - Rules", page.name());
        // & colour codes become § ones
        assertEquals("§aGeneral§f: be §cnice§f.\nSee [the map](map.almuramc.com).", page.content());
        assertEquals(UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696"), page.creator());
        assertEquals(Instant.parse("2019-01-02T03:04:05Z"), page.created());
        // A bad UUID is nobody, as on 3.5
        assertEquals(new UUID(0L, 0L), page.modifier());
    }

    @Test
    void readsAPageWithNothingButItsText() {
        final ServerGuide.Page page = ServerGuide.read("empty", "{ \"content\" : \"hi\" }");
        assertEquals("empty", page.name());
        assertEquals(0, page.index());
        assertEquals("hi", page.content());
    }

    @Test
    void savesAPageAs35DidAndReadsItBack() {
        final UUID dockter = UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696");
        final ServerGuide.Page page = new ServerGuide.Page("rules", 4, "§9Welcome - Rules", "§aBe §lnice§r.\nSee [the map](http://x.y).",
            dockter, Instant.parse("2019-01-02T03:04:05Z"), dockter, Instant.parse("2020-05-06T07:08:09Z"));
        final String written = ServerGuide.write(page);
        // 3.5 saved & colour codes
        assertTrue(written.contains("&aBe &lnice&r.") && !written.contains("§"), written);
        assertEquals(page, ServerGuide.read("rules", written));
    }

    @Test
    void namesAPlayersGuideFromItsName() {
        final UUID owner = UUID.fromString("bcbce24c-20fc-4914-8f49-5aaed0cd3696");
        // As 3.5: every character but a-z and 0-9 is a _, those at the ends dropped
        assertEquals("player/" + owner + "/my_farm__notes", ServerGuide.playerGuideId(owner, "§aMy Farm: Notes!"));
        assertEquals("player/" + owner + "/guide", ServerGuide.playerGuideId(owner, "§a!!!"));
        assertEquals(32, ServerGuide.playerGuideId(owner, "a".repeat(40)).length() - ("player/" + owner + "/").length());
    }
}
