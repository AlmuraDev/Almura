/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.death.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.bank.BankMoney;
import com.almuradev.almura.feature.death.network.DeathMessages.ClientboundDied;
import com.almuradev.almura.feature.menu.AlmuraScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.screen.ScreenHandle;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.text.TextStyle;
import org.inspirenxe.strata.api.theme.SpriteId;
import org.inspirenxe.strata.minecraft.MinecraftText;
import org.inspirenxe.strata.minecraft.StrataScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Almura's death screen, as 3.5's {@code PlayerDiedGUI}: a "You have died." window with the fallen Steve, one of 3.5's remarks, vanilla's
 * death message under it, what the player dropped and what they lost to death taxes, and Revive (with {@code almura.death.revive}), Respawn
 * and Quit. It doesn't close with Escape, as vanilla's.
 */
final class PlayerDiedScreen implements Screen {

    private static final SpriteId DEAD_STEVE = SpriteId.of(Almura.ID, "dead_steve");
    private static final float WIDTH = 220f, STEVE = 59f, BUTTON_WIDTH = 50f, BUTTON_HEIGHT = 20f;
    private static final int WHITE = 0xFFFFFFFF, GREY = 0xFFAAAAAA, GOLD = 0xFFFFAA00, RED = 0xFFFF5555;

    private final ClientboundDied died;
    private final String remark;
    private final List<Button> buttons = new ArrayList<>();

    PlayerDiedScreen(final ClientboundDied died, final String remark) {
        this.died = died;
        this.remark = remark;
    }

    /**
     * The screen as the game shows it: not pausing (the server keeps going), over vanilla's red death backdrop.
     */
    net.minecraft.client.gui.screens.Screen host() {
        return new Host(this);
    }

    @Override
    public Text title() {
        return Text.of("You have died.");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Image steve = factory.image(DEAD_STEVE).setImageSize(STEVE, STEVE).setScaling(Image.Scaling.FIT);
        steve.setSize(STEVE, STEVE);

        final StackPanel column = factory.stackPanel()
            .setSpacing(3f)
            .add(centred(steve).setMargin(0f, 2f, 0f, 2f))
            .add(stretched(wrapped(factory.label(Text.of(this.remark).color(WHITE)).setTextScale(1.1f))));
        if (!this.died.cause().getString().isEmpty()) {
            column.add(stretched(wrapped(factory.label(MinecraftText.of(this.died.cause())).setTextScale(0.8f)
                .setStyle(TextStyle.DEFAULT.withColor(GREY).withShadow(true)))));
        }
        if (this.died.showMoney()) {
            column.add(stretched(factory.separator()));
            column.add(centred(factory.label(Text.of("You dropped: ").color(WHITE)
                .append(Text.of("$" + BankMoney.format(BankMoney.parse(this.died.dropped()))).color(GOLD))
                .append(Text.of(".").color(WHITE)))));
            column.add(centred(factory.label(Text.of("You lost: ").color(WHITE)
                .append(Text.of("$" + BankMoney.format(BankMoney.parse(this.died.lost()))).color(RED))
                .append(Text.of(" to death taxes.").color(WHITE)))));
        }
        column.add(stretched(factory.separator()));

        final Button revive = this.button(factory, Text.of("Revive"), PlayerDiedScreen::revive);
        revive.setEnabled(this.died.canRevive());
        if (!this.died.canRevive()) {
            revive.setTooltip(Text.of("Needs almura.death.revive"));
        }
        final StackPanel row = factory.stackPanel(Orientation.HORIZONTAL)
            .setSpacing(4f)
            .add(revive)
            .add(this.button(factory, Text.of("Respawn"), PlayerDiedScreen::respawn))
            .add(this.button(factory, Text.of("Quit").color(RED), PlayerDiedScreen::quit));
        // As far below the line above as the window's edge is below it
        column.add(centred(row).setMargin(0f, -1f, 0f, 0f));

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

    @Override
    public boolean closeRequested(final ScreenHandle handle) {
        // Only by respawning, reviving or quitting, as vanilla's
        return false;
    }

    private Button button(final ComponentFactory factory, final Text text, final Runnable press) {
        final Button button = factory.button(text).setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        button.onEvent(Button.PRESSED, event -> {
            // One choice: the screen closes once the server has respawned the player
            this.buttons.forEach(each -> each.setEnabled(false));
            press.run();
        });
        this.buttons.add(button);
        return button;
    }

    /**
     * Back where they died: the server is asked first, so it knows when the respawn comes.
     */
    private static void revive() {
        ClientDeath.requestRevive();
        respawn();
    }

    private static void respawn() {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.respawn();
        }
    }

    /**
     * 3.5's Rage Quit: leaves the world for the title screen, as the game menu's Quit does; the player is still dead there when they come
     * back.
     */
    private static void quit() {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getReportingContext().draftReportHandled(minecraft, minecraft.gui.screen(), () -> {
            minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
            if (!(minecraft.gui.screen() instanceof TitleScreen)) {
                minecraft.gui.setScreen(new TitleScreen());
            }
        }, true);
    }

    private static Label wrapped(final Label label) {
        return label.setWrap(true).setTextAlignment(HorizontalAlignment.CENTER);
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }

    private static <T extends Component<T>> T stretched(final T component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    static final class Host extends StrataScreen {

        // Vanilla's death backdrop
        private static final int TOP = 0x60500000, BOTTOM = 0xA0803030;

        private Host(final PlayerDiedScreen screen) {
            super(net.minecraft.network.chat.Component.translatable("deathScreen.title"), screen, AlmuraScreens.IN_GAME_THEME_REF);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public boolean isAllowedInPortal() {
            return true;
        }

        @Override
        public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
            graphics.fillGradient(0, 0, this.width, this.height, TOP, BOTTOM);
        }
    }
}
