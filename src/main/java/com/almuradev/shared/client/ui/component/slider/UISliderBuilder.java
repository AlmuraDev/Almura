/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.client.ui.component.slider;

import com.google.common.base.Converter;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UISliderBuilder {

    private final MalisisGui gui;
    @Nullable private UIContainer container;
    @Nullable private FontOptions fro;
    private FontOptions hoverFro;
    private int width, height, x, y, z, anchor;
    @Nullable private MalisisFont font;
    @Nullable private Object object;
    @Nullable private Text text;
    @Nullable private Object value;
    @Nullable private UITooltip tooltip;
    @Nullable private Converter converter;
    private float scrollStep;
    private boolean enabled = true;

    public UISliderBuilder(MalisisGui gui, Converter converter) {
        this.gui = gui;
        this.converter = converter;
    }

    public UISliderBuilder text(String text) {
        return this.text(Text.of(text));
    }

    public UISliderBuilder text(Text text) {
        this.text = text;
        return this;
    }

    @SuppressWarnings("deprecation")
    public UISliderBuilder tooltip(Text text) {
        return this.tooltip(new UITooltip(this.gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(text), 15));
    }

    public UISliderBuilder tooltip(UITooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public UISliderBuilder size(int width, int height) {
        this.width(width);
        this.height(height);
        return this;
    }

    public UISliderBuilder width(int width) {
        this.width = width;
        return this;
    }

    public UISliderBuilder height(int height) {
        this.height = height;
        return this;
    }

    public UISliderBuilder fro(FontOptions fro) {
        this.fro = fro;
        return this;
    }

    public UISliderBuilder font(MalisisFont font) {
        this.font = font;
        return this;
    }

    public UISliderBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UISliderBuilder position(int x, int y) {
        return this.position(x, y, 0);
    }

    public UISliderBuilder position(int x, int y, int z) {
        this.x(x);
        this.y(y);
        this.z(z);
        return this;
    }

    public UISliderBuilder x(int x) {
        this.x = x;
        return this;
    }

    public UISliderBuilder y(int y) {
        this.y = y;
        return this;
    }

    public UISliderBuilder z(int z) {
        this.z = z;
        return this;
    }

    public UISliderBuilder anchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    public UISliderBuilder listener(Object object) {
        this.object = object;
        return this;
    }

    public UISliderBuilder value(Object value) {
        this.value = value;
        return this;
    }

    public UISliderBuilder container(UIContainer container) {
        this.container = container;
        return this;
    }

    public UISliderBuilder scrollStep(float scrollStep) {
        this.scrollStep = scrollStep;
        return this;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public UISlider build(String id) {
        final UISlider slider = new UISlider(this.gui, this.width, this.converter, TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.text !=
                null ? this.text : Text.EMPTY));
        slider.setPosition(this.x, this.y);
        if (id != null) {
            slider.setName(id);
        }
        if (this.tooltip != null) {
            slider.setTooltip(this.tooltip);
        }
        if (this.width != 0) {
            slider.setSize(this.width, slider.getHeight());
        }
        if (this.height != 0) {
            slider.setSize(slider.getWidth(), this.height);
        }
        if (this.anchor != 0) {
            slider.setAnchor(this.anchor);
        }
        if (this.object != null) {
            slider.register(this.object);
        }
        if (this.fro != null) {
            slider.setFontOptions(this.fro);
        }
        if (this.font != null) {
            slider.setFont(this.font);
        }
        if (this.container != null) {
            this.container.add(slider);
        }
        if (this.value != null) {
            slider.setValue(this.value);
        }
        if (this.scrollStep != 0) {
            slider.setScrollStep(this.scrollStep);
        }
        slider.setDisabled(!this.enabled);
        slider.setZIndex(this.z);

        return slider;
    }
}
