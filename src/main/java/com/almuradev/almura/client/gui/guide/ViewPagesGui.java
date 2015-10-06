/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.client.PermissionsHelper;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.components.UIForm;
import com.almuradev.almura.client.network.play.C00PageInformation;
import com.almuradev.almura.content.Page;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.content.PageUtil;
import com.almuradev.almura.event.PageDeleteEvent;
import com.almuradev.almura.event.PageInformationEvent;
import com.almuradev.almura.server.network.play.S01PageDelete;
import com.almuradev.almura.util.Color;
import com.almuradev.almura.util.Colors;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ViewPagesGui extends SimpleGui {

    public static final Color CONTROL = new Color("control", 13158600);
    public static final Function<Page, String> FUNCTION_LABEL_NAME = new Function<Page, String>() {
        @Override
        public String apply(Page page) {
            return page.getName();
        }
    };

    private final int internalPadding = 2;
    private final int externalPadding = 4;

    private UIForm form;
    private UISelect<Page> selectPage;
    private UITextField textFieldContents;
    private UIButton buttonStyled, buttonRaw, buttonDetails, buttonDelete, buttonAdd, buttonClose, buttonSave;

    @Override
    public void construct() {
        guiscreenBackground = false;

        form = new UIForm(this, 300, 225, "Guide");
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
        form.setName("form.guide.main");
        form.setColor(CONTROL.getGuiColorCode());
        form.setBackgroundAlpha(255);

        selectPage = new UISelect<>(this, 140, populate());
        selectPage.setAnchor(Anchor.TOP | Anchor.RIGHT);
        selectPage.setPosition(-externalPadding, externalPadding);
        selectPage.setSize(160, 15);
        selectPage.setName("form.guide.view.select.page");
        selectPage.setLabelFunction(FUNCTION_LABEL_NAME);
        selectPage.setColors(selectPage.getBgColor(), Colors.DARK_GRAY.getGuiColorCode());
        selectPage.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        selectPage.getFontRenderOptions().shadow = false;
        selectPage.getHoveredFontRendererOptions().color = Colors.GRAY.getGuiColorCode();
        selectPage.getHoveredFontRendererOptions().shadow = false;
        selectPage.getSelectedFontRendererOptions().color = Colors.YELLOW.getGuiColorCode();
        selectPage.getSelectedFontRendererOptions().shadow = false;
        selectPage.getSelectedFontRendererOptions().bold = true;
        selectPage.maxDisplayedOptions(15);
        selectPage.register(this);

        buttonDetails = new UIButton(this, "?");
        buttonDetails.setAnchor(Anchor.TOP | Anchor.RIGHT);
        buttonDetails.setPosition(SimpleGui.getPaddedX(selectPage, internalPadding, Anchor.RIGHT), externalPadding);
        buttonDetails.setSize(0, 15);
        buttonDetails.setName("form.guide.view.button.details");
        buttonDetails.setVisible(false);
        buttonDetails.getFontRenderOptions().color = Colors.GOLD.getGuiColorCode();
        buttonDetails.getFontRenderOptions().shadow = false;
        buttonDetails.getHoveredFontRendererOptions().color = Colors.GOLD.getGuiColorCode();
        buttonDetails.getHoveredFontRendererOptions().shadow = false;
        buttonDetails.setTooltip(new UITooltip(this, "Details of this page", 20));
        buttonDetails.register(this);

        buttonAdd = new UIButton(this, "+");
        buttonAdd.setAnchor(Anchor.TOP | Anchor.RIGHT);
        buttonAdd.setPosition(SimpleGui.getPaddedX(buttonDetails.isVisible() ? buttonDetails : selectPage, internalPadding, Anchor.RIGHT), externalPadding);
        buttonAdd.setName("form.guide.view.button.add");
        buttonAdd.setVisible(true);
        buttonAdd.getFontRenderOptions().color = Colors.GREEN.getGuiColorCode();
        buttonAdd.getFontRenderOptions().shadow = false;
        buttonAdd.getHoveredFontRendererOptions().shadow = false;
        buttonAdd.setTooltip(new UITooltip(this, "Add a new page", 20));
        buttonAdd.register(this);

        buttonDelete = new UIButton(this, "-");
        buttonDelete.setAnchor(Anchor.TOP | Anchor.RIGHT);
        buttonDelete.setPosition(SimpleGui.getPaddedX(buttonAdd, internalPadding, Anchor.RIGHT), externalPadding);
        buttonDelete.setName("form.guide.view.button.delete");
        buttonDelete.setVisible(false);
        buttonDelete.getFontRenderOptions().color = Colors.RED.getGuiColorCode();
        buttonDelete.getFontRenderOptions().shadow = false;
        buttonDelete.getHoveredFontRendererOptions().shadow = false;
        buttonDelete.setTooltip(new UITooltip(this, "Delete this page", 20));
        buttonDelete.register(this);

        buttonClose = new UIButton(this, "Close");
        buttonClose.setAnchor(Anchor.BOTTOM | Anchor.RIGHT);
        buttonClose.setPosition(-externalPadding, -externalPadding);
        buttonClose.setSize(0, 15);
        buttonClose.setName("form.guide.view.button.close");
        buttonClose.getFontRenderOptions().shadow = false;
        buttonClose.getHoveredFontRendererOptions().shadow = false;
        buttonClose.register(this);

        buttonSave = new UIButton(this, "Save");
        buttonSave.setAnchor(Anchor.BOTTOM | Anchor.RIGHT);
        buttonSave.setPosition(SimpleGui.getPaddedX(buttonClose, internalPadding, Anchor.RIGHT), -externalPadding);
        buttonSave.setSize(0, 15);
        buttonSave.setName("form.guide.view.button.save");
        buttonSave.setVisible(false);
        buttonSave.getFontRenderOptions().shadow = false;
        buttonSave.getHoveredFontRendererOptions().shadow = false;
        buttonSave.register(this);

        textFieldContents = new UITextField(this, true);
        textFieldContents.setPosition(externalPadding, SimpleGui.getPaddedY(selectPage, internalPadding));
        textFieldContents.setSize(form.getWidth() - (externalPadding * internalPadding), form.getContentHeight() - textFieldContents.getY() -
                externalPadding - (buttonClose.getHeight() * internalPadding));
        textFieldContents.setOptions(Colors.DARK_GRAY.getGuiColorCode(), CONTROL.getGuiColorCode(), Colors.BLACK.getGuiColorCode());
        textFieldContents.getFontRenderOptions().fontScale = 0.8f;
        textFieldContents.getFontRenderOptions().color = Colors.WHITE.getGuiColorCode();
        textFieldContents.getFontRenderOptions().shadow = false;
        textFieldContents.getScrollbar().setAutoHide(true);
        textFieldContents.setEditable(false);

        buttonStyled = new UIButton(this, "Styled");
        buttonStyled.setAnchor(Anchor.BOTTOM | Anchor.LEFT);
        buttonStyled.setPosition(externalPadding, -externalPadding);
        buttonStyled.setSize(0, 12);
        buttonStyled.setName("form.guide.view.button.styled");
        buttonStyled.setVisible(false);
        buttonStyled.getFontRenderOptions().italic = true;
        buttonStyled.getFontRenderOptions().shadow = false;
        buttonStyled.getHoveredFontRendererOptions().italic = true;
        buttonStyled.getHoveredFontRendererOptions().shadow = false;
        buttonStyled.setTooltip(new UITooltip(this, "Styles the page text", 20));
        buttonStyled.register(this);

        buttonRaw = new UIButton(this, "</>");
        buttonRaw.setAnchor(Anchor.BOTTOM | Anchor.LEFT);
        buttonRaw.setPosition(SimpleGui.getPaddedX(buttonStyled, internalPadding), buttonStyled.getY());
        buttonRaw.setSize(0, 12);
        buttonRaw.setName("form.guide.view.button.raw");
        buttonRaw.setVisible(false);
        buttonRaw.getFontRenderOptions().shadow = false;
        buttonRaw.getHoveredFontRendererOptions().shadow = false;
        buttonRaw.setTooltip(new UITooltip(this, "Shows the raw text behind the styled page", 20));
        buttonRaw.register(this);

        form.getContentContainer().add(buttonStyled, buttonRaw, selectPage, textFieldContents, buttonDetails, buttonDelete, buttonAdd,
                buttonSave, buttonClose);

        addToScreen(form);

        selectPage.selectFirst();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onClose() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        super.keyTyped(keyChar, keyCode);

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            new ConfirmGui("Be sure you read the Membership and Rules guide.", "Close anyways?", "Guide", false).display();
        }
        
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_F5)) {
            Page selected = null;

            if (selectPage.getSelectedValue() != null) {
                selected = selectPage.getSelectedValue();
            }

            PageUtil.loadAll();

            final List<Page> options = populate();
            selectPage.setOptions(options);

            if (!options.isEmpty()) {
                if (selected != null && selectPage.getOption(selected) != null) {
                    selectPage.select(selected);
                } else {
                    selectPage.selectFirst();
                }
            }
        }
    }
    
    @Override
    public void update(int mouseX, int mouseY, float partialTick) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof ViewPagesGui)) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof CreatePageGui)) {
                if (!(Minecraft.getMinecraft().currentScreen instanceof ModifyPageGui)) {
                    this.close();
                }
            }
        }
    }

    @Subscribe
    public void onUIButtonClickEvent(UIButton.ClickEvent event) {
        switch (event.getComponent().getName().toLowerCase()) {
            case "form.guide.view.button.styled":
                textFieldContents.setText(PageUtil.replaceColorCodes("&", textFieldContents.getText(), true));
                break;
            case "form.guide.view.button.raw":
                textFieldContents.setText(PageUtil.replaceColorCodes("&", textFieldContents.getText(), false));
                break;
            case "form.guide.view.button.details":
                new ModifyPageGui(this, selectPage.getSelectedValue()).display();
                break;
            case "form.guide.view.button.add":
                new CreatePageGui(this).display();
                break;
            case "form.guide.view.button.delete":
                CommonProxy.NETWORK_FORGE.sendToServer(new S01PageDelete(selectPage.getSelectedOption().getKey().getIdentifier()));
                break;
            case "form.guide.view.button.close":
                close();
                break;
            case "form.guide.view.button.save":
                if (selectPage.getSelectedOption() != null) {
                    final Page page = selectPage.getSelectedOption().getKey();
                    if (PermissionsHelper.hasPermission(PermissionsHelper.PERMISSIBLE_GUIDE, "save." + page.getIdentifier())) {
                        CommonProxy.NETWORK_FORGE.sendToServer(
                                new C00PageInformation(page.getIdentifier(), page.getIndex(), page.getName(), textFieldContents.getText()));
                    }
                }
                break;
        }
    }

    @Subscribe
    public void onUISelectEvent(@SuppressWarnings("rawtypes") UISelect.SelectEvent event) {
        if (event.getNewValue() == null) {
            updateGui(null);
            return;
        }
        if (event.getComponent().getName().equalsIgnoreCase("form.guide.view.select.page")) {
            updateGui((Page) event.getNewValue());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPageInformationEvent(PageInformationEvent event) {
        selectPage.setOptions(populate());

        if (selectPage.getSelectedValue() != null && selectPage.getSelectedValue().getIdentifier().equals(event.page.getIdentifier())) {
            final String copyContents = textFieldContents.getText();
            selectPage.setSelectedOption(event.page);
            updateGui(selectPage.getSelectedValue());
            textFieldContents.setText(copyContents);
        } else {
            selectPage.setSelectedOption(event.page);
            updateGui(selectPage.getSelectedValue());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPageDeleteEvent(PageDeleteEvent event) {
        selectPage.setOptions(populate());
        if (selectPage.getSelectedValue() != null && Objects.equals(selectPage.getSelectedValue().getIdentifier(), event.identifier)) {
            selectPage.setSelectedOption(selectPage.selectFirst());
            updateGui(selectPage.getSelectedValue());
        }
    }

    public void selectPage(Page page) {
        selectPage.setSelectedOption(page);
        updateGui(selectPage.getSelectedValue());
    }

    private List<Page> populate() {
        final List<Page> pageList = Lists.newArrayList(PageRegistry.getAll().values());
        Collections.sort(pageList, new Page.PageIndexComparator());
        return pageList;
    }

    private void updateGui(Page page) {
        //final boolean hasCreatePermission = ClientProxy.getPermissions().hasPermission("create");
        //final boolean hasSavePermission = ClientProxy.getPermissions().hasPermission("save." + page.getIdentifier());
        //final boolean hasDeletePermission = ClientProxy.getPermissions().hasPermission("delete." + page.getIdentifier());
        final boolean hasCreatePermission = canChange(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
        final boolean hasSavePermission = canChange(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
        final boolean hasDeletePermission = canChange(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
        
        if (page == null) {
            form.setTitle("Guide");
            textFieldContents.setText("");
            textFieldContents.setEditable(false);
            buttonStyled.setVisible(false);
            buttonRaw.setVisible(false);
            buttonDelete.setVisible(false);
            buttonDetails.setVisible(false);
            buttonSave.setVisible(false);
            buttonAdd.setVisible(hasCreatePermission);
            buttonAdd.setPosition(SimpleGui.getPaddedX(buttonDetails.isVisible() ? buttonDetails : selectPage, internalPadding, Anchor.RIGHT), externalPadding);
            buttonDelete.setPosition(SimpleGui.getPaddedX(buttonAdd, internalPadding, Anchor.RIGHT), externalPadding);
            return;
        }

        form.setTitle("Guide - " + page.getName());
        textFieldContents.setText(page.getContents());
        Keyboard.enableRepeatEvents(true);

        // Show formatting buttons if user has save permission
        buttonStyled.setVisible(hasSavePermission);
        buttonRaw.setVisible(hasSavePermission);
        textFieldContents.setEditable(hasSavePermission);

        // Show delete ('-') button when player has delete permission
        buttonDelete.setVisible(hasDeletePermission);

        // Show add ('+') button when player has add permission
        buttonAdd.setVisible(hasCreatePermission);

        // Show save button when player has save permission
        buttonSave.setVisible(hasSavePermission);

        // Show the details button
        buttonDetails.setVisible(true);

        // Adjust position of delete and add buttons based on visibility of buttonDetails
        buttonAdd.setPosition(SimpleGui.getPaddedX(buttonDetails.isVisible() ? buttonDetails : selectPage, internalPadding, Anchor.RIGHT), externalPadding);
        buttonDelete.setPosition(SimpleGui.getPaddedX(buttonAdd, internalPadding, Anchor.RIGHT), externalPadding);
    }
    
    public boolean canChange(String name) {
        return name.equalsIgnoreCase("tunnel_brat") || name.equalsIgnoreCase("mcsnetworks") || name.equalsIgnoreCase("gregabyte") || name
                .equalsIgnoreCase("wolfeyeamd0") || name.equalsIgnoreCase("wifee");
    }
}
