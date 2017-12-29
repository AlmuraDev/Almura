/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.main;

import com.almuradev.almura.Almura;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.client.texture.GuiRemoteTexture;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleContainerScreen;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class SimpleAboutMenu extends SimpleContainerScreen {

    private UISimpleList list;
    private UITextField textField;

    public SimpleAboutMenu(@Nullable SimpleScreen parent) {
        super(parent, Text.of(I18n.format("almura.menu_button.about")));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void construct() {
        super.construct();

        this.list = new UISimpleList(this, 125, UIComponent.INHERITED);
        this.list.setPosition(4, 0);
        this.list.setElementSpacing(4);
        this.list.setUnselect(false);

        final List<AboutListElement> elementList = Lists.newArrayList();
        // Static entry
        elementList.add(new AboutListElement(
                this,
                this.list,
                new UIImage(this, new GuiTexture(GuiConfig.Location.ALMURA_MAN), null), 23, 32, 5, 0, 8,
                AboutConfig.TITLE,
                AboutConfig.STORY));

        AboutConfig.ENTRIES.forEach(entry -> {
            Text titles = Text.EMPTY;
            for (String title : entry.titles) {
                titles = titles.toBuilder().append(Text.of("  • ", I18n.format(title)), Text.NEW_LINE).build();
            }
            elementList.add(new AboutListElement(
                    this,
                    this.list,
                    new UIImage(this, new GuiRemoteTexture(
                            GuiConfig.Location.GENERIC_AVATAR,
                            new ResourceLocation(Almura.ID, "textures/gui/skins/avatars/" + entry.uniqueId + ".png"),
                            String.format(GuiConfig.Url.SKINS, entry.uniqueId, 32),
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
        this.textField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

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

                final int width = parent.getContentWidth() - (parent.getScrollBar().isEnabled() ? parent.getScrollBar().getRawWidth() + 1 : 0);

                setSize(width, getHeight());

                if (this == parent.getSelected()) {
                    super.drawBackground(renderer, mouseX, mouseY, partialTick);
                }
            }
        }
    }
}
