/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;
import org.inspirenxe.strata.minecraft.StrataScreen;

/**
 * The vanilla screen that shows Almura's world loading screen in place of vanilla's {@link LevelLoadingScreen}, keeping vanilla's screen
 * going behind it:
 * <ul>
 *     <li>it doesn't pause the game, as vanilla's doesn't: opening a world on this computer, a paused game would never finish loading it;</li>
 *     <li>vanilla's screen is ticked, and closes once the world is ready, which closes this one;</li>
 *     <li>vanilla's screen draws the background: the title screen's panorama, or a portal's when going through one.</li>
 * </ul>
 */
final class WorldLoadingHost extends StrataScreen {

    private LevelLoadingScreen loading;

    WorldLoadingHost(final LevelLoadingScreen loading) {
        this(loading, new WorldLoadingScreen[1]);
    }

    // The Strata screen needs the host for vanilla's progress, and the host the Strata screen to show
    private WorldLoadingHost(final LevelLoadingScreen loading, final WorldLoadingScreen[] content) {
        super(Component.translatable("multiplayer.downloadingTerrain"), content[0] = new WorldLoadingScreen(), AlmuraScreens.THEME_REF);
        this.loading = loading;
        content[0].setHost(this);
    }

    LevelLoadingScreen loading() {
        return this.loading;
    }

    /**
     * Carries on with a new vanilla screen: joining a world, vanilla starts another once the server says which world to load (it would have
     * reused its own, but this isn't one).
     */
    void setLoading(final LevelLoadingScreen loading) {
        this.loading = loading;
        this.loading.init(this.width, this.height);
    }

    @Override
    protected void init() {
        super.init();
        // Sized like this screen, for the background it draws
        this.loading.init(this.width, this.height);
    }

    @Override
    public void tick() {
        super.tick();
        this.loading.tick();
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        this.loading.extractBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
