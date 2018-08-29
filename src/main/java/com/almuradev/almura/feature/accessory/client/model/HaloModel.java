package com.almuradev.almura.feature.accessory.client.model;

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

    public HaloModel(final AccessoryType accessoryType, final ModelBase rootModel) {
        this.accessoryType = accessoryType;
        this.rootModel = rootModel;

        this.right = new ModelRenderer(this, 0, 0);
        this.right.addBox(-3F, 28f, -2F, 1, 1, 4);
        this.right.setTextureSize(64, 64);
        this.right.mirror = true;

        this.front = new ModelRenderer(this, 0, 0);
        this.front.addBox(-2F, 28f, -3F, 4, 1, 1);
        this.front.setTextureSize(64, 64);
        this.front.mirror = true;

        this.left = new ModelRenderer(this, 0, 0);
        this.left.addBox(2F, 28f, -2F, 1, 1, 4);
        this.left.setTextureSize(64, 64);
        this.left.mirror = true;

        this.back = new ModelRenderer(this, 0, 0);
        this.back.addBox(-2F, 28f, 2F, 4, 1, 1);
        this.back.setTextureSize(64, 64);
        this.back.mirror = true;
    }

    @Override
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw,
        final float headPitch, final float scale) {

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
