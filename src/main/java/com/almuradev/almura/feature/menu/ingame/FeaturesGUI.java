package com.almuradev.almura.feature.menu.ingame;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.exchange.ClientExchangeManager;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.nick.client.gui.NicknameGUI;
import com.almuradev.almura.feature.title.client.gui.TitleGUI;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class FeaturesGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel titleLabel;
    private UIButton guideButton, titleButton, nicknameButton, exchangeButton, accessoriesButton;

    private World world;
    private EntityPlayerSP player;

    @Inject private static PluginContainer container;
    @Inject private static ClientExchangeManager exchangeManager;
    @Inject private static ClientPageManager guideManager;

    public FeaturesGUI(EntityPlayerSP player, World worldIn) {
        this.player = player;
        this.world = worldIn;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Main Panel
        final UIFormContainer form = new UIFormContainer(this, 150, 200, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Almura Features");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Guide button
        guideButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Guide")
                .listener(this)
                .build("button.guide");

        // Nickname button
        nicknameButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Nicknames")
                .position(0, guideButton.getY() + 18)
                .listener(this)
                .build("button.nickname");

        // Nickname button
       titleButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Titles")
                .position(0, nicknameButton.getY() + 18)
                .listener(this)
                .build("button.title");

        // Exchange button
        exchangeButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, titleButton.getY() + 18)
                .text("Grand Exchange")
                .listener(this)
                .build("button.exchange");

        // Accessories button
       accessoriesButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, exchangeButton.getY() + 18)
                .text("Accessories")
                .enabled(false)
                .listener(this)
                .build("button.accessories");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        form.add(titleLabel, guideButton, exchangeButton, titleButton, nicknameButton, accessoriesButton, buttonClose);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.exchange":
                exchangeManager.requestExchangeGUI();
                break;

            case "button.guide":
                guideManager.requestGuideGUI();
                break;

            case "button.nickname":
                new NicknameGUI(player).display();
                break;

            case "button.title":
                new TitleGUI(player).display();
                break;

            case "button.close":
                close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {}
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}

