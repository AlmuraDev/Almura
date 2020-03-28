/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client.gui;

/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */

import com.almuradev.almura.feature.notification.ClientNotificationManager;
import com.almuradev.almura.shared.client.GuiConfig;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public final class MembershipGui extends BasicScreen {

    private static final int innerPadding = 2;
    private int lastUpdate = 0;
    private boolean unlockMouse = true;
    private boolean update = true;
    private UILabel messageLabel, droppedLabel, deathTaxLabel;
    private BasicForm form;
    private UIButton buttonRespawn, buttonRevive, buttonClose;
    private UIButton citizen_donation_button, citizen_purchase_button, citizen_skills_button;
    private UIButton explorer_donation_button, explorer_purchase_button, explorer_skills_button;
    private UIButton pioneer_donation_button, pioneer_purchase_button, pioneer_skills_button;

    private EntityPlayer player;
    private boolean isAdmin;
    private int skillsLevel;
    private double availableFunds;
    private int currentMembershipLevel;

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;
    @Inject private static ClientNotificationManager clientNotificationManager;
    @Inject private static PluginContainer container;

    public MembershipGui(EntityPlayer player, boolean isAdmin, int skillsLevel, double availableFunds, int currentMembershipLevel) {
        this.player = player;
        this.isAdmin = isAdmin;
        this.skillsLevel = skillsLevel;
        this.availableFunds = availableFunds;
        this.currentMembershipLevel = currentMembershipLevel;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        this.form = new BasicForm(this, 310, 250, "Almura Memberships");
        this.form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        this.form.setMovable(true);
        this.form.setClosable(true);
        this.form.setBorder(FontColors.WHITE, 1, 185);
        this.form.setBackgroundAlpha(215);
        this.form.setBottomPadding(3);
        this.form.setRightPadding(3);
        this.form.setTopPadding(20);
        this.form.setLeftPadding(3);

        final BasicForm citizenArea = new BasicForm(this, 100, 200, "");
        citizenArea.setPosition(0, 0, Anchor.LEFT | Anchor.TOP);
        citizenArea.setMovable(false);
        citizenArea.setClosable(false);
        citizenArea.setBorder(FontColors.WHITE, 1, 185);
        citizenArea.setBackgroundAlpha(215);
        citizenArea.setBottomPadding(3);
        citizenArea.setRightPadding(3);
        citizenArea.setTopPadding(3);
        citizenArea.setLeftPadding(3);

        final UIImage citizenLogo = new UIImage(this, new GuiTexture(GuiConfig.Location.citizen_logo), null);
        citizenLogo.setSize(99, 99);
        citizenLogo.setPosition(0, -3, Anchor.TOP | Anchor.CENTER);

        final UILabel citizenLabel0 = new UILabel(this, "Chose One");
        citizenLabel0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).underline(true).shadow(true).scale(0.8F).build());
        citizenLabel0.setPosition(0, 125, Anchor.CENTER | Anchor.TOP);

        this.citizen_donation_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, citizenLabel0.getY() + 10)
                .text("$25.00 Donation")
                .listener(this)
                .enabled(currentMembershipLevel == 0)
                .build("button.citizen_donation");

        this.citizen_purchase_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, citizen_donation_button.getY() + 18)
                .text("$2.5 million in-game")
                .listener(this)
                .enabled(availableFunds >= 2500000 && currentMembershipLevel == 0)
                .build("button.citizen_purchase");

        this.citizen_skills_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, citizen_purchase_button.getY() + 18)
                .text("Skills total at 250+")
                .listener(this)
                .enabled(skillsLevel >= 250 && currentMembershipLevel == 0)
                .build("button.citizen_skills");

        citizenArea.add(citizenLogo, citizenLabel0, citizen_donation_button, citizen_skills_button, citizen_purchase_button);

        final BasicForm explorerArea = new BasicForm(this, 100, 200, "");
        explorerArea.setPosition(0, 0, Anchor.CENTER | Anchor.TOP);
        explorerArea.setMovable(false);
        explorerArea.setClosable(false);
        explorerArea.setBorder(FontColors.WHITE, 1, 185);
        explorerArea.setBackgroundAlpha(215);
        explorerArea.setBottomPadding(3);
        explorerArea.setRightPadding(3);
        explorerArea.setTopPadding(3);
        explorerArea.setLeftPadding(3);

        final UIImage explorerLogo = new UIImage(this, new GuiTexture(GuiConfig.Location.explorer_logo), null);
        explorerLogo.setSize(99, 99);
        explorerLogo.setPosition(0, -3, Anchor.TOP | Anchor.CENTER);

        final UILabel explorerLabel0 = new UILabel(this, "Chose One");
        explorerLabel0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).underline(true).shadow(true).scale(0.8F).build());
        explorerLabel0.setPosition(0, 125, Anchor.CENTER | Anchor.TOP);

        this.explorer_donation_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, explorerLabel0.getY() + 10)
                .text("$50.00 Donation")
                .listener(this)
                .enabled(currentMembershipLevel == 0 || currentMembershipLevel == 1)
                .build("button.explorer_donation");

        this.explorer_purchase_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, explorer_donation_button.getY() + 18)
                .text("$5 million in-game")
                .listener(this)
                .enabled(availableFunds >=5000000 && (currentMembershipLevel == 0 || currentMembershipLevel == 1))
                .build("button.explorer_purchase");

        this.explorer_skills_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, explorer_purchase_button.getY() + 18)
                .text("Skills total at 375+")
                .listener(this)
                .enabled(skillsLevel >= 375 && (currentMembershipLevel == 0 || currentMembershipLevel ==1))
                .build("button.explorer_skills");

        explorerArea.add(explorerLogo, explorerLabel0, explorer_donation_button, explorer_skills_button, explorer_purchase_button);

        final BasicForm pioneerArea = new BasicForm(this, 100, 200, "");
        pioneerArea.setPosition(0, 0, Anchor.RIGHT | Anchor.TOP);
        pioneerArea.setMovable(false);
        pioneerArea.setClosable(false);
        pioneerArea.setBorder(FontColors.WHITE, 1, 185);
        pioneerArea.setBackgroundAlpha(215);
        pioneerArea.setBottomPadding(3);
        pioneerArea.setRightPadding(3);
        pioneerArea.setTopPadding(3);
        pioneerArea.setLeftPadding(3);

        final UIImage pioneerLogo = new UIImage(this, new GuiTexture(GuiConfig.Location.pioneer_logo), null);
        pioneerLogo.setSize(99, 99);
        pioneerLogo.setPosition(0, -3, Anchor.TOP | Anchor.CENTER);

        final UILabel pioneerLabel0 = new UILabel(this, "Chose One");
        pioneerLabel0.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).underline(true).shadow(true).scale(0.8F).build());
        pioneerLabel0.setPosition(0, 125, Anchor.CENTER | Anchor.TOP);

        this.pioneer_donation_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, pioneerLabel0.getY() + 10)
                .text("$100.00 Donation")
                .tooltip("Hi")
                .listener(this)
                .enabled(currentMembershipLevel == 0 || currentMembershipLevel == 1 || currentMembershipLevel == 2)
                .build("button.pioneer_donation");

        this.pioneer_purchase_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, pioneer_donation_button.getY() + 18)
                .text("$10 million in-game")
                .listener(this)
                .enabled(availableFunds >= 10000000 && (currentMembershipLevel == 0 || currentMembershipLevel == 1 || currentMembershipLevel == 2))
                .build("button.pioneer_purchase");

        this.pioneer_skills_button = new UIButtonBuilder(this)
                .fontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .hoverFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build())
                .width(40)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .position(0, pioneer_purchase_button.getY() + 18)
                .text("Skills total at 400+")
                .listener(this)
                .enabled(skillsLevel >= 400 && (currentMembershipLevel == 0 || currentMembershipLevel == 1 || currentMembershipLevel == 2))
                .build("button.pioneer_skills");

        pioneerArea.add(pioneerLogo, pioneerLabel0, pioneer_donation_button, pioneer_skills_button, pioneer_purchase_button);

        final UILabel skillsLabel = new UILabel(this, "Current Skills Total: ");
        skillsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(0.8F).build());
        skillsLabel.setPosition(10, -5, Anchor.LEFT | Anchor.BOTTOM);

        final UILabel skillsLevelLabel = new UILabel(this, "" + skillsLevel);
        skillsLevelLabel.setFontOptions(FontOptions.builder().from(FontColors.BLUE_FO).shadow(true).scale(0.8F).build());
        skillsLevelLabel.setPosition(skillsLabel.getX() + skillsLabel.getWidth(), -5, Anchor.LEFT | Anchor.BOTTOM);

        final UISeparator topWindowTitleSeparator = new UISeparator(this);
        topWindowTitleSeparator.setSize(this.form.getWidth() , 1);
        topWindowTitleSeparator.setPosition(0, -5, Anchor.TOP | Anchor.CENTER);
        this.form.add(topWindowTitleSeparator);

        final UISeparator aboveButtonsSeparator = new UISeparator(this);
        aboveButtonsSeparator.setSize(this.form.getWidth() -5, 1);
        aboveButtonsSeparator.setPosition(0, -20, Anchor.BOTTOM | Anchor.CENTER);
        this.form.add(aboveButtonsSeparator);

        this.buttonClose = new UIButtonBuilder(this)
                .width(40)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .text(I18n.format("almura.menu_button.close"))
                //.fontOptions(FontOptions.builder().from(FontColors.RED_FO).shadow(true).build())
                .listener(this)
                .build("button.close");

        this.form.add(citizenArea, explorerArea, pioneerArea, skillsLabel, skillsLevelLabel, this.buttonClose);

        addToScreen(this.form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) throws IOException, URISyntaxException {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.citizen_purchase":
                new PurchaseConfirmGui(this,"Citizen", "$2,500,000",1).display();
                break;

            case "button.explorer_purchase":
                new PurchaseConfirmGui(this,"Explorer", "$5,000,000",2).display();
                break;

            case "button.pioneer_purchase":
                new PurchaseConfirmGui(this, "Pioneer", "$10,000,000",3).display();
                break;

            case "button.citizen_donation":
            case "button.explorer_donation":
            case "button.pioneer_donation":
                Desktop.getDesktop().browse(new URI(GuiConfig.Url.SHOP));
                break;

            case "button.close":
                this.close();
                break;
        }
    }

    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        super.update(mouseX, mouseY, partialTick);

        if (this.unlockMouse && this.lastUpdate == 25) {
            Mouse.setGrabbed(false); // Force the mouse to be visible even though Mouse.isGrabbed() is false.  //#BugsUnited.
            this.unlockMouse = false; // Only unlock once per session.
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (this.player.isDead) {
                return;
            } else {
                this.close();
            }

        }
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
