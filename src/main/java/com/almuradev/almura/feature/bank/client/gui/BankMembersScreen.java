/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.client.gui;

import com.almuradev.almura.feature.bank.BankAccountView;
import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.client.ClientBankManager;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * The owner's view of who shares an account: add players by name (depositing, and withdrawing if allowed), change whether they may withdraw,
 * remove them.
 */
@SideOnly(Side.CLIENT)
public final class BankMembersScreen extends BasicScreen implements BankScreen.Child {

    @Inject private static ClientBankManager manager;

    private final BankScreen bankScreen;
    private final int account;
    private BasicList<BankAccountView.Member> memberList;
    private BasicTextBox nameTextBox;
    private UICheckBox canWithdrawCheckBox;
    private UIButton buttonToggle, buttonRemove;

    BankMembersScreen(final BankScreen parent, final int account) {
        super(parent, true);
        this.bankScreen = parent;
        this.account = account;
    }

    @Override
    public BankScreen getBankScreen() {
        return this.bankScreen;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final String accountName = manager.getAccount(this.account).map(BankAccountView::getName).orElse("");
        final BasicForm form = new BasicForm(250, 210, I18n.format("almura.feature.bank.title.members", accountName));
        form.setZIndex(10);
        form.setBackgroundAlpha(255);

        // Add a member
        this.nameTextBox = new BasicTextBox("");
        this.nameTextBox.setSize(100, 0);
        this.nameTextBox.setPosition(0, 1);
        this.nameTextBox.setAcceptsReturn(false);
        this.nameTextBox.setAcceptsTab(false);
        this.nameTextBox.setTooltip(I18n.format("almura.feature.bank.tooltip.member_name"));
        this.nameTextBox.setOnEnter(tb -> this.add());

        this.canWithdrawCheckBox = new UICheckBox(I18n.format("almura.feature.bank.checkbox.can_withdraw"));
        this.canWithdrawCheckBox.setFontOptions(FontOptions.builder().from(FontColors.WHITE_FO).shadow(true).build());
        this.canWithdrawCheckBox.setPosition(BasicScreen.getPaddedX(this.nameTextBox, 4), 3);

        final UIButton buttonAdd = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.add"))
            .width(36)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .position(0, 0)
            .onClick(this::add)
            .build("button.add");

        // Members
        this.memberList = new BasicList<>(BasicScreen.getPaddedWidth(form), BasicScreen.getPaddedHeight(form) - 38);
        this.memberList.setBorder(FontColors.WHITE, 1, 185);
        this.memberList.setPadding(2, 2);
        this.memberList.setItemComponentSpacing(1);
        this.memberList.setItemComponentFactory(MemberComponent::new);
        this.memberList.setPosition(0, 17);
        this.memberList.setSelectConsumer(member -> this.updateButtons());

        this.buttonToggle = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.toggle_withdraw"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .onClick(this::toggleWithdraw)
            .build("button.toggle");

        this.buttonRemove = new UIButtonBuilder()
            .text(TextFormatting.RED + I18n.format("almura.feature.bank.button.remove"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(BasicScreen.getPaddedX(this.buttonToggle, 2), 0)
            .onClick(this::remove)
            .build("button.remove");

        final UIButton buttonClose = new UIButtonBuilder()
            .text(I18n.format("almura.button.close"))
            .width(50)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::close)
            .build("button.close");

        form.add(this.nameTextBox, this.canWithdrawCheckBox, buttonAdd, this.memberList, this.buttonToggle, this.buttonRemove, buttonClose);
        this.addToScreen(form);

        this.onBankRefresh();
        this.nameTextBox.focus();
    }

    @Override
    public void onBankRefresh() {
        final BankAccountView view = manager.getAccount(this.account).orElse(null);
        if (view == null || !view.isMine()) {
            this.close(); // Deleted
            return;
        }
        final BankAccountView.Member selected = this.memberList.getSelectedItem();
        this.memberList.setItems(view.getMembers());
        this.memberList.setSelectedItem(view.getMembers().stream()
            .filter(member -> selected != null && member.getUniqueId().equals(selected.getUniqueId()))
            .findFirst()
            .orElse(null));
        this.updateButtons();
    }

    private void updateButtons() {
        final boolean selected = this.memberList.getSelectedItem() != null;
        this.buttonToggle.setEnabled(selected);
        this.buttonRemove.setEnabled(selected);
    }

    private void add() {
        final String name = this.nameTextBox.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        manager.request(BankAction.ADD_MEMBER, this.account, name, BigDecimal.ZERO, "", this.canWithdrawCheckBox.isChecked());
        this.nameTextBox.setText("");
    }

    private void toggleWithdraw() {
        final BankAccountView.Member member = this.memberList.getSelectedItem();
        if (member != null) {
            manager.request(BankAction.SET_MEMBER_WITHDRAW, this.account, "", BigDecimal.ZERO, member.getUniqueId().toString(),
                !member.canWithdraw());
        }
    }

    private void remove() {
        final BankAccountView.Member member = this.memberList.getSelectedItem();
        if (member != null) {
            manager.request(BankAction.REMOVE_MEMBER, this.account, "", BigDecimal.ZERO, member.getUniqueId().toString(), false);
            this.memberList.setItems(Collections.emptyList());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static final class MemberComponent extends BasicList.ItemComponent<BankAccountView.Member> {

        MemberComponent(final MalisisGui gui, final BasicList<BankAccountView.Member> parent, final BankAccountView.Member item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            this.setSize(0, 16);

            final UILabel nameLabel = new UILabel(this.item.getName());
            nameLabel.setFontOptions(FontColors.WHITE_FO);
            nameLabel.setPosition(2, 0, Anchor.MIDDLE | Anchor.LEFT);

            final UILabel accessLabel = new UILabel(this.item.canWithdraw()
                ? TextFormatting.GREEN + I18n.format("almura.feature.bank.text.can_withdraw")
                : TextFormatting.GRAY + I18n.format("almura.feature.bank.text.member_deposit_only"));
            accessLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().scale(0.8f).build());
            accessLabel.setPosition(-2, 0, Anchor.MIDDLE | Anchor.RIGHT);

            this.add(nameLabel, accessLabel);
        }
    }
}
