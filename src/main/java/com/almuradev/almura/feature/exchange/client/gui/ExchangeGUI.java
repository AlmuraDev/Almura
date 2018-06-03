/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.inventory.MalisisSlot;
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
public final class ExchangeGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private UILabel titleLabel;

    private World world;
    private EntityPlayer player;
    private BlockPos blockpos;
    //private UISimpleList<ExchangeManager.BrokerListElementData> exchangeItemList;
    //private UISimpleList<ExchangeManager.DealerListElementData> dealerItemList;
    private UITextField itemSearchField, sellerSearchField;

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

        // Main Panel
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

        // Item Search Area
        final UIFormContainer searchArea = new UIFormContainer(this, 295, 315, "");
        searchArea.setPosition(0, 10, Anchor.LEFT | Anchor.TOP);
        searchArea.setMovable(false);
        searchArea.setClosable(false);
        searchArea.setBorder(FontColors.WHITE, 1, 185);
        searchArea.setBackgroundAlpha(215);
        searchArea.setBottomPadding(3);
        searchArea.setRightPadding(3);
        searchArea.setTopPadding(3);
        searchArea.setLeftPadding(3);

        UILabel searchLabel = new UILabel(this, "Item Name:");
        searchLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        searchLabel.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);

        this.itemSearchField = new UITextField(this, "", false);
        this.itemSearchField.setSize(150, 0);
        this.itemSearchField.setPosition(searchLabel.getWidth() + innerPadding, searchLabel.getY(), Anchor.LEFT | Anchor.TOP);
        this.itemSearchField.setEditable(true);
        this.itemSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Seller Search Area
        UILabel sellerLabel = new UILabel(this, "Seller:");
        sellerLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        sellerLabel.setPosition(itemSearchField.getX() - sellerLabel.getWidth() + innerPadding, searchLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);

        this.sellerSearchField = new UITextField(this, "", false);
        this.sellerSearchField.setSize(150, 0);
        this.sellerSearchField.setPosition(itemSearchField.getX(), sellerLabel.getY(), Anchor.LEFT | Anchor.TOP);
        this.sellerSearchField.setEditable(true);
        this.sellerSearchField.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(false).build());

        // Search button
        final UIButton buttonSearch = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.TOP)
                .position(sellerSearchField.getX() + sellerSearchField.getWidth() + 10, sellerLabel.getY() - 7)
                .text("Search")
                .listener(this)
                .build("button.search");

        // Bottom Page Control - first button
        final UIButton buttonFirstPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("First")
                .listener(this)
                .build("button.first");

        // Bottom Page Control - previous button
        final UIButton buttonPreviousPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(buttonFirstPage.getX() + buttonFirstPage.getWidth() + innerPadding,0)
                .text("Previous")
                .listener(this)
                .build("button.previous");

        // Bottom Page Control - last button
        final UIButton buttonLastPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Last")
                .listener(this)
                .build("button.last");

        // Bottom Page Control - next button
        final UIButton buttonNextPage = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(-(buttonLastPage.getX() + buttonLastPage.getWidth() + innerPadding),0)
                .text("Next")
                .listener(this)
                .build("button.next");

        // Add Elements of Search Area
        searchArea.add(searchLabel, itemSearchField, sellerLabel, sellerSearchField, buttonSearch, buttonFirstPage, buttonPreviousPage, buttonNextPage, buttonLastPage);

        // Economy Pane
        final UIFormContainer economyActionArea = new UIFormContainer(this, 295, 50, "");
        economyActionArea.setPosition(0, searchArea.getY() + searchArea.getHeight() + innerPadding , Anchor.LEFT | Anchor.TOP);
        economyActionArea.setMovable(false);
        economyActionArea.setClosable(false);
        economyActionArea.setBorder(FontColors.WHITE, 1, 185);
        economyActionArea.setBackgroundAlpha(215);
        economyActionArea.setBottomPadding(3);
        economyActionArea.setRightPadding(3);
        economyActionArea.setTopPadding(3);
        economyActionArea.setLeftPadding(3);

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuyStack = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy Stack")
                .listener(this)
                .build("button.buyStack");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuySingle = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy 1")
                .listener(this)
                .build("button.buySingle");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonBuyQuantity = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Buy Quantity")
                .listener(this)
                .build("button.buyQuantity");

        economyActionArea.add(buttonBuyStack, buttonBuySingle, buttonBuyQuantity);

        // Seller Inventory Area
        final UIFormContainer sellerInventoryArea = new UIFormContainer(this, 295, 30, "");
        sellerInventoryArea.setPosition(0, 10, Anchor.RIGHT | Anchor.TOP);
        sellerInventoryArea.setMovable(false);
        sellerInventoryArea.setClosable(false);
        sellerInventoryArea.setBorder(FontColors.WHITE, 1, 185);
        sellerInventoryArea.setBackgroundAlpha(215);
        sellerInventoryArea.setBottomPadding(3);
        sellerInventoryArea.setRightPadding(3);
        sellerInventoryArea.setTopPadding(3);
        sellerInventoryArea.setLeftPadding(3);

        final UISlot exchangeSlot1 = new UISlot(this, new MalisisSlot());
        exchangeSlot1.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot1.setTooltip("Exchange Slot 1");

        final UISlot exchangeSlot2 = new UISlot(this, new MalisisSlot());
        exchangeSlot2.setPosition(exchangeSlot1.getX() + exchangeSlot1.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot2.setTooltip("Exchange Slot 2");

        final UISlot exchangeSlot3 = new UISlot(this, new MalisisSlot());
        exchangeSlot3.setPosition(exchangeSlot2.getX() + exchangeSlot2.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot3.setTooltip("Exchange Slot 3");

        final UISlot exchangeSlot4 = new UISlot(this, new MalisisSlot());
        exchangeSlot4.setPosition(exchangeSlot3.getX() + exchangeSlot3.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot4.setTooltip("Exchange Slot 4");

        final UISlot exchangeSlot5 = new UISlot(this, new MalisisSlot());
        exchangeSlot5.setPosition(exchangeSlot4.getX() + exchangeSlot4.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot5.setTooltip("Exchange Slot 5");

        final UISlot exchangeSlot6 = new UISlot(this, new MalisisSlot());
        exchangeSlot6.setPosition(exchangeSlot5.getX() + exchangeSlot5.getWidth() + 10, 0, Anchor.LEFT | Anchor.MIDDLE);
        exchangeSlot6.setTooltip("Exchange Slot 6");

        sellerInventoryArea.add(exchangeSlot1, exchangeSlot2, exchangeSlot3, exchangeSlot4, exchangeSlot5, exchangeSlot6);

        // Inventory Area Section (right pane)
        final UIFormContainer inventoryArea = new UIFormContainer(this, 295, 315, "");
        inventoryArea.setPosition(0, 45, Anchor.RIGHT | Anchor.TOP);
        inventoryArea.setMovable(false);
        inventoryArea.setClosable(false);
        inventoryArea.setBorder(FontColors.WHITE, 1, 185);
        inventoryArea.setBackgroundAlpha(215);
        inventoryArea.setBottomPadding(3);
        inventoryArea.setRightPadding(3);
        inventoryArea.setTopPadding(3);
        inventoryArea.setLeftPadding(3);



        // Bottom Economy Pane - buyStack button
        final UIButton buttonList = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.LEFT | Anchor.BOTTOM)
                .position(0,0)
                .text("List for Sale")
                .listener(this)
                .build("button.listForSaleButton");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonSetPrice = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.CENTER | Anchor.BOTTOM)
                .position(0,0)
                .text("Set Price")
                .listener(this)
                .build("button.setPrice");

        // Bottom Economy Pane - buyStack button
        final UIButton buttonRemoveItem = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.RIGHT | Anchor.BOTTOM)
                .position(0,0)
                .text("Remove Item")
                .listener(this)
                .build("button.removeItem");

        inventoryArea.add(buttonList, buttonSetPrice, buttonRemoveItem);

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        form.add(titleLabel, searchArea, economyActionArea, sellerInventoryArea, inventoryArea, buttonClose);

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

    protected Consumer<Task> openWindow(String details) {  // Scheduler
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
