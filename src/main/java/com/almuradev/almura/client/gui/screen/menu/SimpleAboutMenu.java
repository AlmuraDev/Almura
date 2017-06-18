/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.screen.menu;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
import com.almuradev.almura.client.ClientProxy;
import com.almuradev.almura.client.gui.GuiRemoteTexture;
import com.almuradev.almura.client.gui.component.UISimpleList;
import com.almuradev.almura.client.gui.screen.SimpleContainerScreen;
import com.almuradev.almura.client.gui.screen.SimpleScreen;
import com.almuradev.almura.client.gui.util.FontOptionsConstants;
import com.almuradev.almura.client.gui.util.builder.UIButtonBuilder;
import com.almuradev.almura.configuration.MappedConfigurationAdapter;
import com.almuradev.almura.configuration.category.gui.about.AboutCategory;
import com.almuradev.almura.configuration.category.gui.about.AboutEntryCategory;
import com.almuradev.almura.configuration.type.ClientConfiguration;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SimpleAboutMenu extends SimpleContainerScreen {

    private UISimpleList list;
    private UITextField textField;

    public SimpleAboutMenu(@Nullable SimpleScreen parent) {
        super(parent, Text.of(I18n.format("almura.menu.about")));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        super.construct();

        this.list = new UISimpleList(this, 125, UIComponent.INHERITED);
        this.list.setPosition(4, 0);
        this.list.setElementSpacing(4);
        this.list.setUnselect(false);

        // Add defaults if they don't exist.
        final MappedConfigurationAdapter<ClientConfiguration> configAdapter = ((ClientProxy) Almura.proxy).getPlatformConfigAdapter();
        final ClientConfiguration configuration = configAdapter.getConfig();
        final AboutCategory aboutCategory = configuration.gui.about;
        if (configuration.gui.about.entries.isEmpty()) {
            // Zidane
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.BLUE, "almura.menu.about.zidane.name",
                    "almura.menu.about.zidane.description",
                    "85271de5-8380-4db5-9f05-ada3b4aa785c",
                    "almura.menu.about.zidane.titles.1",
                    "almura.menu.about.zidane.titles.2",
                    "almura.menu.about.zidane.titles.3"));
            // Dockter
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.GOLD, "almura.menu.about.dockter.name",
                    "almura.menu.about.dockter.description",
                    "bcbce24c-20fc-4914-8f49-5aaed0cd3696",
                    "almura.menu.about.dockter.titles.1",
                    "almura.menu.about.dockter.titles.2",
                    "almura.menu.about.dockter.titles.3"));
            // Grinch
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.DARK_GREEN, "almura.menu.about.grinch.name",
                    "almura.menu.about.grinch.description",
                    "7c104888-df99-4224-a8ba-2c4e15dbc777",
                    "almura.menu.about.grinch.titles.1",
                    "almura.menu.about.grinch.titles.2",
                    "almura.menu.about.grinch.titles.3"));
            // Wifee
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.LIGHT_PURPLE, "almura.menu.about.wifee.name",
                    "almura.menu.about.wifee.description",
                    "5f757396-8bc7-4dff-8b1f-37fd454a86b7",
                    "almura.menu.about.wifee.titles.1"));
            // Wolfeye
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.RED, "almura.menu.about.wolfeye.name",
                    "almura.menu.about.wolfeye.description",
                    "33f9598e-9890-4f76-90ff-12cd73ca1e3c",
                    "almura.menu.about.wolfeye.titles.1",
                    "almura.menu.about.wolfeye.titles.2"));
            // Mumfrey
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.GRAY, "almura.menu.about.mumfrey.name",
                    "almura.menu.about.mumfrey.description",
                    "e8e0361e-9b3b-481a-b06a-5c314a6c1ef0",
                    "almura.menu.about.mumfrey.titles.1",
                    "almura.menu.about.mumfrey.titles.2"));
            // Blood
            aboutCategory.entries.add(new AboutEntryCategory(TextColors.DARK_RED, "almura.menu.about.blood.name",
                    "almura.menu.about.blood.description",
                    "87caf570-b1fc-4100-bd95-3e7f1fa2e153",
                    "almura.menu.about.blood.titles.1",
                    "almura.menu.about.blood.titles.2"));
            try {
                ((ClientProxy) Almura.proxy).getPlatformConfigAdapter().save();
            } catch (IOException | ObjectMappingException e) {
                throw new RuntimeException("Failed to save config for class [" + configAdapter.getConfigClass() + "] in [" + configAdapter
                        .getConfigPath() + "]!", e);
            }
        }

        final List<AboutListElement> elementList = Lists.newArrayList();
        // Static entry
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiTexture(Constants.Gui.LOCATION_ALMURA_MAN), null), 23, 32, 5, 0, 8,
                Text.of(TextColors.WHITE, "Almura"),
                Text.of(TextColors.WHITE, "Almura was created and is maintained by the AlmuraDev Team (https://www.github.com/AlmuraDev/)",
                        Text.NEW_LINE, Text.NEW_LINE,
                        TextStyles.BOLD, "The Beginning...", TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                        "Almura 1.0 was conceived on June 1st, 2011. It was built around Spoutcraft (1.7.3 beta). As the technology changed we were",
                        " forced to abandon the Spoutcraft ecosystem in favor of the Forge one. ", TextColors.AQUA, "Zidane", TextColors.RESET,
                        " our lead developer for AlmuraDev finally put his foot down and said enough is enough which began our six ",
                        "month journey to migrate the features we had relied on in Spoutcraft to our new client.", Text.NEW_LINE,
                        Text.NEW_LINE,
                        "Almura Rediscovered was initially conceived on September 4th, 2014.", Text.NEW_LINE,
                        Text.NEW_LINE,
                        "Almura has a number of changes from a typical Minecraft setup that gives it an extremely unique and customizable "
                                + "experience.",
                        Text.NEW_LINE,
                        "  • ASM through Mixin.", Text.NEW_LINE,
                        "  • YAML/JSON content loading system for items, blocks, crops, trees and more.", Text.NEW_LINE,
                        "  • Client/Server security system.", Text.NEW_LINE,
                        "  • Customized GUI for a more unique experience.", Text.NEW_LINE,
                        "  • Information guide system displayed in-game.", Text.NEW_LINE,
                        "  • Player accessory system for hats, wings, capes, earrings and more.")));

        aboutCategory.entries.forEach(entry -> {
            Text titles = Text.EMPTY;
            for (String title : entry.titles) {
                titles = titles.toBuilder().append(Text.of("  • ", I18n.format(title)), Text.NEW_LINE).build();
            }
            elementList.add(new AboutListElement(
                    this,
                    this.list,
                    new UIImage(this, new GuiRemoteTexture(
                            Constants.Gui.LOCATION_AVATAR_GENERIC,
                            new ResourceLocation(Constants.Plugin.ID, "textures/gui/skins/avatars/" + entry.uniqueId + ".png"),
                            String.format(Constants.Gui.SKIN_URL_BASE, UUID.fromString(entry.uniqueId), 32),
                            32, 32), null),
                    Text.of(entry.color, I18n.format(entry.name)),
                    Text.of(TextColors.WHITE, I18n.format(entry.description),
                            Text.NEW_LINE, Text.NEW_LINE,
                            TextStyles.BOLD, I18n.format("almura.menu.about.titles"), TextStyles.RESET, TextColors.RESET, Text.NEW_LINE,
                            titles)));
        });

        final UIButton doneButton = new UIButtonBuilder(this)
                .text(Text.of(I18n.format("gui.done")))
                .size(98, 20)
                .position(0, -15, 1)
                .anchor(Anchor.BOTTOM | Anchor.CENTER)
                .listener(this)
                .build("button.done");

        this.textField = new UITextField(this, "", true);
        this.textField.setPosition(SimpleScreen.getPaddedX(this.list, 4), 0);
        this.textField.setEditable(false);
        this.textField.setFontOptions(FontOptions.builder().from(FontOptionsConstants.FRO_COLOR_WHITE).shadow(false).build());

        this.list.setElements(elementList);
        this.list.register(this);
        this.list.select(elementList.get(0));

        this.getContainer().add(this.list, this.textField);

        final UILabel spongeForgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "SpongeForge: " + ((Optional<String>) Sponge.getPlatform()
                .asMap().get("ImplementationVersion")).orElse("dev"));
        spongeForgeVersionLabel.setPosition(4, -24, Anchor.LEFT | Anchor.BOTTOM);

        final Optional<PluginContainer> optForge = Sponge.getPluginManager().getPlugin("Forge");
        if (optForge.isPresent()) {
            final UILabel forgeVersionLabel = new UILabel(this, TextFormatting.WHITE + "Forge: " + optForge.get().getVersion().orElse("dev"));
            forgeVersionLabel.setPosition(4, -14, Anchor.LEFT | Anchor.BOTTOM);
            addToScreen(forgeVersionLabel);
        }
        final Optional<PluginContainer> optAlmura = Sponge.getPluginManager().getPlugin("almura");
        if (optForge.isPresent()) {
            final UILabel almuraVersionLabel = new UILabel(this, TextFormatting.WHITE + "Almura: " + optAlmura.get().getVersion().orElse("dev"));
            almuraVersionLabel.setPosition(4, -4, Anchor.LEFT | Anchor.BOTTOM);
            addToScreen(almuraVersionLabel);
        }

        addToScreen(doneButton);
        addToScreen(spongeForgeVersionLabel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.setPosition(4, 0);
        this.list.setSize(125, this.getContainer().getHeight());
        this.textField.setSize(this.width - this.list.getWidth() - 12, this.getContainer().getHeight());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.done":
                this.close();
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onElementSelect(UIListContainer.SelectEvent event) {
        if (event.getSelected() instanceof AboutListElement) {
            final AboutListElement element = (AboutListElement) event.getSelected();

            this.textField.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(element.contentText));
        }
    }

    protected static final class AboutListElement extends UIBackgroundContainer {

        private static final int BORDER_COLOR = org.spongepowered.api.util.Color.ofRgb(128, 128, 128).getRgb();
        private static final int INNER_COLOR = org.spongepowered.api.util.Color.ofRgb(0, 0, 0).getRgb();

        private final Text contentText;
        private final UIImage image;
        private final UILabel label;

        private AboutListElement(MalisisGui gui, UISimpleList parent, UIImage image, Text text) {
            this(gui, parent, image, text, Text.EMPTY);
        }

        private AboutListElement(MalisisGui gui, UISimpleList parent, UIImage image, Text text, Text contentText) {
            this(gui, parent, image, 32, 32, 2, 0, 4, text, contentText);
        }

        @SuppressWarnings("deprecation")
        private AboutListElement(MalisisGui gui, UISimpleList parent, UIImage image, int imageWidth, int imageHeight, int imageX, int imageY, int
                padding, Text text, Text contentText) {
            super(gui);

            // Set parent
            this.parent = parent;

            // Create image
            this.image = image;
            this.image.setSize(imageWidth, imageHeight);
            this.image.setPosition(imageX, imageY, Anchor.MIDDLE | Anchor.LEFT);

            // Create label
            this.label = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
            this.label.setPosition(SimpleScreen.getPaddedX(image, padding), 2);

            // Set content text
            this.contentText = contentText;

            // Add image/label
            this.add(this.image, this.label);

            // Set size
            this.setSize(((UIListContainer) this.getParent()).getContentWidth() - 3, image.getHeight() + 6);

            // Set padding
            this.setPadding(1, 1);

            // Set colors
            this.setColor(INNER_COLOR);
            this.setBorder(BORDER_COLOR, 1, 255);
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            if (this.parent instanceof UISimpleList) {
                final UISimpleList parent = (UISimpleList) this.parent;

                final int width = parent.getContentWidth() - (parent.getScrollBar().isDisabled() ? 0 : parent.getScrollBar().getRawWidth() + 1);

                setSize(width, getHeight());

                if (this == parent.getSelected()) {
                    super.drawBackground(renderer, mouseX, mouseY, partialTick);
                }
            }
        }
    }
}
