package com.almuradev.almura.feature.shop.gui;

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.feature.notification.type.PopupNotification;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

public class ShopListGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel titleLabel, nameLabel, idLabel;
    private UIButton buttonAdd, buttonRemove, buttonDetails;
    private UIFormContainer form;
    private UIPanel panel;
    private int screenWidth = 300;
    private int screenHeight = 300;

    @Inject @ChannelId(NetworkConfig.CHANNEL) private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        form = new UIFormContainer(this, this.screenWidth, this.screenHeight, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Server Shops");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Tab Area
        final UIFormContainer shopListArea = new UIFormContainer(this, 285, 260, "");
        shopListArea.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);
        shopListArea.setMovable(false);
        shopListArea.setClosable(false);
        shopListArea.setBorder(FontColors.WHITE, 1, 185);
        shopListArea.setBackgroundAlpha(215);
        shopListArea.setPadding(3, 3);

        nameLabel = new UILabel(this, "Shop Name");
        nameLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        nameLabel.setPosition(-50, 10, Anchor.CENTER | Anchor.TOP);

        idLabel = new UILabel(this, "Shop ID");
        idLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        idLabel.setPosition(50, 10, Anchor.CENTER | Anchor.TOP);

        // Admin Add button
        this.buttonAdd = new UIButtonBuilder(this)
                .width(10)
                .text(Text.of(TextColors.GREEN, "+"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.add");

        // Admin Details button
        this.buttonDetails = new UIButtonBuilder(this)
                .width(10)
                .x(12)
                .text(Text.of(TextColors.YELLOW, "?"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.details");

        // Admin Remove button
        this.buttonRemove = new UIButtonBuilder(this)
                .width(10)
                .x(24)
                .text(Text.of(TextColors.RED, "-"))
                .anchor(Anchor.BOTTOM | Anchor.LEFT)
                .listener(this)
                .build("button.remove");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.guide.button.close"))
                .listener(this)
                .build("button.close");

        form.add(titleLabel, shopListArea, nameLabel, idLabel, buttonAdd, buttonDetails, buttonRemove, buttonClose);

        // Detect if screen area is large enough to display.
        if (screenWidth > resolution.getScaledWidth() || screenHeight > resolution.getScaledHeight()) {
            clientNotificationManager.queuePopup(new PopupNotification(Text.of("Admin Shop Error"), Text.of("Screen area of: " + screenHeight + " x " + screenWidth + " required."), 5));
            this.close();
        } else {
            addToScreen(form);
        }
    }

    private UIContainer buyTab() {
        UIContainer buyTab = new UIContainer<>(this);
        final UIFormContainer buyItemArea = new UIFormContainer(this, 215, 200, "");
        buyItemArea.setPosition(0, -3, Anchor.CENTER | Anchor.TOP);
        buyItemArea.setMovable(false);
        buyItemArea.setClosable(false);
        buyItemArea.setBorder(FontColors.WHITE, 1, 185);
        buyItemArea.setBackgroundAlpha(215);
        buyItemArea.setPadding(3, 3);

        //buyTab.add(buyItemArea);

        return buyTab;
    }

    private UIContainer sellTab() {
        UIContainer sellTab = new UIContainer<>(this);

        final UIFormContainer sellItemArea = new UIFormContainer(this, 215, 200, "");
        sellItemArea.setPosition(0, -3, Anchor.CENTER | Anchor.TOP);
        sellItemArea.setMovable(false);
        sellItemArea.setClosable(false);
        sellItemArea.setBorder(FontColors.WHITE, 1, 185);
        sellItemArea.setBackgroundAlpha(215);
        sellItemArea.setPadding(3, 3);

        //sellTab.add(sellItemArea);
        return sellTab;
    }


    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            unlockMouse = false; // Only unlock once per session.
        }

        if (++this.lastUpdate > 100) {
        }
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
