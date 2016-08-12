/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.util.builders;

import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.GuiIcon;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class UIButtonBuilder {

    private final MalisisGui gui;
    private FontRenderOptions fro, hoverFro;
    private int width, height, x, y, anchor;
    private MalisisFont font;
    private Object object;
    private String name, text;
    private UIImage image;
    private UITooltip tooltip;
    private boolean enabled = true;

    public UIButtonBuilder(MalisisGui gui) {
        this.gui = gui;
    }

    public UIButtonBuilder text(String text) {
        this.text = text;
        return this;
    }

    public UIButtonBuilder tooltip(String tooltip) {
        return this.tooltip(new UITooltip(gui, tooltip, 0));
    }

    public UIButtonBuilder tooltip(UITooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public UIButtonBuilder image(UIImage image) {
        this.image = image;
        return this;
    }

    public UIButtonBuilder image(GuiIcon icon) {
        return this.image(new UIImage(gui, null, icon));
    }

    public UIButtonBuilder image(GuiTexture texture) {
        return this.image(new UIImage(gui, texture, null));
    }

    public UIButtonBuilder name(String name) {
        this.name = name;
        return this;
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

    public UIButtonBuilder fro(FontRenderOptions fro) {
        this.fro = fro;
        return this;
    }

    public UIButtonBuilder hoverFro(FontRenderOptions hoverFro) {
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

    @SuppressWarnings("deprecation")
    public UIButtonBuilder text(Text text) {
        return this.text(TextSerializers.LEGACY_FORMATTING_CODE.serialize(text));
    }

    public UIButtonBuilder position(int x, int y) {
        this.x(x);
        this.y(y);
        return this;
    }

    public UIButtonBuilder position(int x, int y, int anchor) {
        this.x(x);
        this.y(y);
        this.anchor(anchor);
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

    public UIButtonBuilder anchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public UIButtonBuilder listener(Object object) {
        this.object = object;
        return this;
    }

    public UIButton build() {
        final UIButton button = new UIButton(gui);
        button.setPosition(x, y);
        if (name != null) {
            button.setName(name);
        }
        if (text != null) {
            button.setText(text);
        }
        if (image != null) {
            button.setImage(image);
        }
        if (tooltip != null) {
            button.setTooltip(tooltip);
        }
        if (width != 0) {
            button.setSize(width, button.getHeight());
        }
        if (height != 0) {
            button.setSize(button.getWidth(), height);
        }
        if (anchor != 0) {
            button.setAnchor(anchor);
        }
        if (object != null) {
            button.register(object);
        }
        if (fro != null) {
            button.setFontRenderOptions(fro);
        }
        if (hoverFro != null) {
            button.setHoveredFontRendererOptions(hoverFro);
        }
        if (font != null) {
            button.setFont(font);
        }

        button.setDisabled(!enabled);

        return button;
    }
}
