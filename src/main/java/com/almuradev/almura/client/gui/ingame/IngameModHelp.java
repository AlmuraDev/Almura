/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.ingame;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.gui.menu.DynamicConfigurationMenu;
import com.almuradev.almurasdk.FileSystem;
import com.almuradev.almurasdk.client.gui.SimpleGui;
import com.almuradev.almurasdk.client.gui.components.UIForm;
import com.almuradev.almurasdk.util.Colors;
import com.almuradev.guide.client.gui.ViewPagesGui;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiShareToLan;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;

import java.awt.AWTException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class IngameModHelp extends SimpleGui {
    
    protected static final ResourceLocation ALMURA_LOGO_LOCATION;
    
    public IngameModHelp() {
    }
    
    static {
        try {
            ALMURA_LOGO_LOCATION = FileSystem.registerTexture(Almura.MOD_ID, "textures/gui/almura.png", Filesystem.CONFIG_GUI_LOGO_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load logo.", e);
        }
    }

    @Override
    public void construct() {
        guiscreenBackground = true;
        // Create the form
        final UIForm form = new UIForm(this, 225, 225, "Almura - Mod Help", true);
        form.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
       
        final int padding = 3;
        
        // Create the Back to Game button
        final UIButton bibButton = new UIButton(this, Colors.AQUA + "Bibliocraft Help");
        bibButton.setSize(180, 8);
        bibButton.setPosition(0, 5, Anchor.CENTER | Anchor.TOP);
        bibButton.setName("button.bib");
        bibButton.register(this);
        
        // Create the Back to Game button
        final UIButton bcButton = new UIButton(this, Colors.AQUA + "Buildcraft Help");
        bcButton.setSize(180, 8);
        bcButton.setPosition(0, getPaddedY(bibButton, padding), Anchor.CENTER | Anchor.TOP);
        bcButton.setName("button.bc");
        bcButton.register(this);
        
        // Create the Back to Game button
        final UIButton carpButton = new UIButton(this, Colors.AQUA + "Carpenter Blocks Help");
        carpButton.setSize(180, 8);
        carpButton.setPosition(0, getPaddedY(bcButton, padding), Anchor.CENTER | Anchor.TOP);
        carpButton.setName("button.carp");
        carpButton.register(this);
        
        // Create the Back to Game button
        final UIButton ic2Button = new UIButton(this, Colors.AQUA + "Industrial Craft Help");
        ic2Button.setSize(180, 8);
        ic2Button.setPosition(0, getPaddedY(carpButton, padding), Anchor.CENTER | Anchor.TOP);
        ic2Button.setName("button.ic2");
        ic2Button.register(this);
        
        // Create the Back to Game button
        final UIButton mfrButton = new UIButton(this, Colors.AQUA + "MineFactoryReloaded Help");
        mfrButton.setSize(180, 8);
        mfrButton.setPosition(0, getPaddedY(ic2Button, padding), Anchor.CENTER | Anchor.TOP);
        mfrButton.setName("button.mfr");
        mfrButton.register(this);
        
        // Create the Back to Game button
        final UIButton teButton = new UIButton(this, Colors.AQUA + "Thermal Expansion Help");
        teButton.setSize(180, 8);
        teButton.setPosition(0, getPaddedY(mfrButton, padding), Anchor.CENTER | Anchor.TOP);
        teButton.setName("button.te");
        teButton.register(this);
        
        // Create the Back to Game button
        final UIButton tfButton = new UIButton(this, Colors.AQUA + "Thermal Foundation Help");
        tfButton.setSize(180, 8);
        tfButton.setPosition(0, getPaddedY(teButton, padding), Anchor.CENTER | Anchor.TOP);
        tfButton.setName("button.tf");
        tfButton.register(this);
        
        // Create the Back to Game button
        final UIButton tcButton = new UIButton(this, Colors.AQUA + "Tinkers Construct Help");
        tcButton.setSize(180, 8);
        tcButton.setPosition(0, getPaddedY(tfButton, padding), Anchor.CENTER | Anchor.TOP);
        tcButton.setName("button.tc");
        tcButton.register(this);
        
        // Create the Back to Game button
        final UIButton rcButton = new UIButton(this, Colors.AQUA + "Railcraft Help");
        rcButton.setSize(180, 8);
        rcButton.setPosition(0, getPaddedY(tcButton, padding), Anchor.CENTER | Anchor.TOP);
        rcButton.setName("button.rc");
        rcButton.register(this);
        
        // Create the Back to Game button
        final UIButton thcButton = new UIButton(this, Colors.AQUA + "Thaumcraft Help");
        thcButton.setSize(180, 8);
        thcButton.setPosition(0, getPaddedY(rcButton, padding), Anchor.CENTER | Anchor.TOP);
        thcButton.setName("button.thc");
        thcButton.register(this);

        // Create the quit button
        final UIButton closeButton = new UIButton(this, "Close");
        closeButton.setSize(50, 8);
        closeButton.setPosition(0, getPaddedY(thcButton, padding + 5), Anchor.CENTER | Anchor.TOP);
        closeButton.setName("button.quit");
        closeButton.register(this);
        
        
      
        form.getContentContainer().add(bibButton, bcButton, ic2Button, mfrButton, carpButton, tfButton, teButton, rcButton,
                tcButton, thcButton, closeButton);        
        addToScreen(form);
    }

    @Subscribe
    public void onButtonClick(UIButton.ClickEvent event) throws IOException, URISyntaxException, AWTException {
        switch (event.getComponent().getName().toLowerCase()) {
        case "button.bib":
            Desktop.getDesktop().browse(new URI("http://www.bibliocraftmod.com/"));                
            break;
        case "button.bc":
            Desktop.getDesktop().browse(new URI("http://www.mod-buildcraft.com/"));                
            break;
        case "button.carp":
            Desktop.getDesktop().browse(new URI("http://www.carpentersblocks.com/"));                
            break;
        case "button.ic2":
            Desktop.getDesktop().browse(new URI("http://wiki.industrial-craft.net/index.php?title=Main_Page"));                
            break;            
        case "button.mfr":
            Desktop.getDesktop().browse(new URI("http://teamcofh.com/docs/minefactory-reloaded/"));                
            break;
        case "button.te":
            Desktop.getDesktop().browse(new URI("http://teamcofh.com/docs/thermal-expansion/"));                
            break;
        case "button.tf":
            Desktop.getDesktop().browse(new URI("http://teamcofh.com/docs/thermal-foundation/"));                
            break;
        case "button.tc":
            Desktop.getDesktop().browse(new URI("http://tinkers-construct.wikia.com/wiki/Tinkers%27_Construct_Wiki"));                
            break;
        case "button.rc":
            Desktop.getDesktop().browse(new URI("http://www.railcraft.info/"));                
            break;
        case "button.thc":
            Desktop.getDesktop().browse(new URI("http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1292130-thaumcraft-4-2-3-5-updated-2015-2-17"));                
            break;
        case "button.quit":
            close();                
            break;
        }
    }
}
