/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.almuradev.almura.client.gui.util.builders.FontRenderOptionsBuilder;
import com.almuradev.almura.client.gui.util.builders.UIButtonBuilder;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.api.util.Color;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public class DynamicMainMenu extends SimpleGui {

    public DynamicMainMenu(SimpleGui parent){
        super(parent);
    }

    @Override
    public void construct() {
        final UIBackgroundContainer container = new UIBackgroundContainer(this);
        container.setBackgroundAlpha(0);

        final int longButtonWidth = 200;
        final int shortButtonWidth = 98;
        final int buttonHeight = 20;
        final int buttonPadding = 4;

        // Almura Image
        final UIImage almuraImage = new UIImage(this, new GuiTexture(ALMURA_LOGO_LOCATION), null);
        almuraImage.setPosition(0, 20, Anchor.TOP | Anchor.CENTER);
        almuraImage.setSize(228, 70);

        final UIButton singleplayerButton = new UIButtonBuilder(this)
                .text("Singleplayer")
                .name("button.singleplayer")
                .size(longButtonWidth, buttonHeight)
                .position(0, SimpleGui.getPaddedY(almuraImage, 10))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();

        final UIButton multiplayerButton = new UIButtonBuilder(this)
                .text("Multiplayer")
                .name("button.multiplayer")
                .size(longButtonWidth, buttonHeight)
                .position(0, SimpleGui.getPaddedY(singleplayerButton, buttonPadding))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();


        final UIButton optionsButton = new UIButtonBuilder(this)
                .text("Options")
                .name("button.options")
                .size(shortButtonWidth, buttonHeight)
                .position(-((shortButtonWidth / 2) + (buttonPadding / 2)), SimpleGui.getPaddedY(multiplayerButton, buttonPadding))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();


        final UIButton aboutButton = new UIButtonBuilder(this)
                .text("About")
                .name("button.about")
                .size(shortButtonWidth, buttonHeight)
                .position(SimpleGui.getPaddedX(optionsButton, buttonPadding), SimpleGui.getPaddedY(multiplayerButton, buttonPadding))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();

        final UIButton quitButton = new UIButtonBuilder(this)
                .text("Quit")
                .name("button.quit")
                .fro(FontRenderOptionsBuilder.builder().from(FontRenderOptionsConstants.FRO_COLOR_RED).shadow(true).build())
                .hoverFro(FontRenderOptionsBuilder.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(longButtonWidth, buttonHeight)
                .position(singleplayerButton.getX(), SimpleGui.getPaddedY(optionsButton, buttonPadding))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build();

        final UIButton forumButton = new UIButtonBuilder(this)
                .name("button.forums")
                .image(SimpleGui.ICON_FORUM)
                .size(24, 24)
                .position(-4, -4)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(new UITooltip(this, "Forums", 15))
                .build();

        final UIButton issuesButton = new UIButtonBuilder(this)
                .name("button.issues")
                .image(SimpleGui.ICON_GITHUB)
                .size(24, 24)
                .position(SimpleGui.getPaddedX(forumButton, 4, Anchor.RIGHT), forumButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(new UITooltip(this, "Issues", 15))
                .build();

        // Trademark
        final UILabel trademarkLabel = new UILabel(this, TextFormatting.YELLOW + "Minecraft is a registered trademark of Mojang AB");
        trademarkLabel.setPosition(4, -4, Anchor.BOTTOM | Anchor.LEFT);

        // Copyright
        final UILabel copyrightLabel = new UILabel(this, TextFormatting.YELLOW + "Copyright AlmuraDev 2012 - 2016");
        copyrightLabel.setPosition(trademarkLabel.getX(), SimpleGui.getPaddedY(trademarkLabel, 4, Anchor.BOTTOM), trademarkLabel.getAnchor());

        // Add content to panel strip
        container.add(almuraImage, singleplayerButton, multiplayerButton, optionsButton, aboutButton, quitButton, trademarkLabel, copyrightLabel,
                forumButton, issuesButton);

        // Disable escape keypress
        registerKeyListener((keyChar, keyCode) -> {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                new DynamicMainMenu(null).display();
                return true;
            }
            return false;
        });

        // Add content to screen
        addToScreen(new UIAnimatedBackground(this));
        addToScreen(container);
    }

    @Override
    public void onClose() {
        mc.shutdown();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws URISyntaxException, IOException {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.singleplayer":
                mc.displayGuiScreen(new GuiWorldSelection(this));
                break;
            case "button.multiplayer":
                new DynamicServerMenu(this).display();
                break;
            case "button.options":
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
                break;
            case "button.about":
                new DynamicAboutMenu(this).display();
                break;
            case "button.quit":
                close();
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI("http://almuramc.com"));
                break;
            case "button.issues":
                Desktop.getDesktop().browse(new URI("https://github.com/AlmuraDev/Almura/issues"));
                break;
        }
    }
}
