/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.gui.guide;

import java.util.Arrays;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.TabPosition;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.control.UICloseHandle;
import net.malisis.core.client.gui.component.control.UIMoveHandle;
import net.malisis.core.client.gui.component.control.UIResizeHandle;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UIMultiLineLabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UIRadioButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UISlider;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import com.almuradev.almura.client.gui.AlmuraGui;
import com.google.common.eventbus.Subscribe;

public class ExampleGUI extends AlmuraGui
{
	private UIPanel panel;
	private UITab tab1;
	private UIButton btn1, btn2, btnOver;
	private UICheckBox cb;	

	public ExampleGUI(AlmuraGui parent) {
		super(parent);		
		setup();
	}

	@Subscribe
	public void onSliderChanged(ValueChange<UISlider, Float> event)
	{
		int v = (int) (event.getNewValue() / 100 * 255);
		int g = Math.abs(-255 + 2 * v);
		int r = v;
		int b = 255 - g;
		//MalisisCore.message(r + " > " + Integer.toHexString(r << 16 | 0x00FFFF));
		tab1.setColor(r << 16 | g << 8 | b);
	}

	@Override
	protected void setup() {		
		guiscreenBackground = false;

		/**
		 * CONTAINER 1
		 */
		cb = new UICheckBox(this, "CheckBox with label").setTooltip(new UITooltip(this, EnumChatFormatting.AQUA + "with a tooltip!", 5));

		UIRadioButton rb1 = new UIRadioButton(this, "newRb", "Radio value 1").setPosition(0, 14);
		UIRadioButton rb2 = new UIRadioButton(this, "newRb", "Radio value 2").setPosition(rb1.getWidth() + 10, 14);

		UISlider slider = new UISlider(this, 150, 0, 100, "Slider value : %.0f").setPosition(0, 26).register(this);

		UITextField tf = new UITextField(this, "This textfield will only accept numbers.");
		tf.setSize(200, 0);
		tf.setPosition(0, 52);
		tf.setAutoSelectOnFocus(true);

		UISelect select = new UISelect(this, 100, UISelect.Option.fromList(Arrays.asList("Option 1", "Option 2",
				"Very ultra longer option 3", "Shorty", "Moar options", "Even more", "Even Steven", "And a potato too")));
		select.setPosition(0, 70);
		select.setMaxExpandedWidth(120);
		//select.maxDisplayedOptions(5);
		select.select(2);

		btn1 = new UIButton(this, "Horizontal").setSize(90).setPosition(0, 85, Anchor.CENTER);
		btn2 = new UIButton(this, "<").setPosition(-49, 85, Anchor.CENTER).setSize(10, 10);
		btnOver = new UIButton(this, ">").setPosition(50, 85, Anchor.CENTER).setSize(10, 10);

		UIContainer tabCont1 = new UIContainer(this);
		tabCont1.add(cb);
		tabCont1.add(rb1);
		tabCont1.add(rb2);
		tabCont1.add(slider);

		tabCont1.add(tf);
		tabCont1.add(select);

		tabCont1.add(btn1);
		tabCont1.add(btn2);
		tabCont1.add(btnOver);

		int i = 0;
		for (Item item : new Item[] { Items.cooked_porkchop, Items.cooked_beef, Items.cooked_chicken, Items.baked_potato })
		{
			UIImage img = new UIImage(this, new ItemStack(item));
			UIButton btnImage = new UIButton(this, img).setSize(0, 0).setPosition(0, i++ * 24, Anchor.RIGHT);
			tabCont1.add(btnImage);
		}

		/**
		 * CONTAINER 2
		 */
		UILabel label1 = new UILabel(this, "This is LABEL!" + EnumChatFormatting.DARK_RED + " Colored!").setPosition(20, 30);
		UILabel label2 = new UILabel(this, "Smaller label!").setPosition(20, 40);
		label2.setFontScale(0.8F);

		UIContainer tabCont2 = new UIContainer(this);
		UITextField mltf = new UITextField(this, true);
		mltf.setSize(125, 50);
		mltf.setPosition(0, 55);
		mltf.setFontScale(2 / 3F);
		mltf.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras quis semper mi. Pellentesque dapibus diam egestas orci vulputate, a tempor ex hendrerit. Nullam tristique lacinia quam, a dapibus leo gravida eu. Donec placerat, turpis ut egestas dignissim, sem nibh tincidunt neque, eu facilisis massa felis eu nisl. Aenean pellentesque sed nunc et ultrices. Aenean facilisis convallis mauris in mollis. In porta hendrerit tellus id vehicula. Sed non interdum eros, vel condimentum diam. Sed vestibulum tincidunt velit, ac laoreet metus blandit quis. Aliquam sit amet ullamcorper velit. In tristique viverra imperdiet. Mauris facilisis ac leo non molestie.\r\n"
				+ "\r\n"
				+ "Phasellus orci metus, bibendum in molestie eu, interdum lacinia nulla. Nulla facilisi. Duis sagittis suscipit est vitae eleifend. Morbi bibendum tortor nec tincidunt pharetra. Vivamus tortor tortor, egestas sed condimentum ac, tristique non risus. Curabitur magna metus, porta sit amet dictum in, vulputate a dolor. Phasellus viverra euismod tortor, porta ultrices metus imperdiet a. Nulla pellentesque ipsum quis eleifend blandit. Aenean neque nulla, rhoncus et vestibulum eu, feugiat quis erat. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Suspendisse lacus justo, porttitor aliquam tellus eu, commodo tristique leo. Suspendisse scelerisque blandit nisl at malesuada. Proin ut tincidunt augue. Phasellus vel nisl sapien.\r\n"
				+ "\r"
				+ "Sed ut lacinia tellus. Nam arcu ligula, accumsan id lorem id, dapibus bibendum tortor. Cras eleifend varius est, eget eleifend est commodo at. Vivamus sapien purus, faucibus ac urna id, scelerisque sagittis elit. Curabitur commodo elit nec diam vulputate finibus vitae porttitor magna. Nullam nec feugiat dolor. Pellentesque malesuada dolor arcu, ut sagittis mi mattis eu. Vivamus et tortor non nulla venenatis hendrerit nec faucibus quam. Aliquam laoreet leo in risus tempus placerat. In lobortis nulla id enim semper posuere a et libero. Nullam sit amet sapien commodo, egestas nisi eu, viverra nulla. Cras ac vulputate tellus, nec auctor elit.\r\n"
				+ "\r"
				+ "In commodo finibus urna, eu consectetur quam commodo dapibus. Pellentesque metus ligula, ullamcorper non lorem a, dapibus elementum quam. Praesent iaculis pellentesque dui eget pellentesque. Nunc vel varius dui. Aliquam sit amet ex feugiat, aliquet ipsum nec, sollicitudin dolor. Ut ac rhoncus enim. Quisque maximus diam nec neque placerat, euismod blandit purus congue. Integer finibus tellus ligula, eget pretium magna luctus vel. Pellentesque gravida pretium nisl sit amet fermentum. Quisque odio nunc, tristique vitae pretium ut, imperdiet a nunc. Sed eu purus ultricies, tincidunt sapien et, condimentum nunc. Duis luctus augue ac congue luctus. Integer ut commodo turpis, vitae hendrerit quam. Vivamus vulputate efficitur est nec dignissim. Praesent convallis posuere lacus ut suscipit. Aliquam at odio viverra, cursus nulla eget, maximus purus.\r\n"
				+ "\r\n"
				+ "Donec convallis tortor in pretium hendrerit. Maecenas mollis ullamcorper sapien, rhoncus pretium nibh condimentum ut. Phasellus tincidunt aliquet ligula in blandit. Nunc ornare vel ligula eu vulputate. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Suspendisse vitae ultricies nunc. Morbi lorem purus, tempor eget magna at, placerat posuere massa. Donec hendrerit risus a pharetra bibendum. ");
		//	mltf.setText("Some text");

		UIMultiLineLabel ipsum = new UIMultiLineLabel(this);
		ipsum.setPosition(0, 0, Anchor.RIGHT);
		ipsum.setSize(150, 0);
		ipsum.setText("Simple Test");

		tabCont2.add(new UIImage(this, MalisisGui.ITEM_TEXTURE, Items.diamond_axe.getIconFromDamage(0)).setPosition(0, 25));
		tabCont2.add(label1);
		tabCont2.add(label2);
		tabCont2.add(mltf);

		tabCont2.add(ipsum);

		//		new UISlimScrollbar(this, ipsum, UIScrollBar.Type.VERTICAL);

		/**
		 * PANEL
		 */
		panel = new UIPanel(this, UIComponent.INHERITED, 140);
		panel.add(tabCont1);
		panel.add(tabCont2);

		/**
		 * TAB GROUP
		 */
		UITabGroup tabGroup = new UITabGroup(this, TabPosition.TOP);
		//		UIImage img = new UIImage(this, new ItemStack(Items.nether_star));
		//		UIImage img2 = new UIImage(this, new ItemStack(Blocks.gold_ore));

		tab1 = new UITab(this, "Tab 1");
		UITab tab2 = new UITab(this, "Tab 2");

		tab1.setColor(0xFFDDEE);
		tab2.setColor(0xCCCCFF);;

		tabGroup.addTab(tab1, tabCont1);
		tabGroup.addTab(tab2, tabCont2);

		tabGroup.setActiveTab(tab1);
		tabGroup.attachTo(panel, true);

		/**
		 * WINDOW
		*/
		
		UIWindow window = new UIWindow(this, 300, 240).setPosition(0, -20, Anchor.CENTER | Anchor.MIDDLE).setZIndex(0);
		window.add(tabGroup);
		window.add(panel);
		

		new UIMoveHandle(this, window);
		new UIResizeHandle(this, window);
		new UICloseHandle(this, window);

		/**
		 * UIBackgroundContainer
		 */
		UIBackgroundContainer bgc = new UIBackgroundContainer(this, "Container Background", 0, 60);

		bgc.setTopLeftColor(0x993333);
		bgc.setTopRightColor(0x3333FF);
		bgc.setBottomRightColor(0x993333);
		bgc.setBottomLeftColor(0x3333FF);
		bgc.setBottomAlpha(0);

		addToScreen(bgc);
		addToScreen(window);		
	}
}