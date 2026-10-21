/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

public interface GuideConfig {

    String DIR_GUIDE = "guide";

    String DIR_GUIDE_PAGES = "pages";

    // Player guides: guide/player_pages/<owner uuid>/<id>.conf, known in game as player/<owner uuid>/<id>
    String DIR_GUIDE_PLAYER_PAGES = "player_pages";

    String PLAYER_ID_PREFIX = "player/";

    String EXT_CONFIG = ".conf";

    String EXT_CONFIG_PAGE = ".conf";

    String NAME = "name";

    String TITLE = "title";

    String CONTENT = "content";

    String INDEX = "index";

    interface LastModified {

        String LAST_MODIFIED = "last_modified";

        String MODIFIER = "modifier";

        String TIME = "time";
    }

    interface Created {

        String CREATED = "created";

        String CREATOR = "creator";

        String TIME = "time";
    }
}
