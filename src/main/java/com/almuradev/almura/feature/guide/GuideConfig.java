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
