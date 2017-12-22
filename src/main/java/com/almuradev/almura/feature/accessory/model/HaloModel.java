package com.almuradev.almura.feature.accessory.model;

import com.almuradev.almura.feature.accessory.AccessoryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class HaloModel extends ModelBase {

    private final AccessoryType accessoryType;
    private final ModelBase rootModel;
    private ModelRenderer right;
    private ModelRenderer front;
    private ModelRenderer left;
    private ModelRenderer back;

    public HaloModel(AccessoryType accessoryType, ModelBase rootModel) {
        this.accessoryType = accessoryType;
        this.rootModel = rootModel;

        right = new ModelRenderer(this, 0, 0);
        right.addBox(-3F, 28f, -2F, 1, 1, 4);
        right.setTextureSize(64, 64);
        right.mirror = true;

        front = new ModelRenderer(this, 0, 0);
        front.addBox(-2F, 28f, -3F, 4, 1, 1);
        front.setTextureSize(64, 64);
        front.mirror = true;

        left = new ModelRenderer(this, 0, 0);
        left.addBox(2F, 28f, -2F, 1, 1, 4);
        left.setTextureSize(64, 64);
        left.mirror = true;

        back = new ModelRenderer(this, 0, 0);
        back.addBox(-2F, 28f, 2F, 4, 1, 1);
        back.setTextureSize(64, 64);
        back.mirror = true;
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.rootModel instanceof ModelBiped) {
            this.right.rotateAngleX = -((ModelBiped) this.rootModel).bipedHead.rotateAngleX;

            this.front.rotateAngleX = -((ModelBiped) this.rootModel).bipedHead.rotateAngleX;

            this.left.rotateAngleX = -((ModelBiped) this.rootModel).bipedHead.rotateAngleX;

            this.back.rotateAngleX = -((ModelBiped) this.rootModel).bipedHead.rotateAngleX;

            GlStateManager.pushMatrix();

            final ResourceLocation textureLocation;
            if (this.accessoryType.getTextureLayers().length != 0) {
                textureLocation = this.accessoryType.getTextureLayers()[0];
            } else {
                textureLocation = ModelLoader.White.LOCATION;
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

            GlStateManager.scale(0.75F, 0.75F, 0.75F);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);

            this.right.render(scale);
            this.front.render(scale);
            this.left.render(scale);
            this.back.render(scale);

            GlStateManager.popMatrix();
        }
    }
}
