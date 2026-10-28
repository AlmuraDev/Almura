/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.client.gui;

import com.almuradev.almura.feature.membership.ClientMembershipManager;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UISeparator;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MembershipDetailsGui extends BasicScreen {

    private static final int padding = 4;
    private String membership, value;
    private int membershipLevel;

    public MembershipDetailsGui(@Nullable GuiScreen parent, String membership, String value, int membershipLevel) {
        super(parent, true);
        this.membershipLevel = membershipLevel;
        this.membership = membership;
        this.value = value;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = true;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(250, 200, membership);
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        final UILabel unlocksWorldsLabel = new UILabel("Unlocks Worlds:");
        unlocksWorldsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        unlocksWorldsLabel.setPosition(25, 10, Anchor.LEFT | Anchor.TOP);
        form.add(unlocksWorldsLabel);

        final UILabel cemariaWorldLabel = new UILabel("Cemaria");
        cemariaWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        cemariaWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, unlocksWorldsLabel.getY(), Anchor.LEFT | Anchor.TOP);
        form.add(cemariaWorldLabel);

        final UILabel highgardenWorldLabel = new UILabel("Highgarden");
        highgardenWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        highgardenWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, cemariaWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(highgardenWorldLabel);

        final UILabel forestRiverWorldLabel = new UILabel("ForestRiver");
        forestRiverWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
        forestRiverWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, highgardenWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(forestRiverWorldLabel);

        if (membershipLevel >= 2) {
            final UILabel atlantisWorldLabel = new UILabel("Atlantis");
            atlantisWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
            atlantisWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, forestRiverWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(atlantisWorldLabel);

            final UILabel keystoneWorldLabel = new UILabel("Keystone");
            keystoneWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
            keystoneWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, atlantisWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(keystoneWorldLabel);

            final UILabel marionWorldLabel = new UILabel("Marion");
            marionWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
            marionWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, keystoneWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(marionWorldLabel);

            if (membershipLevel == 3) {
                final UILabel zealWorldLabel = new UILabel("Zeal");
                zealWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
                zealWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, marionWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(zealWorldLabel);

                final UILabel tollanaWorldLabel = new UILabel("Tollana");
                tollanaWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
                tollanaWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, zealWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(tollanaWorldLabel);

                final UILabel othalaWorldLabel = new UILabel("Othala");
                othalaWorldLabel.setFontOptions(FontOptions.builder().from(FontColors.GREEN_FO).shadow(true).scale(1.0F).build());
                othalaWorldLabel.setPosition(unlocksWorldsLabel.getX() + unlocksWorldsLabel.getWidth() + 5, tollanaWorldLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(othalaWorldLabel);
            }
        }

        final UILabel featuresLabel = new UILabel("Features: ");
        featuresLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
        if (membershipLevel == 1) {
            featuresLabel.setPosition(25, 45, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 170);
        }
        if (membershipLevel == 2) {
            featuresLabel.setPosition(25, 75, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 210);
        }
        if (membershipLevel == 3) {
            featuresLabel.setPosition(25, 105, Anchor.LEFT | Anchor.TOP);
            form.setSize(form.getWidth(), 300);
        }

        form.add(featuresLabel);

        final UILabel keepInventoryOnDeathLabel = new UILabel("- Keep Inventory on Death");
        keepInventoryOnDeathLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        keepInventoryOnDeathLabel.setPosition(35, featuresLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(keepInventoryOnDeathLabel);

        final UILabel keepExperienceOnDeathLabel = new UILabel("- Keep Experience on Death");
        keepExperienceOnDeathLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        keepExperienceOnDeathLabel.setPosition(keepInventoryOnDeathLabel.getX(), keepInventoryOnDeathLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(keepExperienceOnDeathLabel);

        final UILabel abilityToReviveLabel = new UILabel("- Ability to Revive");
        abilityToReviveLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        abilityToReviveLabel.setPosition(keepInventoryOnDeathLabel.getX(), keepExperienceOnDeathLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(abilityToReviveLabel);

        final UILabel griefPreventionLeaseLabel = new UILabel("- 90 day Grief Prevention Leases");
        griefPreventionLeaseLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        griefPreventionLeaseLabel.setPosition(keepInventoryOnDeathLabel.getX(), abilityToReviveLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(griefPreventionLeaseLabel);

        final UILabel priorityPlayerTechnicalSupportLabel = new UILabel("- Priority Player Technical Support");
        priorityPlayerTechnicalSupportLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        priorityPlayerTechnicalSupportLabel.setPosition(keepInventoryOnDeathLabel.getX(), griefPreventionLeaseLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(priorityPlayerTechnicalSupportLabel);

        final UILabel earlyAccessToNewContentLabel = new UILabel("- Early access to new content");
        earlyAccessToNewContentLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
        earlyAccessToNewContentLabel.setPosition(keepInventoryOnDeathLabel.getX(), priorityPlayerTechnicalSupportLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
        form.add(earlyAccessToNewContentLabel);

        if (membershipLevel == 1) {
            final UILabel citizenItemSlotsLabel = new UILabel("- 100 item slots on Exchange");
            citizenItemSlotsLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
            citizenItemSlotsLabel.setPosition(keepInventoryOnDeathLabel.getX(), earlyAccessToNewContentLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(citizenItemSlotsLabel);
        }

        if (membershipLevel >= 2) {
            final UILabel abilityToFlyLabel = new UILabel("- Ability to Fly!");
            abilityToFlyLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
            abilityToFlyLabel.setPosition(keepInventoryOnDeathLabel.getX(), earlyAccessToNewContentLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
            form.add(abilityToFlyLabel);

            if (membershipLevel == 2) {
                final UILabel explorerItemSlotsLabel = new UILabel("- 500 item slots on Exchange");
                explorerItemSlotsLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                explorerItemSlotsLabel.setPosition(keepInventoryOnDeathLabel.getX(), abilityToFlyLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(explorerItemSlotsLabel);
            }

            if (membershipLevel == 3) {
                final UILabel pioneerItemSlotLabel = new UILabel("- 5,000 item slots on Exchange");
                pioneerItemSlotLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                pioneerItemSlotLabel.setPosition(keepInventoryOnDeathLabel.getX(), abilityToFlyLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(pioneerItemSlotLabel);

                final UILabel commandsLabel = new UILabel("Additional Commands: ");
                commandsLabel.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).scale(1.0F).build());
                commandsLabel.setPosition(25, pioneerItemSlotLabel.getY() + 15, Anchor.LEFT | Anchor.TOP);
                form.add(commandsLabel);

                final UILabel backCommandLabel = new UILabel("- /back");
                backCommandLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                backCommandLabel.setPosition(keepInventoryOnDeathLabel.getX(), commandsLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(backCommandLabel);

                final UILabel nicknameCommandLabel = new UILabel("- /nickname");
                nicknameCommandLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                nicknameCommandLabel.setPosition(keepInventoryOnDeathLabel.getX(), backCommandLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(nicknameCommandLabel);

                final UILabel anvilCommandLabel = new UILabel("- /anvil");
                anvilCommandLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                anvilCommandLabel.setPosition(keepInventoryOnDeathLabel.getX(), nicknameCommandLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(anvilCommandLabel);

                final UILabel enchantingTableCommandLabel = new UILabel("- /enchantingtable");
                enchantingTableCommandLabel.setFontOptions(FontOptions.builder().from(FontColors.GRAY_FO).shadow(true).scale(1.0F).build());
                enchantingTableCommandLabel.setPosition(keepInventoryOnDeathLabel.getX(), anvilCommandLabel.getY() + 10, Anchor.LEFT | Anchor.TOP);
                form.add(enchantingTableCommandLabel);
            }
        }

        final UISeparator aboveCloseSeparator = new UISeparator(this);
        aboveCloseSeparator.setSize(form.getWidth() -5, 1);
        aboveCloseSeparator.setPosition(0, -18, Anchor.BOTTOM | Anchor.CENTER);

        final UIButton buttonClose = new UIButtonBuilder()
                .text(I18n.format("almura.button.close"))
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .position(0, 0)
                .width(40)
                .listener(this)
                .build("button.close");

        form.add(buttonClose, aboveCloseSeparator);

        addToScreen(form);
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "button.close":
                this.close();
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
