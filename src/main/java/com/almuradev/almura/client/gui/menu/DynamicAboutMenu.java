/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIAnimatedBackground;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.gui.util.FontRenderOptionsConstants;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

@SideOnly(Side.CLIENT)
public class DynamicAboutMenu extends SimpleGui {

    private static final Text aboutText = Text.of(
            "Almura was created and is maintained by the AlmuraDev Team. https://www.github.com/AlmuraDev/", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, TextStyles.UNDERLINE, "The Beginning...", TextStyles.RESET, Text.NEW_LINE,
            "Almura 1.0 was conceived on June 1st, 2011. It was built around Spoutcraft (1.7.3 beta). As the technology changed we wereforced to ",
            "abandon the Spoutcraft ecosystem in favor of the Forge one. ", TextColors.BLUE, "Zidane", TextColors.RESET,
            " our lead developer for AlmuraDev finally put his foot down and said enough is enough which began our six ",
            "month journey to migrate the features we had relied on in Spoutcraft to our new client.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura Rediscovered was initially conceived on September 4th, 2014.", Text.NEW_LINE,
            Text.NEW_LINE,
            "Almura has a number of changes from a typical Minecraft setup that gives it an extremely unique and customizable experience.", Text
                    .NEW_LINE,
            "  - ASM through Mixin.", Text.NEW_LINE,
            "  - YAML/JSON content loading system for items, blocks, crops, trees and more.", Text.NEW_LINE,
            "  - Client/Server security system.", Text.NEW_LINE,
            "  - Customized GUI for a more unique experience.", Text.NEW_LINE,
            "  - Information guide system displayed in-game.", Text.NEW_LINE,
            "  - Player accessory system for hats, wings, capes, earrings and more.", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, TextStyles.UNDERLINE, "Credits", TextStyles.RESET, Text.NEW_LINE,
            TextColors.BLUE, "Zidane", TextColors.RESET, Text.NEW_LINE,
            "  - Lead developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Lead developer of AlmuraDev", Text.NEW_LINE,
            "  - Former Spoutcraft/Spout developer", Text.NEW_LINE,
            TextColors.GREEN, "Grinch", TextColors.RESET, Text.NEW_LINE,
            "  - Moderator and contributor to Sponge", Text.NEW_LINE,
            "  - Solder administrator for AlmuraDev", Text.NEW_LINE,
            TextColors.GOLD, "Dockter", TextColors.RESET, Text.NEW_LINE,
            "  - Developer, Lead Tester and Owner for/of Almura public servers", Text.NEW_LINE,
            "  - CFO for Sponge Foundation", Text.NEW_LINE,
            "  - Former Spoutcraft developer", Text.NEW_LINE,
            TextColors.DARK_RED, "Blood", TextColors.RESET, Text.NEW_LINE,
            "  - Developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Former Cauldron developer", Text.NEW_LINE,
            TextColors.DARK_PURPLE, "Mumfrey", TextColors.RESET, Text.NEW_LINE,
            "  - Developer for Sponge", Text.NEW_LINE,
            "  - Developer and co-founder of SpongePowered", Text.NEW_LINE,
            "  - Creator and maintainer of Mixin ASM for Java", Text.NEW_LINE,
            TextColors.GOLD, "Wifee", TextColors.RESET, Text.NEW_LINE,
            "  - Lead model and graphics artist for Almura's custom content", Text.NEW_LINE,
            Text.NEW_LINE,
            TextStyles.BOLD, "Special thanks to ", TextColors.BLUE, "Zidane", TextColors.RESET, " for all his time, patience and effort over the ",
            "years. Without this individual.. Almura and AlmuraDev simply would not exist."
    );

    public DynamicAboutMenu(SimpleGui parent) {
        super(parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void construct() {
        // Create the form
        final UIForm form = new UIForm(this, 300, 225, "About");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);

        // Create the logo
        final UIImage almuraLogoImage = new UIImage(this, new GuiTexture(DynamicMainMenu.ALMURA_LOGO_LOCATION), null);
        almuraLogoImage.setPosition(0, 4, Anchor.TOP | Anchor.CENTER);
        almuraLogoImage.setSize(131, 25);

        final UIImage spongepoweredLogoImage = new UIImage(this, new GuiTexture(DynamicMainMenu.SPONGEPOWERED_LOGO_LOCATION), null);
        spongepoweredLogoImage.setPosition(0, SimpleGui.getPaddedY(almuraLogoImage, 4), Anchor.TOP | Anchor.CENTER);
        spongepoweredLogoImage.setSize(123, 36);

        // Almura Version
        final UILabel almuraVersion = new UILabel(this, true);
        almuraVersion.setPosition(4, 4, Anchor.RIGHT);
        almuraVersion.setText(TextFormatting.GRAY + "Almura: " + Almura.GUI_VERSION);

        // Minecraft Version
        final UILabel minecraftVersion = new UILabel(this);
        minecraftVersion.setText(TextFormatting.GRAY + "Minecraft: " + ((MinecraftVersion) (Sponge.getPlatform().asMap().get("MinecraftVersion")))
                .getName());
        minecraftVersion.setPosition(almuraVersion.getX(), SimpleGui.getPaddedY(almuraVersion, 3), almuraVersion.getAnchor());

        // Forge Version
        final UILabel forgeVersion = new UILabel(this);
        forgeVersion.setText(TextFormatting.GRAY + "Forge: " + Sponge.getPlatform().asMap().get("ForgeVersion"));
        forgeVersion.setPosition(minecraftVersion.getX(), SimpleGui.getPaddedY(minecraftVersion, 3), minecraftVersion.getAnchor());

        // Create the mods button
        final UIButton modsButton = new UIButton(this, "Mods");
        modsButton.setSize(50, 16);
        modsButton.setPosition(5, -5, Anchor.BOTTOM | Anchor.LEFT);
        modsButton.setName("button.mods");
        modsButton.register(this);

        // Create the back button
        final UIButton backButton = new UIButton(this, "Back");
        backButton.setSize(50, 16);
        backButton.setPosition(-5, -5, Anchor.BOTTOM | Anchor.RIGHT);
        backButton.setName("button.back");
        backButton.register(this);

        // Create About us multi-line label
        final UITextField aboutUsTextField = new UITextField(this, "", true);
        aboutUsTextField.setSize(290, 115);
        aboutUsTextField.setPosition(0, getPaddedY(modsButton, 5, Anchor.BOTTOM), Anchor.BOTTOM | Anchor.CENTER);
        aboutUsTextField.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(aboutText));
        aboutUsTextField.setFontRenderOptions(FontRenderOptionsConstants.FRO_COLOR_WHITE);
        aboutUsTextField.setName("textfield.about_us");
        aboutUsTextField.setEditable(false);

        form.getContentContainer().add(almuraLogoImage, spongepoweredLogoImage, almuraVersion, minecraftVersion, forgeVersion, aboutUsTextField,
                modsButton, backButton);

        addToScreen(new UIAnimatedBackground(this));
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.mods":
                mc.displayGuiScreen(new GuiModList(this));
                break;
            case "button.back":
                close();
                break;
        }
    }
}
