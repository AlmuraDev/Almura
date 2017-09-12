/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util.builder;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public final class UIButtonBuilder {

    private final MalisisGui gui;
    @Nullable private UIContainer container;
    @Nullable private FontOptions fro;
    @Nullable private FontOptions hoverFro;
    private int width, height, x, y, z, anchor;
    @Nullable private MalisisFont font;
    @Nullable private Object object;
    @Nullable private Text text;
    @Nullable private UIImage image;
    @Nullable private UITooltip tooltip;
    private boolean enabled = true;

    public UIButtonBuilder(MalisisGui gui) {
        this.gui = gui;
    }

    public UIButtonBuilder text(String text) {
        return this.text(Text.of(text));
    }

    public UIButtonBuilder text(Text text) {
        this.text = text;
        return this;
    }

    @SuppressWarnings("deprecation")
    public UIButtonBuilder tooltip(Text text) {
        return this.tooltip(new UITooltip(this.gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), 15));
    }

    public UIButtonBuilder tooltip(UITooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public UIButtonBuilder image(UIImage image) {
        this.image = image;
        return this;
    }

    public UIButtonBuilder icon(GuiIcon icon) {
        return this.image(new UIImage(this.gui, null, icon));
    }

    public UIButtonBuilder texture(GuiTexture texture) {
        return this.image(new UIImage(this.gui, texture, null));
    }

    public UIButtonBuilder size(int width, int height) {
        this.width(width);
        this.height(height);
        return this;
    }

    public UIButtonBuilder width(int width) {
        this.width = width;
        return this;
    }

    public UIButtonBuilder height(int height) {
        this.height = height;
        return this;
    }

    public UIButtonBuilder fro(FontOptions fro) {
        this.fro = fro;
        return this;
    }

    public UIButtonBuilder hoverFro(FontOptions hoverFro) {
        this.hoverFro = hoverFro;
        return this;
    }

    public UIButtonBuilder font(MalisisFont font) {
        this.font = font;
        return this;
    }

    public UIButtonBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UIButtonBuilder position(int x, int y) {
        return this.position(x, y, 0);
    }

    public UIButtonBuilder position(int x, int y, int z) {
        this.x(x);
        this.y(y);
        this.z(z);
        return this;
    }

    public UIButtonBuilder x(int x) {
        this.x = x;
        return this;
    }

    public UIButtonBuilder y(int y) {
        this.y = y;
        return this;
    }

    public UIButtonBuilder z(int z) {
        this.z = z;
        return this;
    }

    public UIButtonBuilder anchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public UIButtonBuilder listener(Object object) {
        this.object = object;
        return this;
    }

    public UIButtonBuilder container(UIContainer container) {
        this.container = container;
        return this;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public UIButton build(String id) {
        final UIButton button = new UIButton(this.gui);
        button.setPosition(this.x, this.y);
        if (id != null) {
            button.setName(id);
        }
        if (this.text != null) {
            button.setText(TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.text));
        }
        if (this.image != null) {
            button.setImage(this.image);
        }
        if (this.tooltip != null) {
            button.setTooltip(this.tooltip);
        }
        if (this.width != 0) {
            button.setSize(this.width, button.getHeight());
        }
        if (this.height != 0) {
            button.setSize(button.getWidth(), this.height);
        }
        if (this.anchor != 0) {
            button.setAnchor(this.anchor);
        }
        if (this.object != null) {
            button.register(this.object);
        }
        if (this.fro != null) {
            button.setFontOptions(this.fro);
        }
        if (this.hoverFro != null) {
            button.setHoveredFontOptions(this.hoverFro);
        }
        if (this.font != null) {
            button.setFont(this.font);
        }
        if (this.container != null) {
            this.container.add(button);
        }
        button.setDisabled(!this.enabled);
        button.setZIndex(this.z);

        return button;
    }
}
