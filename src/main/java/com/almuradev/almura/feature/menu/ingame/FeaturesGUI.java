/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.ingame;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.exchange.ClientExchangeManager;
import com.almuradev.almura.feature.guide.ClientPageManager;
import com.almuradev.almura.feature.nick.ClientNickManager;
import com.almuradev.almura.feature.store.client.gui.StoreScreen;
import com.almuradev.almura.feature.store.client.gui.StoreListScreen;
import com.almuradev.almura.feature.title.ClientTitleManager;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.renderer.font.FontOptions;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class FeaturesGUI extends SimpleScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean isAdmin = false;
    private UILabel titleLabel;
    private UIButton guideButton, manageTitleButton, nicknameButton, exchangeButton, serverShopButton, npcShopButton, accessoriesButton;

    private World world;
    private EntityPlayerSP player;

    @Inject private static PluginContainer container;
    @Inject private static ClientExchangeManager exchangeManager;
    @Inject private static ClientPageManager guideManager;
    @Inject private static ClientNickManager nickManager;
    @Inject private static ClientTitleManager titleManager;

    public FeaturesGUI(EntityPlayerSP player, World worldIn, boolean isAdmin) {
        this.player = player;
        this.world = worldIn;
        this.isAdmin = isAdmin;
    }

    @Override
    public void construct() {
        guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        // Main Panel
        final UIFormContainer form = new UIFormContainer(this, 150, 230, "");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setMovable(true);
        form.setClosable(true);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setBackgroundAlpha(215);
        form.setBottomPadding(3);
        form.setRightPadding(3);
        form.setTopPadding(20);
        form.setLeftPadding(3);

        titleLabel = new UILabel(this, "Almura Features");
        titleLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.1F).build());
        titleLabel.setPosition(0, -15, Anchor.CENTER | Anchor.TOP);

        // Guide button
        guideButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Guide")
                .listener(this)
                .build("button.guide");

        // Nickname button
        nicknameButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Nicknames")
                .position(0, guideButton.getY() + 18)
                .listener(this)
                .build("button.nickname");

       // Manage Titles button
       manageTitleButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("Manage Titles")
                .position(0, nicknameButton.getY() + 18)
                .listener(this)
                .build("button.title.manage");

       // Exchange button
       exchangeButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, manageTitleButton.getY() + 18)
                .visible(isAdmin)
                .text("Exchange")
                .listener(this)
                .build("button.exchange");

       // Server Shop Configuration GUI button
       serverShopButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, exchangeButton.getY() + 18)
                .visible(isAdmin)
                .text("Server Shops")
                .listener(this)
                .build("button.servershop");

       // Server Shop - NPC Shop GUI button
       npcShopButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, serverShopButton.getY() + 18)
                .visible(isAdmin)
                .text("NPC Shop")
                .listener(this)
                .build("button.npcshop");

       // Accessories button
       accessoriesButton = new UIButtonBuilder(this)
                .width(100)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, npcShopButton.getY() + 18)
                .text("Accessories")
                .visible(isAdmin)
                .listener(this)
                .build("button.accessories");

        // Close button
        final UIButton buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(Text.of("almura.button.close"))
                .listener(this)
                .build("button.close");

        form.add(titleLabel, guideButton, exchangeButton,  manageTitleButton, nicknameButton, accessoriesButton, serverShopButton, npcShopButton, buttonClose);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.exchange":
                exchangeManager.requestExchangeGUI();
                break;
            case "button.npcshop":
                // Todo: need packet based request here.
                new StoreScreen(true).display(); //isAdmin TRUE
                break;
            case "button.servershop":
                // Todo: need packet based request here.
                new StoreListScreen().display();
                break;
            case "button.title.manage":
                titleManager.requestManageTitlesGUI();
                break;
            case "button.guide":
                guideManager.requestGuideGUI();
                break;
            case "button.nickname":
                nickManager.requestNicknameGUI();
                break;
            case "button.close":
                this.close();
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

