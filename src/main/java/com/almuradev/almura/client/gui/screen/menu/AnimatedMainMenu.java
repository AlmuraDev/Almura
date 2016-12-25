/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.menu;

import com.almuradev.almura.Constants;
import com.almuradev.almura.client.gui.component.UIAnimatedBackground;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.client.gui.util.builder.UIButtonBuilder;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.client.renderer.OpenGlHelper;
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
public class AnimatedMainMenu extends SimpleScreen {

    private static final int PADDING = 4;

    private UIBackgroundContainer buttonContainer;

    public AnimatedMainMenu(@Nullable SimpleScreen parent) {
        super(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        final UIBackgroundContainer container = new UIBackgroundContainer(this);
        container.setBackgroundAlpha(0);
        container.setPosition(0, -10, Anchor.MIDDLE | Anchor.CENTER);
        container.setSize(Constants.Gui.BUTTON_WIDTH_LONG, 205);

        // Almura header
        final UIImage almuraHeader = new UIImage(this, new GuiTexture(Constants.Gui.LOCATION_ALMURA_LOGO), null);
        almuraHeader.setSize(60, 99);
        almuraHeader.setPosition(0, 0, Anchor.TOP | Anchor.CENTER);

        this.buttonContainer = new UIBackgroundContainer(this, Constants.Gui.BUTTON_WIDTH_LONG, (Constants.Gui.BUTTON_HEIGHT * 4) + (PADDING * 3));
        this.buttonContainer.setPosition(0, SimpleScreen.getPaddedY(almuraHeader, 10), Anchor.TOP | Anchor.CENTER);
        this.buttonContainer.setBackgroundAlpha(0);

        final UIButton singleplayerButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("Singleplayer"))
                .size(Constants.Gui.BUTTON_WIDTH_LONG, Constants.Gui.BUTTON_HEIGHT)
                .position(0, 0)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.singleplayer");

        final UIButton multiplayerButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("Multiplayer"))
                .size(Constants.Gui.BUTTON_WIDTH_LONG, Constants.Gui.BUTTON_HEIGHT)
                .position(0, SimpleScreen.getPaddedY(singleplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.multiplayer");

        final UIButton optionsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("Options"))
                .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                .position(-68, SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.options");

        final UIButton modsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("Mods"))
                .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                .position(SimpleScreen.getPaddedX(optionsButton, PADDING), SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.mods");

        final UIButton aboutButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("About"))
                .size(Constants.Gui.BUTTON_WIDTH_TINY, Constants.Gui.BUTTON_HEIGHT)
                .position(SimpleScreen.getPaddedX(modsButton, PADDING), SimpleScreen.getPaddedY(multiplayerButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.about");

        final UIButton quitButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .text(Text.of("Quit"))
                .fro(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_LIGHT_RED).shadow(true).build())
                .hoverFro(FontOptions.builder().color(Color.ofRgb(255, 89, 89).getRgb()).shadow(true).build())
                .size(Constants.Gui.BUTTON_WIDTH_LONG, Constants.Gui.BUTTON_HEIGHT)
                .position(singleplayerButton.getX(), SimpleScreen.getPaddedY(optionsButton, PADDING))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .listener(this)
                .build("button.quit");

        final UIButton forumsButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(Constants.Gui.ICON_ENJIN)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(-PADDING, -PADDING)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of("Forums"))
                .build("button.forums");

        final UIButton issuesButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(Constants.Gui.ICON_FA_GITHUB)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(forumsButton, PADDING, Anchor.RIGHT), forumsButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of("Issues"))
                .build("button.issues");

        final UIButton shopButton = new UIButtonBuilder(this)
                .container(this.buttonContainer)
                .icon(Constants.Gui.ICON_FA_SHOPPING_BAG)
                .size(Constants.Gui.BUTTON_WIDTH_ICON, Constants.Gui.BUTTON_HEIGHT_ICON)
                .position(SimpleScreen.getPaddedX(issuesButton, PADDING, Anchor.RIGHT), issuesButton.getY())
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .tooltip(Text.of("Shop"))
                .build("button.shop");

        final UILabel trademarkLabel = new UILabel(this, TextFormatting.YELLOW + Constants.Gui.TRADEMARK);
        trademarkLabel.setPosition(PADDING, -PADDING, Anchor.BOTTOM | Anchor.LEFT);

        final UILabel copyrightLabel = new UILabel(this, TextFormatting.YELLOW + Constants.Gui.COPYRIGHT);
        copyrightLabel
                .setPosition(trademarkLabel.getX(), SimpleScreen.getPaddedY(trademarkLabel, PADDING, Anchor.BOTTOM), trademarkLabel.getAnchor());

        container.add(almuraHeader, this.buttonContainer);

        // Disable escape key press
        registerKeyListener((keyChar, keyCode) -> keyCode == Keyboard.KEY_ESCAPE);

        // Add content to screen
        addToScreen(new UIAnimatedBackground(this));
        addToScreen(container);
        addToScreen(trademarkLabel);
        addToScreen(copyrightLabel);
        addToScreen(shopButton);
        addToScreen(forumsButton);
        addToScreen(issuesButton);

        // OpenGL Warning
        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported()) {
            final UILabel glWarning1 = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextStyles.BOLD,
                    TextColors.DARK_RED, "Warning! This graphics card does not support OpenGL 2.0.")));
            glWarning1.setPosition(2, 2, Anchor.TOP | Anchor.LEFT);

            final UILabel glWarning2 = new UILabel(this, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextStyles.BOLD,
                    TextColors.DARK_RED, "Future versions of Minecraft may not be compatible.")));
            glWarning2.setPosition(2, SimpleScreen.getPaddedY(glWarning1, 2), Anchor.TOP | Anchor.LEFT);

            addToScreen(glWarning1);
            addToScreen(glWarning2);
        }
    }

    @Override
    public void onClose() {
        mc.shutdown();
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws URISyntaxException, IOException {
        switch (event.getComponent().getName().toLowerCase(Locale.ENGLISH)) {
            case "button.singleplayer":
                this.mc.displayGuiScreen(new GuiWorldSelection(this));
                break;
            case "button.multiplayer":
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
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
                Desktop.getDesktop().browse(new URI(Constants.Gui.SHOP_URL));
                break;
            case "button.forums":
                Desktop.getDesktop().browse(new URI(Constants.Gui.FORUM_URL));
                break;
            case "button.issues":
                Desktop.getDesktop().browse(new URI(Constants.Gui.ISSUES_URL));
                break;
        }
    }
}
