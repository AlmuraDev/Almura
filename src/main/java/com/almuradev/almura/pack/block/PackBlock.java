/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.LanguageRegistry;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IRotatable;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.model.PackShape;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PackBlock extends Block implements IClipContainer, IShapeContainer, IRotatable {

    public static int renderId;
    private final ContentPack pack;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    private final int dropAmount;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    private boolean rotation;
    private boolean mirrorRotation;

    public PackBlock(ContentPack pack, String identifier, String textureName, float hardness, int dropAmount, float resistance, boolean rotation,
                     boolean mirrorRotation, float lightLevel, int lightOpacity, boolean showInCreativeTab, String creativeTabName,
                     Map<Integer, List<Integer>> textureCoordinates, String shapeName) {
        super(Material.rock);
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.dropAmount = dropAmount;
        this.rotation = rotation;
        this.mirrorRotation = mirrorRotation;
        setBlockName(pack.getName() + "_" + identifier);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightLevel);

        setLightOpacity(lightOpacity);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":images/" + textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    public static PackBlock createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name).split("\n")[0];
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);
        float lightLevel = reader.getChild("LightLevel").getFloat(0f);
        if (lightLevel > 1f) {
            lightLevel = lightLevel / 15;
        }
        final int lightOpacity = reader.getChild("Light-Opacity").getInt(0);
        final int dropAmount = reader.getChild("ItemDropAmount").getInt(0);
        final boolean showInCreativeTab = reader.getChild("Show-In-Creative-Tab").getBoolean(true);
        final String creativeTabName = reader.getChild("Creative-Tab-Name").getString("other");
        final float resistance = reader.getChild("Resistance").getFloat(0);
        final boolean rotation = reader.getChild("Rotation").getBoolean(true);
        final boolean mirrorRotation = reader.getChild("MirrorRotate").getBoolean(false);

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader);

        LanguageRegistry.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "_" + name + ".name", title);

        return new PackBlock(pack, name, textureName, hardness, dropAmount, resistance, rotation, mirrorRotation, lightLevel, lightOpacity,
                             showInCreativeTab, creativeTabName, textureCoordinatesByFace, shapeName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(pack.getName(), textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, blockIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmpty(clippedIcons)) {
            return super.getIcon(side, type);
        }
        ClippedIcon sideIcon;

        if (side >= clippedIcons.length) {
            sideIcon = clippedIcons[0];
        } else {
            sideIcon = clippedIcons[side];

            if (sideIcon == null) {
                sideIcon = clippedIcons[0];
            }
        }

        return sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock() {
        return shape == null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isOpaqueCube() {
        return shape == null;
    }

    @Override
    public int quantityDropped(Random p_149745_1_) {
        return dropAmount;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
        final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
        final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);
        world.setBlockMetadataWithNotify(x, y, z, RotationMeta.getRotationMeta(cameraDir, playerDir).ordinal(), 3);
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (shape == null) {
            return vanillaBB;
        }
        return shape.getCollisionBoundingBoxFromPool(vanillaBB, world, x, y, z);
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        if (shape == null) {
            return vanillaBB;
        }
        return shape.getSelectedBoundingBoxFromPool(vanillaBB, world, x, y, z);
    }

    @Override
    public ContentPack getPack() {
        return pack;
    }

    @Override
    public ClippedIcon[] getClipIcons(World world, int x, int y, int z, int metadata) {
        return clippedIcons;
    }

    @Override
    public PackShape getShape(World world, int x, int y, int z, int metadata) {
        return shape;
    }

    @Override
    public void setShapeFromPack() {
        this.shape = null;

        if (shapeName != null) {
            for (PackShape shape : pack.getShapes()) {
                if (shape.getName().equals(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
        if (shape != null) {
            if (!shape.useVanillaCollision) {
                setBlockBounds(shape.collisionCoordinates.get(0).floatValue(), shape.collisionCoordinates.get(1).floatValue(),
                               shape.collisionCoordinates.get(2).floatValue(), shape.collisionCoordinates.get(3).floatValue(),
                               shape.collisionCoordinates.get(4).floatValue(), shape.collisionCoordinates.get(5).floatValue());
            }
        }
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }


    @Override
    public boolean canMirrorRotate(World world, int x, int y, int z, int metadata) {
        return mirrorRotation;
    }

    @Override
    public boolean canRotate(World world, int x, int y, int z, int metadata) {
        return rotation;
    }
}
