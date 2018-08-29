package com.almuradev.almura.feature.accessory.client.model;

import com.almuradev.almura.feature.accessory.AccessoryType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class WingsModel extends ModelBase {

    private final AccessoryType accessoryType;

    private ModelRenderer leftWingPart0;

    private ModelRenderer leftWingPart1;

    private ModelRenderer leftWingPart2;

    private ModelRenderer leftWingPart3;

    private ModelRenderer leftWingPart4;

    private ModelRenderer leftWingPart5;

    private ModelRenderer leftWingPart6;

    private ModelRenderer leftWingPart7;

    private ModelRenderer leftWingPart8;

    private ModelRenderer rightWingPart0;

    private ModelRenderer rightWingPart1;

    private ModelRenderer rightWingPart2;

    private ModelRenderer rightWingPart3;

    private ModelRenderer rightWingPart4;

    private ModelRenderer rightWingPart5;

    private ModelRenderer rightWingPart6;

    private ModelRenderer rightWingPart7;

    private ModelRenderer rightWingPart8;

    public WingsModel(final AccessoryType accessoryType, final ModelBase rootModel) {
        this.accessoryType = accessoryType;

        this.leftWingPart1 = new ModelRenderer(rootModel, 56, 0);
        this.leftWingPart1.addBox(-1F, 1F, 3F, 1, 10, 1);
        this.setRotation(this.leftWingPart1, 0F, 0.5007752F, 0.0174533F);

        this.leftWingPart2 = new ModelRenderer(rootModel, 50, 0);
        this.leftWingPart2.addBox(-1F, 0F, 4F, 1, 10, 2);
        this.setRotation(this.leftWingPart2, 0F, 0.5182285F, 0.0349066F);

        this.leftWingPart3 = new ModelRenderer(rootModel, 46, 0);
        this.leftWingPart3.addBox(-1F, -1F, 6F, 1, 10, 1);
        this.setRotation(this.leftWingPart3, 0F, 0.5356818F, 0.0523599F);

        this.leftWingPart4 = new ModelRenderer(rootModel, 38, 0);
        this.leftWingPart4.addBox(-1F, -2F, 7F, 1, 10, 3);
        this.setRotation(this.leftWingPart4, 0F, 0.5531351F, 0.0698132F);

        this.leftWingPart5 = new ModelRenderer(rootModel, 34, 0);
        this.leftWingPart5.addBox(-1F, -1F, 10F, 1, 10, 1);
        this.setRotation(this.leftWingPart5, 0F, 0.5531351F, 0.0523599F);

        this.leftWingPart6 = new ModelRenderer(rootModel, 30, 0);
        this.leftWingPart6.addBox(-1F, 0F, 11F, 1, 10, 1);
        this.setRotation(this.leftWingPart6, 0F, 0.5705884F, 0.0349066F);

        this.leftWingPart7 = new ModelRenderer(rootModel, 26, 0);
        this.leftWingPart7.addBox(-1F, 1F, 12F, 1, 10, 1);
        this.setRotation(this.leftWingPart7, 0F, 0.5880417F, 0.0174533F);

        this.leftWingPart8 = new ModelRenderer(rootModel, 22, 0);
        this.leftWingPart8.addBox(-1F, 3F, 13F, 1, 10, 1);
        this.setRotation(this.leftWingPart8, 0F, 0.5880417F, 0F);

        this.leftWingPart0 = new ModelRenderer(rootModel, 60, 0);
        this.leftWingPart0.addBox(-1F, 2F, 2F, 1, 10, 1);
        this.setRotation(this.leftWingPart0, 0F, 0.4833219F, 0F);

        this.rightWingPart0 = new ModelRenderer(rootModel, 60, 21);
        this.rightWingPart0.addBox(0F, 2F, 2F, 1, 10, 1);
        this.setRotation(this.rightWingPart0, 0F, -0.4833166F, 0F);

        this.rightWingPart1 = new ModelRenderer(rootModel, 56, 21);
        this.rightWingPart1.addBox(0F, 1F, 3F, 1, 10, 1);
        this.setRotation(this.rightWingPart1, 0F, -0.5007699F, -0.0174533F);

        this.rightWingPart2 = new ModelRenderer(rootModel, 50, 20);
        this.rightWingPart2.addBox(0F, 0F, 4F, 1, 10, 2);
        this.setRotation(this.rightWingPart2, 0F, -0.5182232F, -0.0349066F);

        this.rightWingPart3 = new ModelRenderer(rootModel, 46, 21);
        this.rightWingPart3.addBox(0F, -1F, 6F, 1, 10, 1);
        this.setRotation(this.rightWingPart3, 0.0174533F, -0.5356765F, -0.0523599F);

        this.rightWingPart4 = new ModelRenderer(rootModel, 38, 19);
        this.rightWingPart4.addBox(0F, -2F, 7F, 1, 10, 3);
        this.setRotation(this.rightWingPart4, 0.0174533F, -0.5531297F, -0.0698132F);

        this.rightWingPart5 = new ModelRenderer(rootModel, 34, 21);
        this.rightWingPart5.addBox(0F, -1F, 10F, 1, 10, 1);
        this.setRotation(this.rightWingPart5, 0.0174533F, -0.570583F, -0.0523599F);

        this.rightWingPart6 = new ModelRenderer(rootModel, 30, 21);
        this.rightWingPart6.addBox(0F, 0F, 11F, 1, 10, 1);
        this.setRotation(this.rightWingPart6, 0.0174533F, -0.5880363F, -0.0349066F);

        this.rightWingPart7 = new ModelRenderer(rootModel, 26, 21);
        this.rightWingPart7.addBox(0F, 1F, 12F, 1, 10, 1);
        this.setRotation(this.rightWingPart7, 0.0174533F, -0.6054896F, -0.0174533F);

        this.rightWingPart8 = new ModelRenderer(rootModel, 22, 21);
        this.rightWingPart8.addBox(0F, 3F, 13F, 1, 10, 1);
        this.setRotation(this.rightWingPart8, 0.0174533F, -0.6229429F, 0F);
    }

    @Override
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw,
        final float headPitch, final float scale) {

        GlStateManager.pushMatrix();

        final ResourceLocation textureLocation;
        if (this.accessoryType.getTextureLayers().length != 0) {
            textureLocation = this.accessoryType.getTextureLayers()[0];
        } else {
            textureLocation = ModelLoader.White.LOCATION;
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);

        this.leftWingPart0.render(scale);
        this.leftWingPart1.render(scale);
        this.leftWingPart2.render(scale);
        this.leftWingPart3.render(scale);
        this.leftWingPart4.render(scale);
        this.leftWingPart5.render(scale);
        this.leftWingPart6.render(scale);
        this.leftWingPart7.render(scale);
        this.leftWingPart8.render(scale);

        this.rightWingPart0.render(scale);
        this.rightWingPart1.render(scale);
        this.rightWingPart2.render(scale);
        this.rightWingPart3.render(scale);
        this.rightWingPart4.render(scale);
        this.rightWingPart5.render(scale);
        this.rightWingPart6.render(scale);
        this.rightWingPart7.render(scale);
        this.rightWingPart8.render(scale);

        GlStateManager.popMatrix();
    }

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
