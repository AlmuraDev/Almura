/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.model.ModelBiped;

public abstract class Accessory {
	private ModelBiped model;

	public Accessory(ModelBiped model) {
		this.model = model;
	}

	public ModelBiped getModel() {
		return model;
	}

	public void render(EntityPlayer player, float f) {
	}

	public void render(EntityPlayer player, float f, float par2) {
		render(player, f);
	}

	public abstract AccessoryType getType();
}
