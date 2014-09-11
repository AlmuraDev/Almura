/**
 * This file is part of AlmuraMod, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almuramod.client.renderer;

import org.lwjgl.opengl.GL11;

import com.almuradev.almuramod.AlmuraMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class BaseOBJRenderer extends TileEntitySpecialRenderer {
    private IModelCustom objectModel;
    private ResourceLocation objectTexture;

    public BaseOBJRenderer(String modelName, String textureName) {
        objectModel = AdvancedModelLoader.loadModel(new ResourceLocation(AlmuraMod.MOD_ID.toLowerCase(), "models/" + modelName + ".obj"));
        objectTexture = new ResourceLocation(AlmuraMod.MOD_ID.toLowerCase(), "textures/models/" + textureName + ".png");
    }

    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f){
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        Minecraft.getMinecraft().renderEngine.bindTexture(objectTexture);
        objectModel.renderAll();
        GL11.glPopMatrix();
    }
}