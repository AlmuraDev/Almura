/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.core.client.config.ClientConfiguration;
import com.almuradev.almura.feature.hud.screen.AbstractHUD;
import com.almuradev.almura.feature.hud.screen.origin.OriginHUD;
import com.almuradev.shared.config.ConfigurationAdapter;
import com.almuradev.shared.event.Witness;
import com.google.inject.Injector;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClientHeadUpDisplay implements Witness {

    private final Injector injector;
    private final ConfigurationAdapter<ClientConfiguration> config;
    @Nullable private AbstractHUD hud;

    @Inject
    public ClientHeadUpDisplay(final Injector injector, final ConfigurationAdapter<ClientConfiguration> config) {
        this.injector = injector;
        this.config = config;
    }

    @SubscribeEvent
    public void gameOverlayRender(final RenderGameOverlayEvent.Pre event) {
        switch (this.config.getConfig().client.hud.toLowerCase()) {
            case HUDType.ORIGIN:
                if (!(this.hud instanceof OriginHUD)) {
                    if (this.hud != null) {
                        this.hud.closeOverlay();
                    }
                    this.hud = this.injector.getInstance(OriginHUD.class);
                    this.hud.displayOverlay();
                }
                switch (event.getType()) {
                    case AIR:
                    case ARMOR:
                    case BOSSHEALTH:
                    case BOSSINFO:
                    case DEBUG:
                    case EXPERIENCE:
                    case FOOD:
                    case HEALTH:
                    case HEALTHMOUNT:
                    case PLAYER_LIST:
                        event.setCanceled(true);
                        break;
                    default:
                }
                break;
            case HUDType.VANILLA:
            default:
                if (this.hud != null) {
                    this.hud.closeOverlay();
                    this.hud = null;
                }
                break;
        }
    }

    @SubscribeEvent
    public void mouse(final MouseEvent event) {
        if (this.hud != null && this.hud instanceof OriginHUD) {
            event.setCanceled(((OriginHUD) this.hud).handleScroll());
        }
    }

    public Optional<AbstractHUD> getHUD() {
        return Optional.ofNullable(this.hud);
    }
}
