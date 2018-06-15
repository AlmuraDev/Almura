/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.feature.menu.multiplayer.ServerMenu;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.PanoramicScreen;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class PanoramicMainMenu extends PanoramicScreen {

    private static final int PADDING = 4;

    private UIBackgroundContainer buttonContainer;

    public PanoramicMainMenu(@Nullable SimpleScreen parent) {
        super(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        final UIBackgroundContainer container = new UIBackgroundContainer(this);
        container.setBackgroundAlpha(0);
        container.setPosition(0, -10, Anchor.MIDDLE | Anchor.CENTER);
        container.setSize(GuiConfig.Button.WIDTH_LONG, 205);

        // Almura header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        this.buttonContainer = new UIBackgroundContainer(this, GuiConfig.Button.WIDTH_LONG + PADDING, (GuiConfig.Button.HEIGHT * 4) +
                (PADDING * 3));
        this.buttonContainer.setPosition(0, SimpleScreen.getPaddedY(almuraHeader, 10), Anchor.TOP | Anchor.CENTER);
        this.buttonContainer.setBackgroundAlpha(0);

        final UIButton singleplayerButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("menu.singleplayer")))
                .size(GuiConfig.Button.WIDTH_LONG, GuiConfig.Button.HEIGHT)
                .position(0, 0)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.singleplayer");

        final UIButton multiplayerButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("menu.multiplayer")))
                .size(GuiConfig.Button.WIDTH_LONG, GuiConfig.Button.HEIGHT)
                .position(0, SimpleScreen.getPaddedY(singleplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.multiplayer");

        final UIButton optionsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("options.title")))
                .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT)
                .position(-68, SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.options");

        final UIButton modsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("almura.menu_button.mods")))
                .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT)
                .position(SimpleScreen.getPaddedX(optionsButton, PADDING), SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.mods");

        final UIButton aboutButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("almura.menu_button.about")))
                .size(GuiConfig.Button.WIDTH_TINY, GuiConfig.Button.HEIGHT)
                .position(SimpleScreen.getPaddedX(modsButton, PADDING), SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.about");

        final UIButton quitButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of(I18n.format("almura.menu_button.quit")))
                .fro(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
                .hoverFro(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(GuiConfig.Button.WIDTH_LONG, GuiConfig.Button.HEIGHT)
                .position(singleplayerButton.getX(), SimpleScreen.getPaddedY(optionsButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.quit");

        final UIButton forumsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.ENJIN)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(-PADDING, -PADDING)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of(I18n.format("almura.menu_button.forums")))
                .build("button.forums");

        final UIButton issuesButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.FA_GITHUB)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(forumsButton, PADDING, Anchor.RIGHT), forumsButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of(I18n.format(I18n.format("almura.menu_button.issues"))))
                .build("button.issues");

        final UIButton shopButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(GuiConfig.Icon.FA_SHOPPING_BAG)
                .size(GuiConfig.Button.WIDTH_ICON, GuiConfig.Button.HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(issuesButton, PADDING, Anchor.RIGHT), issuesButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of(I18n.format("almura.menu_button.shop")))
                .build("button.shop");

        final UILabel trademarkLabel = new UILabel(this, TextFormatting.YELLOW + I18n.format("almura.menu.main.trademark"));
        trademarkLabel.setPosition(PADDING, -PADDING, Anchor.BOTTOM | Anchor.LEFT);

        final UILabel copyrightLabel = new UILabel(this, TextFormatting.YELLOW + I18n.format("almura.menu.main.copyright"));
        copyrightLabel
                .setPosition(trademarkLabel.getX(), SimpleScreen.getPaddedY(trademarkLabel, PADDING, Anchor.BOTTOM), trademarkLabel.getAnchor());

        container.add(almuraHeader, this.buttonContainer);

        // Disable escape key press
        registerKeyListener((keyChar, keyCode) -> keyCode == Keyboard.KEY_ESCAPE);

        // Add content to screen
        addToScreen(container);
        addToScreen(trademarkLabel);
        addToScreen(copyrightLabel);
        addToScreen(shopButton);
        addToScreen(forumsButton);
        addToScreen(issuesButton);

        // OpenGL Warning
        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            final UILabel glWarning1 = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextStyles.BOLD,
                    TextColors.DARK_RED, I18n.format("almura.menu.main.opengl.0"))));
            glWarning1.setPosition(2, 2, Anchor.TOP | Anchor.LEFT);

            final UILabel glWarning2 = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextStyles.BOLD,
                    TextColors.DARK_RED, I18n.format("almura.menu.main.opengl.1"))));
            glWarning2.setPosition(2, SimpleScreen.getPaddedY(glWarning1, 2), Anchor.TOP | Anchor.LEFT);

            addToScreen(glWarning1);
            addToScreen(glWarning2);
        }

        //Todo: please tell me there is a better way to do this.
        //Why: the settings.saveOptions() method has a check to see if the game is still loading, thus the "FirstLaunched" settings are not saved when they are initially ran.
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        settings.saveOptions();
        // End stupidity.

    }

    @Override
    public void onClose() {
        this.mc.shutdown();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws URISyntaxException, IOException {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.singleplayer":
                this.mc.displayGuiScreen(new GuiWorldSelection(this));
                break;
            case "button.multiplayer":
                new ServerMenu(this).display();
                break;
            case "button.options":
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case "button.mods":
                this.mc.displayGuiScreen(new GuiModList(this));
                break;
            case "button.about":
                new SimpleAboutMenu(this).display();
                break;
            case "button.quit":
                this.close();
                break;
            case "button.shop":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.SHOP));
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.FORUM));
                break;
            case "button.issues":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.ISSUES));
                break;
        }
    }
}
