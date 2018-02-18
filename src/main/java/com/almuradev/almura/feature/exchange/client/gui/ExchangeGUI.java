/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.menu.main.SimpleAboutMenu;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.button.UISimpleButton;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class ExchangeGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel titleLabel;

    private World world;
    private EntityPlayer player;
    private BlockPos blockpos;
    //private UISimpleList<SimpleAboutMenu.AboutListElementData> list;
    private UITextField textField;

    @Inject private static PluginContainer container;

    public ExchangeGUI(EntityPlayer player, World worldIn, BlockPos blockpos) {
        this.player = player;
        this.world = worldIn;
        this.blockpos = blockpos;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final UIFormContainer form = new UIFormContainer(this, 600, 400, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        UILabel titleLabel = new UILabel(this, "Almura Grand Exchange");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        final UIFormContainer searchArea = new UIFormContainer(this, 295, 250, "");
        searchArea.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        searchArea.setMovable(false);
        searchArea.setClosable(false);
        searchArea.setBorder(FontColors.WHITE, 1, 185);
        searchArea.setBackgroundAlpha(215);
        searchArea.setBottomPadding(3);
        searchArea.setRightPadding(3);
        searchArea.setTopPadding(3);
        searchArea.setLeftPadding(3);

        final UIFormContainer inventoryArea = new UIFormContainer(this, 295, 350, "");
        inventoryArea.setPosition(0, 10, Anchor.RIGHT | Anchor.TOP);
        inventoryArea.setMovable(false);
        inventoryArea.setClosable(false);
        inventoryArea.setBorder(FontColors.WHITE, 1, 185);
        inventoryArea.setBackgroundAlpha(215);
        inventoryArea.setBottomPadding(3);
        inventoryArea.setRightPadding(3);
        inventoryArea.setTopPadding(3);
        inventoryArea.setLeftPadding(3);


        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        form.add(titleLabel, searchArea, inventoryArea, buttonClose);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }

    Consumer<Task> openWindow(String details) {  // Scheduler
        return task -> {

            if (details.equalsIgnoreCase("purchaseRequest")) {
                // Packet to send request.
            }
        };
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);
        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {}
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);
        this.lastUpdate = 0; // Reset the timer when key is typed.
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        this.lastUpdate = 0; // Reset the timer when mouse is pressed.
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Can't stop the game otherwise the Sponge Scheduler also stops.
    }
}
