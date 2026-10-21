/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client.gui;

import com.almuradev.almura.feature.accessory.AccessoryCatalog;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.client.ClientAccessoryManager;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Pick the accessories to wear: a type, then one of its styles, tried on the player preview before wearing it.
 */
@SideOnly(Side.CLIENT)
public final class AccessoryScreen extends BasicScreen {

    private static final int FORM_WIDTH = 320;
    private static final int FORM_HEIGHT = 200;

    private final ClientAccessoryManager manager;
    private final List<AccessoryType> allowed;
    private BasicForm form;
    @Nullable private UISelect<AccessoryType> typeSelect;
    @Nullable private UISelect<String> styleSelect;
    @Nullable private UILabel countLabel;
    @Nullable private UILabel statusLabel;

    public AccessoryScreen(@Nullable final GuiScreen parent, final ClientAccessoryManager manager, final List<AccessoryType> allowed) {
        super(parent);
        this.manager = manager;
        this.allowed = allowed;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new BasicForm(this, FORM_WIDTH, FORM_HEIGHT, "Accessories");
        this.form.setBackgroundAlpha(215);

        final UIButton closeButton = new UIButtonBuilder(this)
                .text("Close")
                .width(50)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.close");
        this.form.add(closeButton);

        if (this.allowed.isEmpty()) {
            final UILabel none = new UILabel(this, "You don't have any accessories you can wear.");
            none.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
            none.setPosition(0, 0, Anchor.CENTER | Anchor.MIDDLE);
            this.form.add(none);
            this.addToScreen(this.form);
            return;
        }

        final UILabel typeLabel = new UILabel(this, "Accessory");
        typeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        typeLabel.setPosition(4, 6, Anchor.LEFT | Anchor.TOP);

        this.typeSelect = new UISelect<>(this, 130, this.allowed);
        this.typeSelect.setLabelFunction(AccessoryType::displayName);
        this.typeSelect.setPosition(4, 18, Anchor.LEFT | Anchor.TOP);
        this.typeSelect.register(this);

        final UILabel styleLabel = new UILabel(this, "Style");
        styleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        styleLabel.setPosition(4, 42, Anchor.LEFT | Anchor.TOP);

        this.styleSelect = new UISelect<>(this, 130);
        this.styleSelect.setPosition(4, 54, Anchor.LEFT | Anchor.TOP);
        this.styleSelect.maxDisplayedOptions(12);
        this.styleSelect.register(this);

        final UIButton previousButton = new UIButtonBuilder(this)
                .text("<")
                .width(24)
                .position(4, 76)
                .anchor(Anchor.LEFT | Anchor.TOP)
                .listener(this)
                .build("button.previous");

        final UIButton nextButton = new UIButtonBuilder(this)
                .text(">")
                .width(24)
                .position(32, 76)
                .anchor(Anchor.LEFT | Anchor.TOP)
                .listener(this)
                .build("button.next");

        this.countLabel = new UILabel(this, "");
        this.countLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).build());
        this.countLabel.setPosition(62, 80, Anchor.LEFT | Anchor.TOP);

        this.statusLabel = new UILabel(this, "");
        this.statusLabel.setFontOptions(FontOptions.builder().from(FontColors.GOLD_FO).shadow(true).build());
        this.statusLabel.setPosition(4, 104, Anchor.LEFT | Anchor.TOP);

        final UIButton wearButton = new UIButtonBuilder(this)
                .text("Wear")
                .width(50)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.wear");

        final UIButton removeButton = new UIButtonBuilder(this)
                .text("Take Off")
                .width(60)
                .position(54, 0)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.remove");

        // Style list is added before the type list so the type list's options draw over it when open
        this.form.add(typeLabel, styleLabel, this.styleSelect, this.typeSelect, previousButton, nextButton, this.countLabel, this.statusLabel,
                wearButton, removeButton);
        this.addToScreen(this.form);

        this.showType(this.allowed.get(0));
        this.typeSelect.setSelectedOption(this.allowed.get(0));
    }

    private void showType(final AccessoryType type) {
        if (this.styleSelect == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        this.styleSelect.setLabelFunction(variant -> AccessoryCatalog.label(type, variant));
        this.styleSelect.setOptions(styles);

        final String worn = this.manager.getWornVariant(Minecraft.getMinecraft().player.getUniqueID(), type);
        final String style = worn != null && styles.contains(worn) ? worn : styles.isEmpty() ? null : styles.get(0);
        if (style != null) {
            this.styleSelect.setSelectedOption(style);
            this.preview(type, style);
        }
    }

    private void preview(final AccessoryType type, @Nullable final String style) {
        this.manager.setPreview(type, style);
        this.updateLabels(type, style);
    }

    private void updateLabels(@Nullable final AccessoryType type, @Nullable final String style) {
        if (type == null || this.countLabel == null || this.statusLabel == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        this.countLabel.setText(style == null ? "" : (styles.indexOf(style) + 1) + " / " + styles.size());

        final String worn = this.manager.getWornVariant(Minecraft.getMinecraft().player.getUniqueID(), type);
        this.statusLabel.setText(worn == null ? "Not wearing " + type.displayName() : "Wearing: " + AccessoryCatalog.label(type, worn));
    }

    @Subscribe
    public void onSelect(final UISelect.SelectEvent<?> event) {
        if (event.getComponent() == this.typeSelect && event.getNewValue() instanceof AccessoryType) {
            this.showType((AccessoryType) event.getNewValue());
        } else if (event.getComponent() == this.styleSelect && event.getNewValue() instanceof String && this.typeSelect != null) {
            this.preview(this.typeSelect.getSelectedValue(), (String) event.getNewValue());
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(final UIButton.ClickEvent event) {
        final AccessoryType type = this.typeSelect == null ? null : this.typeSelect.getSelectedValue();
        final String style = this.styleSelect == null ? null : this.styleSelect.getSelectedValue();

        switch (event.getComponent().getName().toLowerCase()) {
            case "button.previous":
                if (this.styleSelect != null && type != null) {
                    this.preview(type, this.styleSelect.selectPrevious());
                }
                break;
            case "button.next":
                if (this.styleSelect != null && type != null) {
                    this.preview(type, this.styleSelect.selectNext());
                }
                break;
            case "button.wear":
                if (type != null && style != null) {
                    this.manager.select(type, style);
                }
                break;
            case "button.remove":
                if (type != null) {
                    this.manager.select(type, null);
                    this.preview(type, null);
                }
                break;
            case "button.close":
                this.close();
                break;
        }
    }

    @Override
    public void update(final int mouseX, final int mouseY, final float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        // Wearing/taking off is confirmed by the server, keep the status current
        if (this.typeSelect != null && this.styleSelect != null) {
            this.updateLabels(this.typeSelect.getSelectedValue(), this.styleSelect.getSelectedValue());
        }

        final int x = this.form.screenX() + 240;
        final int y = this.form.screenY() + 165;
        drawEntityOnScreen(x, y, 60, x - mouseX, y - 100 - mouseY, Minecraft.getMinecraft().player);
    }

    @Override
    protected void onClose() {
        this.manager.clearPreview();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static void drawEntityOnScreen(final int posX, final int posY, final int scale, final float mouseX, final float mouseY,
            final EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        final float renderYawOffset = entity.renderYawOffset;
        final float rotationYaw = entity.rotationYaw;
        final float rotationPitch = entity.rotationPitch;
        final float prevRotationYawHead = entity.prevRotationYawHead;
        final float rotationYawHead = entity.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        entity.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        entity.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        GlStateManager.disableLighting();
        renderManager.setRenderShadow(false);
        renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        renderManager.setRenderShadow(true);
        entity.renderYawOffset = renderYawOffset;
        entity.rotationYaw = rotationYaw;
        entity.rotationPitch = rotationPitch;
        entity.prevRotationYawHead = prevRotationYawHead;
        entity.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
