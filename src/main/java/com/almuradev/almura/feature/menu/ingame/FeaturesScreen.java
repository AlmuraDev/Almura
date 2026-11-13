/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.notification.history.NotificationHistoryScreen;
import com.almuradev.almura.feature.skills.client.ClientSkills;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;

/**
 * The Almura Features window, as on 1.12: a button for each of Almura's features (Skills only where the server runs skills), and for
 * administrators the exchanges and stores to manage, with Close in the bottom right.
 *
 * <p>The screens the buttons open are ported one at a time; until a feature's is, its button is greyed out and says so.</p>
 */
final class FeaturesScreen implements Screen {

    private static final float WIDTH = 150f, BUTTON_WIDTH = 100f, ADMIN_BUTTON_WIDTH = 125f, CLOSE_WIDTH = 40f, BUTTON_HEIGHT = 16f;
    private static final float BUTTON_GAP = 2f, SECTION_GAP = 4f;

    private final boolean admin;

    FeaturesScreen(final boolean admin) {
        this.admin = admin;
    }

    @Override
    public Text title() {
        return Text.of("Almura Features");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final StackPanel column = factory.stackPanel().setSpacing(BUTTON_GAP)
            .add(later(factory, "Guide", BUTTON_WIDTH))
            .add(later(factory, "Claim Management", BUTTON_WIDTH))
            .add(later(factory, "Nicknames", BUTTON_WIDTH))
            .add(later(factory, "Titles", BUTTON_WIDTH))
            .add(later(factory, "Accessories", BUTTON_WIDTH))
            .add(later(factory, "Memberships", BUTTON_WIDTH))
            .add(later(factory, "Banking", BUTTON_WIDTH));
        // The server only sends skills when it runs the Skills plugin
        if (!ClientSkills.skills().isEmpty()) {
            column.add(later(factory, "Skills", BUTTON_WIDTH));
        }
        // What the player was sent, over this menu
        column.add(centred(factory.button(Text.of("Notifications"))
            .setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> InGameScreens.open(new NotificationHistoryScreen(), Minecraft.getInstance().gui.screen()))));

        if (this.admin) {
            column
                .add(stretched(factory.separator()).setMargin(0f, SECTION_GAP, 0f, 0f))
                .add(centred(factory.label("Administrator").setTextScale(1.1f)))
                .add(stretched(factory.separator()))
                .add(later(factory, "Manage Exchanges", ADMIN_BUTTON_WIDTH).setMargin(0f, SECTION_GAP, 0f, 0f))
                .add(later(factory, "Manage Stores", ADMIN_BUTTON_WIDTH));
        }

        final Button close = factory.button(Text.translatable("almura.button.close"))
            .setSize(CLOSE_WIDTH, BUTTON_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());
        // In the bottom right, as far below the buttons as the window's edge is below it
        column.add(close.setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP).setMargin(0f, SECTION_GAP, 0f, 0f));

        final Window window = factory.window(this.title())
            .setClosable(false)
            .setMovable(false)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(stretched(column))
            .setWidth(WIDTH)
            .setAnchor(Anchor.CENTER);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    /**
     * A feature's button, greyed out until its screen is ported.
     */
    private static Button later(final ComponentFactory factory, final String name, final float width) {
        final Button button = factory.button(Text.of(name))
            .setSize(width, BUTTON_HEIGHT)
            .setEnabled(false)
            .setTooltip(Text.translatable("almura.features.later"));
        return centred(button);
    }

    private static <C extends Component<C>> C centred(final C component) {
        component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
        return component;
    }

    private static <C extends Component<C>> C stretched(final C component) {
        component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        return component;
    }
}
