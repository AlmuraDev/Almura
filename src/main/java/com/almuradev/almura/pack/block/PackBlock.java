/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Tabs;
import com.almuradev.almura.lang.Languages;
import com.almuradev.almura.pack.ContentPack;
import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IShapeContainer;
import com.almuradev.almura.pack.PackIcon;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.model.PackShape;
import com.flowpowered.cerealization.config.ConfigurationException;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PackBlock extends Block implements IClipContainer, IShapeContainer {

    public static int renderId;
    private final ContentPack pack;
    //TEXTURES
    private final Map<Integer, List<Integer>> textureCoordinatesByFace;
    //SHAPES
    private final String shapeName;
    private ClippedIcon[] clippedIcons;
    private String textureName;
    private PackShape shape;
    //COLLISION
    private boolean useVanillaCollision;
    private List<Double> collisionBounds = new LinkedList<>();
    //WIREFRAME
    private boolean useVanillaWireframe;
    private List<Double> wireframeBounds = new LinkedList<>();

    public PackBlock(ContentPack pack, String identifier) {
        this(pack, identifier, identifier, 1f, 0f, 0f, 0, true, "legacy", null, null, true, null, true, null);
    }

    public PackBlock(ContentPack pack, String identifier, String textureName) {
        this(pack, identifier, textureName, 1f, 0f, 0f, 0, true, "legacy", null, null, true, null, true, null);
    }

    public PackBlock(ContentPack pack, String identifier, String textureName, float hardness) {
        this(pack, identifier, textureName, hardness, 0f, 0f, 0, true, "legacy", null, null, true, null, true, null);
    }

    public PackBlock(ContentPack pack, String identifier, String textureName, float hardness, float lightLevel) {
        this(pack, identifier, textureName, hardness, 0f, lightLevel, 0, true, "legacy", null, null, true, null, true, null);
    }

    public PackBlock(ContentPack pack, String identifier, String textureName, float hardness, float resistance, float lightLevel, int lightOpacity,
                     boolean showInCreativeTab, String creativeTabName, Map<Integer, List<Integer>> textureCoordinates, String shapeName,
                     boolean useVanillaCollision, List<Double> collisionBounds, boolean useVanillaWireframe, List<Double> wireframeBounds) {
        super(Material.rock);
        this.pack = pack;
        this.textureCoordinatesByFace = textureCoordinates;
        this.textureName = textureName;
        this.shapeName = shapeName;
        this.useVanillaCollision = useVanillaCollision;
        this.collisionBounds = collisionBounds == null ? new LinkedList<Double>() : collisionBounds;
        this.useVanillaWireframe = useVanillaWireframe;
        this.wireframeBounds = wireframeBounds == null ? new LinkedList<Double>() : wireframeBounds;
        setBlockName(pack.getName() + "_" + identifier);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightLevel);
        setLightOpacity(lightOpacity);
        setBlockTextureName(Almura.MOD_ID.toLowerCase() + ":packs/" + pack.getName() + "-" + textureName);
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
        GameRegistry.registerBlock(this, ItemBlock.class, pack.getName() + "_" + identifier);
    }

    public static PackBlock createFromReader(ContentPack pack, String name, YamlConfiguration reader) throws ConfigurationException {
        final String title = reader.getChild("Title").getString(name);
        String textureName = reader.getChild("Texture").getString(name);
        textureName = textureName.split(".png")[0];

        final float hardness = reader.getChild("Hardness").getFloat(1f);
        final float lightLevel = reader.getChild("LightLevel").getFloat(0f);
        final int lightOpacity = reader.getChild("light-opacity").getInt(0);
        final boolean showInCreativeTab = reader.getChild("show-in-creative-tab").getBoolean(true);
        final String creativeTabName = reader.getChild("creative-tab-name").getString("other");
        final float resistance = reader.getChild("resistance").getFloat(0);
        final boolean useVanillaCollision = !reader.hasChild("collision-bounds");
        final List<Double> collisionCoords = new LinkedList<>();
        if (!useVanillaCollision) {
            final String rawCoords = reader.getChild("collision-bounds").getString();
            final String[] splitRawCoords = rawCoords.split(" ");
            for (String coord : splitRawCoords) {
                collisionCoords.add(Double.parseDouble(coord));
            }
        }

        final boolean useVanillaWireframe = !reader.hasChild("wireframe-bounds");
        final List<Double> wireframeCoords = new LinkedList<>();
        if (!useVanillaWireframe) {
            final String rawCoords = reader.getChild("wireframe-bounds").getString();
            final String[] splitRawCoords = rawCoords.split(" ");
            for (String coord : splitRawCoords) {
                wireframeCoords.add(Double.parseDouble(coord));
            }
        }

        String shapeName = reader.getChild("Shape").getString();
        if (shapeName != null) {
            shapeName = shapeName.split(".shape")[0];
        }

        final Map<Integer, List<Integer>> textureCoordinatesByFace = PackUtil.extractCoordsFrom(reader);

        Almura.LANGUAGES.put(Languages.ENGLISH_AMERICAN, "tile." + pack.getName() + "_" + name + ".name", title);

        return new PackBlock(pack, name, textureName, hardness, resistance, lightLevel, lightOpacity, showInCreativeTab, creativeTabName,
                             textureCoordinatesByFace, shapeName, useVanillaCollision, collisionCoords, useVanillaWireframe, wireframeCoords);
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

        if (textureCoordinatesByFace.isEmpty()) {
            return;
        }

        clippedIcons = PackUtil.generateClippedIconsFromCoords(pack, blockIcon, textureName, textureCoordinatesByFace);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
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

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB box;
        if (useVanillaCollision) {
            box = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        } else {
            box =
                    AxisAlignedBB.getBoundingBox(collisionBounds.get(0), collisionBounds.get(1), collisionBounds.get(2), collisionBounds.get(3),
                                                 collisionBounds.get(4), collisionBounds.get(5));
        }
        return box;
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        AxisAlignedBB box;
        if (useVanillaWireframe) {
            box = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        } else {
            box =
                    AxisAlignedBB.getBoundingBox(x + wireframeBounds.get(0), y + wireframeBounds.get(1), z + wireframeBounds.get(2),
                                                 x + wireframeBounds.get(3), y + wireframeBounds.get(4), z + wireframeBounds.get(5));
        }
        return box;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    public ContentPack getPack() {
        return pack;
    }

    public void reloadShape() {
        this.shape = null;

        if (shapeName != null) {
            for (PackShape shape : pack.getShapes()) {
                if (shape.getName().equals(shapeName)) {
                    this.shape = shape;
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", raw_name= " + getUnlocalizedName() + "}";
    }
}
