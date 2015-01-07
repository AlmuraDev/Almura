/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import java.awt.Color;
import java.util.Arrays;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.GuiModList;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITextField;

public class InfoguideGui extends AlmuraGui{
	
	private UIBackgroundContainer window, uiTitleBar;
	private UIButton modsButton, xButton, closeButton;
	private UILabel titleLabel;
	private UITextField textArea;
	private UISelect dropDownMenu;
	
	public InfoguideGui(AlmuraGui parent) {
		super(parent);
		setup();
	}

	

	@Override
	protected void setup() {
		// Create the window container
		window = new UIBackgroundContainer(this);
		window.setSize(400, 225);
		window.setAnchor(Anchor.CENTER | Anchor.MIDDLE);
		window.setColor(Integer.MIN_VALUE);
		window.setBackgroundAlpha(125);

		final int padding = 4;

		// Create the title & Window layout 
		titleLabel = new UILabel(this, ChatColor.WHITE + "About Almura 2.0");
		titleLabel.setPosition(0, padding + 1, Anchor.CENTER | Anchor.TOP);

		uiTitleBar = new UIBackgroundContainer(this);
		uiTitleBar.setSize(300, 1);
		uiTitleBar.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);
		uiTitleBar.setColor(Color.gray.getRGB());

		xButton = new UIButton(this, ChatColor.BOLD + "X");
		xButton.setSize(5, 1);
		xButton.setPosition(-3, 1, Anchor.RIGHT | Anchor.TOP);
		xButton.setName("button.close");
		xButton.register(this);
		
	    dropDownMenu = new UISelect(this, 200, UISelect.Option.fromList(Arrays.asList("Almura - Banking System",			
		"Almura - Chat System",	
		//"Almura - Control Panel",
		"Almura - Help and Support",
		"Almura - Map Server",
		//"Almura - Member Levels",
		"Almura - News and Events",
		"Almura - Shop Locations",
		"Almura - Upcoming Changes",
		//"Almura - Known Bugs",	
		"Almura - Voice Server",	
		"Almura - Welcome",	
		//"FAQ - Aqualock",	
		"FAQ - Backpack",	
		"FAQ - Becoming a Member",	
		"FAQ - Cheating or Hacking",
		//"FAQ - Client Lag",	
		"FAQ - Farming",	
		//"FAQ - Finding a Recipe",
		"FAQ - Griefing Rules",	
		"FAQ - Jobs and Money",	
		"FAQ - Large Animal Farms",	
		//"FAQ - New PLayers",	
		//"FAQ - Our Server",	
		//"FAQ - Player Accessories",
		//"FAQ - Read Me",
		"FAQ - Residence",	
		"FAQ - Rules",	
		"FAQ - Shops",	
		//"FAQ - Sign Editor",	
		"FAQ - Stargates",	
		"FAQ - Why can't I cut down trees",
		"FAQ - Worlds and Maps")));
	    dropDownMenu.setPosition(5, 20, Anchor.TOP | Anchor.LEFT);
	    dropDownMenu.setMaxExpandedWidth(200);
	    dropDownMenu.select(0);
	    dropDownMenu.register(this);
		//select.maxDisplayedOptions(5);
	    //dropDownMenu.select(0);
	    
		// Create About us multi-line label
		textArea = new UITextField(this, "", true);

		String
		fieldText =
		"Almura 2.0 began June 1st, 2014.  Based on the idea that we could finally get away from the broken and abandoned Spoutcraft client a brilliant developer came to Almura and said,"
				+ " \"Why don't you get rid of that out of date client and move into the present?\" This brilliant developer's name is "
				+ ChatColor.AQUA + "Zidane" + ChatColor.RESET + ". Along with him and another outstanding developer " + ChatColor.AQUA + "Grinch"
				+ ChatColor.RESET + ","
				+ " Almura 2.0 was born.  Using the forge client as our basis these two developers, along with " + ChatColor.GOLD + "Dockter's"
				+ ChatColor.RESET + " content and gui abilities, built, in our opinion, one of the best content loading /"
				+ " gui enabled Minecraft experiences ever conceived. \r \r" + ChatColor.LIGHT_PURPLE + "More info to follow..." + ChatColor.RESET
				+ "";

		textArea.setSize(390, 160);
		textArea.setPosition(0, 40, Anchor.CENTER);
		textArea.setText(fieldText);
		textArea.setTextColor(Color.WHITE.getRGB());
		textArea.setName("mline.aboutus");

		// Create the mods button
		modsButton = new UIButton(this, "Mods");
		modsButton.setSize(50, 16);
		modsButton.setPosition(5, -5, Anchor.LEFT | Anchor.BOTTOM);
		modsButton.setName("button.mods");
		modsButton.register(this);

		// Create the close button
		closeButton = new UIButton(this, "Close");
		closeButton.setSize(50, 16);
		closeButton.setPosition(-5, -5, Anchor.RIGHT | Anchor.BOTTOM);
		closeButton.setName("button.close");
		closeButton.register(this);

		window.add(titleLabel, uiTitleBar, xButton, textArea, modsButton, closeButton,dropDownMenu);

		// Allow the window to move
		new UIMoveHandle(this, window);

		addToScreen(window);
	}

	@Subscribe
	public void onButtonClick(UIButton.ClickEvent event) {
		switch (event.getComponent().getName().toLowerCase()) {
		case "button.mods":
			mc.displayGuiScreen(new GuiModList(this));
			break;
		case "button.close":
			displayParent();
			break;
		}
	}
	
	@Subscribe
	public void onSelectionChanged(UISelect.SelectEvent event) {
		System.out.println("Old: " + event.getOldValue().getLabel());
		System.out.println("New: " + event.getNewValue().getLabel());
		changeGuide(event.getNewValue().getLabel());
	}
	
	public void colorizeGuide(String text) {
		text = text.replaceAll("#0", ChatColor.COLOR_CHAR + "0");
		text = text.replaceAll("#1", ChatColor.COLOR_CHAR + "1");
		text = text.replaceAll("#2", ChatColor.COLOR_CHAR + "2");
		text = text.replaceAll("#3", ChatColor.COLOR_CHAR + "3");
		text = text.replaceAll("#4", ChatColor.COLOR_CHAR + "4");
		text = text.replaceAll("#5", ChatColor.COLOR_CHAR + "5");
		text = text.replaceAll("#6", ChatColor.COLOR_CHAR + "6");
		text = text.replaceAll("#7", ChatColor.COLOR_CHAR + "7");
		text = text.replaceAll("#8", ChatColor.COLOR_CHAR + "8");
		text = text.replaceAll("#9", ChatColor.COLOR_CHAR + "9");
		text = text.replaceAll("#a", ChatColor.COLOR_CHAR + "a");
		text = text.replaceAll("#b", ChatColor.COLOR_CHAR + "b");
		text = text.replaceAll("#c", ChatColor.COLOR_CHAR + "c");
		text = text.replaceAll("#d", ChatColor.COLOR_CHAR + "d");
		text = text.replaceAll("#e", ChatColor.COLOR_CHAR + "e");
		text = text.replaceAll("#f", ChatColor.COLOR_CHAR + "f");
		textArea.setText(text);
	}
	
	public void changeGuide(String guide) {
		String text = "";
		switch (guide) {
		case "Almura - Banking System":
			text =  "#4Hello.";
			colorizeGuide(text);
			break;
			
		case "Almura - Chat System":
			text =  "The following #cChat Channels#f are available to users while in Almura.\r\r"
				    + "#aChat#f = Default Chat Channel for Everyone.\r#aTrade#f = Channel specifically "
				    + "for Buying/Selling/Trading Items. \r#aMember#f = Channel available for#9 Members#f "
				    + "Only.\r#aVeteran#f = Channel available for #6Veterans#f Only.\r#aModerator#f = Channel available "
				    + "for #1Moderators#f Only. \r#aHoliday#f = Channel that makes people talk like "
				    + "#6Pirates#f.\r#aParty#f = Available when you are in a group.  \rType #c/Party#f "
				    + "for all the details.\r\rTo Change Channels, type #c/CHANNELNAME#f then press "
				    + "enter.";
			
			colorizeGuide(text);
			break;
			
		case "Almura - Control Panel":
			break;
			
		case "Almura - Help and Support":
			break;
			
		case "Almura - Map Server":
			break;
			
		case "Almura - Member Levels":
			break;
			
		case "Almura - News and Events":
			break;
			
		case "Almura - Shop Locations":
			break;
			
		case "Almura - Upcoming Changes":
			break;
			
		case "Almura - Known Bugs":
			break;
			
		case "Almura - Voice Server":
			break;
			
		case "Almura - Welcome":
			break;
			
		case "FAQ - Aqualock":
			break;
			
		case "FAQ - Backpack":
			break;
			
		case "FAQ - Becoming a Member":
			break;
			
		case "FAQ - Cheating or Hacking":
			break;
		
		case "FAQ - Client Lag":
			break;
			
		case "FAQ - Farming":
			break;
			
		case "FAQ - Finding a Recipe":
			break;
		
		case "FAQ - Griefing Rules":
			break;
			
		case "FAQ - Jobs and Money":
			break;
			
		case "FAQ - Large Animal Farms":
			break;
			
		case "FAQ - New PLayers":
			break;
			
		case "FAQ - Our Server":
			break;
			
		case "FAQ - Player Accessories":
			break;
			
		case "FAQ - Read Me":
			break;
		
		case "FAQ - Residence":
			break;
			
		case "FAQ - Rules":
			break;
			
		case "FAQ - Shops":
			break;
			
		case "FAQ - Sign Editor":
			break;
			
		case "FAQ - Stargates":
			break;
			
		case "FAQ - Why can't I cut down trees":
			break;
			
		case "FAQ - Worlds and Maps":
			break;
		
		}
	}
}
