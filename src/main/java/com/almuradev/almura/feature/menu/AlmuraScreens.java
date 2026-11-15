/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import com.almuradev.almura.Almura;
import org.inspirenxe.strata.api.Strata;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.theme.ThemeId;
import org.inspirenxe.strata.api.theme.ThemeRef;

/**
 * Opens Almura's screens in Almura's theme. The theme is {@code assets/almura/strata/themes/almura.json}: vanilla's look, with Almura's dark
 * see-through windows with a light border and a grey title bar, as on 1.12. Resource packs can restyle it with a file at the same path.
 */
public final class AlmuraScreens {

    public static final ThemeId THEME = ThemeId.of(Almura.ID, "almura");
    public static final ThemeRef THEME_REF = ThemeRef.of(THEME);
    /**
     * The size of a checkbox's box: as tall as its text, as 3.5's small ones (Strata's is the text's height plus 8).
     */
    public static final float CHECKBOX_SIZE = 9f;
    /**
     * The in-game windows' theme ({@code assets/almura/strata/themes/in_game.json}): Almura's, with list rows as wide as their list, as
     * 3.5's were (vanilla's centres them, at most 220 wide, as its server and world lists).
     */
    public static final ThemeRef IN_GAME_THEME_REF = ThemeRef.of(ThemeId.of(Almura.ID, "in_game"));

    private AlmuraScreens() {
    }

    /**
     * Opens a screen in Almura's theme, from any thread.
     */
    public static ScreenHandle open(final Screen screen) {
        return Strata.screens().open(screen, THEME_REF);
    }
}
