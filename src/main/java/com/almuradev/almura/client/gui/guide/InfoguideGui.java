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
	private UITextField aboutUsLabel;
	private UISelect dropDownMenu;
	
	public InfoguideGui(AlmuraGui parent) {
		super(parent);
		setup();
	}

	

	@Override
	protected void setup() {
		// Create the window container
		window = new UIBackgroundContainer(this);
		window.setSize(300, 225);
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
		
	    dropDownMenu = new UISelect(this, 200, UISelect.Option.fromList(Arrays.asList("Option 1", "Option 2",
				"Very ultra longer option 3", "Shorty", "Moar options", "Even more", "Even Steven", "And a potato too")));
	    dropDownMenu.setPosition(5, 10, Anchor.TOP | Anchor.LEFT);
	    dropDownMenu.setMaxExpandedWidth(120);
		//select.maxDisplayedOptions(5);
	    //dropDownMenu.select(0);
	    
		// Create About us multi-line label
		aboutUsLabel = new UITextField(this, "", true);

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

		aboutUsLabel.setSize(290, 100);
		aboutUsLabel.setPosition(0, 40, Anchor.CENTER);
		aboutUsLabel.setText(fieldText);
		aboutUsLabel.setTextColor(Color.WHITE.getRGB());
		aboutUsLabel.setName("mline.aboutus");

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

		window.add(titleLabel, uiTitleBar, xButton, aboutUsLabel, dropDownMenu, modsButton, closeButton);

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
		System.out.println("Old: " + event.getOldValue());
		System.out.println("New: " + event.getNewValue());		
	}
}
