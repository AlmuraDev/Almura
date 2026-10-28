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
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Pick the accessories to wear: a type, then one of its styles, tried on the player preview before wearing it.
 */
@SideOnly(Side.CLIENT)
public final class AccessoryScreen extends BasicScreen {

    private static final int FORM_WIDTH = 320;
    private static final int FORM_HEIGHT = 200;
    private static final int MAX_PREVIEW_SCALE = 60;
    // Left edge of the player preview area (the controls are to its left)
    private static final int PREVIEW_LEFT = 160;
    // Space the labels take above the head, in blocks: the gap to the head, plus each line of text
    private static final float LABELS_GAP = 0.5F;
    private static final float LABEL_LINE_HEIGHT = 0.2F;

    private final ClientAccessoryManager manager;
    private final List<AccessoryType> allowed;
    private BasicForm form;
    @Nullable private UISelect<AccessoryType> typeSelect;
    @Nullable private UISelect<String> styleSelect;
    @Nullable private UIButton previousButton;
    @Nullable private UIButton nextButton;
    @Nullable private UILabel countLabel;
    @Nullable private UILabel statusLabel;
    @Nullable private UICheckBox hideNameCheckbox;
    @Nullable private UICheckBox hideTitleCheckbox;
    @Nullable private UICheckBox hideHealthCheckbox;
    // Remembered while the game runs
    private static boolean hideNameChoice;
    private static boolean hideTitleChoice;
    private static boolean hideHealthChoice;
    // Preview turn in degrees (0 faces the viewer), changed by dragging over the preview
    private float previewYaw;
    private boolean dragging;
    private int lastDragX;

    public AccessoryScreen(@Nullable final GuiScreen parent, final ClientAccessoryManager manager, final List<AccessoryType> allowed) {
        super(parent);
        this.manager = manager;
        this.allowed = allowed;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        this.form = new BasicForm(FORM_WIDTH, FORM_HEIGHT, "Accessories");
        this.form.setBackgroundAlpha(215);

        final UIButton closeButton = new UIButtonBuilder()
                .text("Close")
                .width(50)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .listener(this)
                .build("button.close");
        this.form.add(closeButton);

        if (this.allowed.isEmpty()) {
            final UILabel none = new UILabel("You don't have any accessories you can wear.");
            none.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
            none.setPosition(0, 0, Anchor.CENTER | Anchor.MIDDLE);
            this.form.add(none);
            this.addToScreen(this.form);
            return;
        }

        final UILabel typeLabel = new UILabel("Accessory");
        typeLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        typeLabel.setPosition(4, 4, Anchor.LEFT | Anchor.TOP);

        this.typeSelect = new UISelect<>(130, this.allowed);
        this.typeSelect.setLabelFunction(AccessoryType::displayName);
        this.typeSelect.setPosition(4, 15, Anchor.LEFT | Anchor.TOP);
        this.typeSelect.register(this);

        final UILabel styleLabel = new UILabel("Style");
        styleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        styleLabel.setPosition(4, 35, Anchor.LEFT | Anchor.TOP);

        this.styleSelect = new UISelect<>(130);
        this.styleSelect.setPosition(4, 46, Anchor.LEFT | Anchor.TOP);
        this.styleSelect.maxDisplayedOptions(12);
        this.styleSelect.register(this);

        this.previousButton = new UIButtonBuilder()
                .text("<")
                .width(24)
                .position(4, 66)
                .anchor(Anchor.LEFT | Anchor.TOP)
                .listener(this)
                .build("button.previous");

        this.nextButton = new UIButtonBuilder()
                .text(">")
                .width(24)
                .position(32, 66)
                .anchor(Anchor.LEFT | Anchor.TOP)
                .listener(this)
                .build("button.next");

        this.countLabel = new UILabel("");
        this.countLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).build());
        this.countLabel.setPosition(62, 70, Anchor.LEFT | Anchor.TOP);

        this.statusLabel = new UILabel("");
        this.statusLabel.setFontOptions(FontOptions.builder().from(FontColors.GOLD_FO).shadow(true).build());
        this.statusLabel.setPosition(4, 90, Anchor.LEFT | Anchor.TOP);

        this.hideNameCheckbox = new UICheckBox("Hide name");
        this.hideNameCheckbox.setPosition(4, 108, Anchor.LEFT | Anchor.TOP);
        this.hideNameCheckbox.setChecked(hideNameChoice);

        this.hideTitleCheckbox = new UICheckBox("Hide title");
        this.hideTitleCheckbox.setPosition(4, 121, Anchor.LEFT | Anchor.TOP);
        this.hideTitleCheckbox.setChecked(hideTitleChoice);

        this.hideHealthCheckbox = new UICheckBox("Hide health bar");
        this.hideHealthCheckbox.setPosition(4, 134, Anchor.LEFT | Anchor.TOP);
        this.hideHealthCheckbox.setChecked(hideHealthChoice);

        final FontOptions checkboxFont = FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build();
        this.hideNameCheckbox.setFontOptions(checkboxFont);
        this.hideTitleCheckbox.setFontOptions(checkboxFont);
        this.hideHealthCheckbox.setFontOptions(checkboxFont);

        // Divides the controls from the player preview
        final UISeparator divider = new UISeparator(this);
        divider.setSize(1, FORM_HEIGHT - 40);
        divider.setPosition(PREVIEW_LEFT - 8, 4, Anchor.LEFT | Anchor.TOP);

        final UIButton wearButton = new UIButtonBuilder()
                .text("Wear")
                .width(50)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.wear");

        final UIButton removeButton = new UIButtonBuilder()
                .text("Take Off")
                .width(60)
                .position(54, 0)
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.remove");

        // Added first so the open dropdown lists draw over them; the style list is added before the type list so the type list's options
        // draw over it when open
        this.form.add(divider, this.hideNameCheckbox, this.hideTitleCheckbox, this.hideHealthCheckbox);
        this.form.add(typeLabel, styleLabel, this.styleSelect, this.typeSelect, this.previousButton, this.nextButton, this.countLabel, this.statusLabel,
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
        final int index = style == null ? -1 : styles.indexOf(style);
        this.countLabel.setText(index < 0 ? "" : (index + 1) + " / " + styles.size());
        if (this.previousButton != null && this.nextButton != null) {
            this.previousButton.setEnabled(index > 0);
            this.nextButton.setEnabled(index >= 0 && index < styles.size() - 1);
        }

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
                this.step(type, style, -1);
                break;
            case "button.next":
                this.step(type, style, 1);
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

    /**
     * Moves to the previous/next style, staying put at either end of the list.
     */
    private void step(@Nullable final AccessoryType type, @Nullable final String style, final int direction) {
        if (this.styleSelect == null || type == null || style == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        final int index = styles.indexOf(style) + direction;
        if (index < 0 || index >= styles.size()) {
            return;
        }
        this.styleSelect.setSelectedOption(styles.get(index));
        this.preview(type, styles.get(index));
    }

    @Override
    public void update() {
        super.update();
        final int mouseX = MalisisGui.MOUSE_POSITION.x();
        final int mouseY = MalisisGui.MOUSE_POSITION.y();

        // Wearing/taking off is confirmed by the server, keep the status current
        if (this.typeSelect != null && this.styleSelect != null) {
            this.updateLabels(this.typeSelect.getSelectedValue(), this.styleSelect.getSelectedValue());
        }

        // Drag with the left mouse button over the preview to turn the player around
        final int left = this.form.screenX() + PREVIEW_LEFT;
        final int top = this.form.screenY() + 15;
        final boolean overPreview = mouseX >= left && mouseX <= this.form.screenX() + FORM_WIDTH && mouseY >= top
                && mouseY <= this.form.screenY() + FORM_HEIGHT;
        if (Mouse.isButtonDown(0) && (this.dragging || overPreview)) {
            if (this.dragging) {
                this.previewYaw = (this.previewYaw + (mouseX - this.lastDragX) * 1.5F) % 360.0F;
            }
            this.dragging = true;
            this.lastDragX = mouseX;
        } else {
            this.dragging = false;
        }

        final EntityPlayer player = Minecraft.getMinecraft().player;
        hideNameChoice = this.hideNameCheckbox != null && this.hideNameCheckbox.isChecked();
        hideTitleChoice = this.hideTitleCheckbox != null && this.hideTitleCheckbox.isChecked();
        hideHealthChoice = this.hideHealthCheckbox != null && this.hideHealthCheckbox.isChecked();

        // Fit the player, what they wear above their head and (unless hidden) the name, title and health bar between the title bar and
        // the bottom of the window
        final int x = this.form.screenX() + (PREVIEW_LEFT + FORM_WIDTH) / 2;
        final int feet = this.form.screenY() + FORM_HEIGHT - 12;
        final int available = feet - (this.form.screenY() + 20);
        // Labels: a 0.5 gap plus a line each for the name and title (the health bar sits below the name)
        final int lines = (hideNameChoice ? 0 : 1) + (hideTitleChoice ? 0 : 1);
        final boolean anyLabel = lines > 0 || !hideHealthChoice;
        final float blocksAboveHead = ClientAccessoryManager.labelLift(player) + (anyLabel ? LABELS_GAP + lines * LABEL_LINE_HEIGHT : 0.05F);
        final int scale = Math.min(MAX_PREVIEW_SCALE, (int) (available / (player.height + blocksAboveHead)));

        // Up and down the player looks towards the mouse (as the inventory preview does), left and right is turned by dragging
        ClientAccessoryManager.setPreviewHidden(hideNameChoice, hideTitleChoice, hideHealthChoice);
        try {
            drawEntityOnScreen(x, feet, scale, this.previewYaw, feet - player.getEyeHeight() * scale - mouseY, player);
        } finally {
            ClientAccessoryManager.setPreviewHidden(false, false, false);
        }
    }

    @Override
    protected void onClose() {
        this.manager.clearPreview();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Draws the entity facing the viewer, turned by {@code yaw} degrees around its vertical axis, tilted and looking up or down towards
     * the mouse ({@code mouseY}: the mouse's height above the entity's eyes).
     */
    private static void drawEntityOnScreen(final int posX, final int posY, final int scale, final float yaw, final float mouseY,
            final EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.scale((float) -scale, (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        final float renderYawOffset = entity.renderYawOffset;
        final float prevRenderYawOffset = entity.prevRenderYawOffset;
        final float rotationYaw = entity.rotationYaw;
        final float rotationPitch = entity.rotationPitch;
        final float prevRotationPitch = entity.prevRotationPitch;
        final float prevRotationYawHead = entity.prevRotationYawHead;
        final float rotationYawHead = entity.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        final float pitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        entity.renderYawOffset = 0.0F;
        entity.prevRenderYawOffset = 0.0F;
        entity.rotationYaw = 0.0F;
        entity.rotationPitch = pitch;
        entity.prevRotationPitch = pitch;
        entity.rotationYawHead = 0.0F;
        entity.prevRotationYawHead = 0.0F;
        final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0F);
        // The nameplate faces the camera's pitch; level it, or looking up or down flattens the name and title into each other
        final float playerViewX = renderManager.playerViewX;
        renderManager.playerViewX = 0.0F;
        GlStateManager.disableLighting();
        renderManager.setRenderShadow(false);
        renderManager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        renderManager.setRenderShadow(true);
        renderManager.playerViewX = playerViewX;
        entity.renderYawOffset = renderYawOffset;
        entity.prevRenderYawOffset = prevRenderYawOffset;
        entity.rotationYaw = rotationYaw;
        entity.rotationPitch = rotationPitch;
        entity.prevRotationPitch = prevRotationPitch;
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
