/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class Sunglasses extends Accessory {
	public ModelRenderer SunglassesFront;
	public ModelRenderer SunglassesFront2;
	public ModelRenderer SunglassesBridge;
	public ModelRenderer RightSunglasses;
	public ModelRenderer LeftSunglasses;
	public ModelRenderer RightSunglassesBridge;
	public ModelRenderer LeftSunglassesBridge;

	public Sunglasses(ModelBiped acs) {
		super(acs);
		SunglassesFront = new ModelRenderer(acs, 0, 0);
		SunglassesFront.addBox(-3F, -4F, -5F, 2, 2, 1);
		SunglassesFront2 = new ModelRenderer(acs, 6, 0);
		SunglassesFront2.addBox(1F, -4F, -5F, 2, 2, 1);
		SunglassesBridge = new ModelRenderer(acs, 0, 4);
		SunglassesBridge.addBox(-1F, -4F, -5F, 2, 1, 1);
		RightSunglasses = new ModelRenderer(acs, 0, 6);
		RightSunglasses.addBox(4.0F, -4.0F, -4.0F, 1, 1, 4);
		LeftSunglasses = new ModelRenderer(acs, 12, 0);
		LeftSunglasses.addBox(-5.0F, -4.0F, -4.0F, 1, 1, 4);
		LeftSunglassesBridge = new ModelRenderer(acs, 6, 5);
		LeftSunglassesBridge.addBox(-5.0F, -4.0F, -5.0F, 2, 1, 1);
		RightSunglassesBridge = new ModelRenderer(acs, 6, 7);
		RightSunglassesBridge.addBox(3.0F, -4.0F, -5.0F, 2, 1, 1);
	}

	@Override
	public void render(EntityPlayer plr, float f) {
		SunglassesFront.rotateAngleY = getModel().bipedHead.rotateAngleY;
		SunglassesFront.rotateAngleX = getModel().bipedHead.rotateAngleX;
		SunglassesFront.render(f);
		SunglassesFront2.rotateAngleY = getModel().bipedHead.rotateAngleY;
		SunglassesFront2.rotateAngleX = getModel().bipedHead.rotateAngleX;
		SunglassesFront2.render(f);
		SunglassesBridge.rotateAngleY = getModel().bipedHead.rotateAngleY;
		SunglassesBridge.rotateAngleX = getModel().bipedHead.rotateAngleX;
		SunglassesBridge.render(f);
		RightSunglasses.rotateAngleY = getModel().bipedHead.rotateAngleY;
		RightSunglasses.rotateAngleX = getModel().bipedHead.rotateAngleX;
		RightSunglasses.render(f);
		LeftSunglasses.rotateAngleY = getModel().bipedHead.rotateAngleY;
		LeftSunglasses.rotateAngleX = getModel().bipedHead.rotateAngleX;
		LeftSunglasses.render(f);
		LeftSunglassesBridge.rotateAngleY = getModel().bipedHead.rotateAngleY;
		LeftSunglassesBridge.rotateAngleX = getModel().bipedHead.rotateAngleX;
		LeftSunglassesBridge.render(f);
		RightSunglassesBridge.rotateAngleY = getModel().bipedHead.rotateAngleY;
		RightSunglassesBridge.rotateAngleX = getModel().bipedHead.rotateAngleX;
		RightSunglassesBridge.render(f);
	}

	@Override
	public AccessoryType getType() {
		return AccessoryType.SUNGLASSES;
	}
}
