/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.block;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.pack.IBlockTextureContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.IItemBlockInformation;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.RotationMeta;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.CollisionNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.RenderNode;
import com.almuradev.almura.pack.node.RotationNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.almuradev.almura.tabs.Tabs;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class PackBlock extends Block implements IPackObject, IBlockTextureContainer, IBlockModelContainer, INodeContainer, IItemBlockInformation {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private Map<Integer, List<Integer>> textureCoordinates;
    private final String modelName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final String textureName;
    private final List<String> tooltip;
    private ClippedIcon[] clippedIcons;
    private Optional<PackModelContainer> modelContainer;
    private RenderNode renderNode;
    private RotationNode rotationNode;
    private BreakNode breakNode;
    @SuppressWarnings("unused")
	private CollisionNode collisionNode;

    public PackBlock(Pack pack, String identifier, List<String> tooltip, String textureName, Map<Integer, List<Integer>> textureCoordinates,
            String modelName,
            PackModelContainer modelContainer, float hardness, float resistance, boolean showInCreativeTab, String creativeTabName,
            RotationNode rotationNode, LightNode lightNode, RenderNode renderNode) {
        super(Material.ground);
        this.pack = pack;
        this.identifier = identifier;
        this.textureCoordinates = textureCoordinates;
        this.textureName = textureName;
        this.modelName = modelName;
        this.renderNode = addNode(renderNode);
        this.rotationNode = addNode(rotationNode);
        this.tooltip = tooltip;
        setModelContainer(modelContainer);
        addNode(rotationNode);
        addNode(lightNode);
        addNode(renderNode);
        setUnlocalizedName(pack.getName() + "\\" + identifier);
        setTextureName(Almura.MOD_ID + ":images/" + textureName);
        setHardness(hardness);
        setResistance(resistance);
        setLightLevel(lightNode.getEmission());
        setLightOpacity(lightNode.getOpacity());
        if (showInCreativeTab) {
            setCreativeTab(Tabs.getTabByName(creativeTabName));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        clippedIcons = PackUtil.generateClippedIconsFromCoordinates(blockIcon, textureName, textureCoordinates);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        if (PackUtil.isEmptyClip(clippedIcons)) {
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
    public boolean renderAsNormalBlock() {
        return modelContainer == null && renderNode.getValue();
    }

    @Override
    public boolean isOpaqueCube() {
        return fullBlock;
    }

    @Override
    public boolean isNormalCube() {
        return this.blockMaterial.isOpaque() && this.renderAsNormalBlock() && !this.canProvidePower();
    }

    //TODO Check this come 1.8
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        final ItemStack held = player.getHeldItem();
        ToolsNode found = null;

        if (breakNode.isEnabled()) {
            for (ToolsNode toolsNode : breakNode.getValue()) {
                if (toolsNode instanceof ToolsNode.OffHand) {
                    if (held == null) {
                        found = toolsNode;
                        break;
                    }
                    continue;
                }
                if (held != null && toolsNode.getTool().minecraftObject == held.getItem()) {
                    found = toolsNode;
                    break;
                }
            }

            if (found == null) {
                found = breakNode.getToolByIdentifier("", "none");
                if (found == null) {
                    return;
                }
            }
        } else {
            return;
        }

        player.addExhaustion(found.getExhaustionRange().getValueWithinRange());
        final ArrayList<ItemStack> drops = Lists.newArrayList();
        for (DropProperty src : found.getValue().getValue()) {
            final GameObject source = src.getSource();
            final ItemStack toDrop;
            if (source.isBlock()) {
                toDrop = new ItemStack((Block) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            } else {
                toDrop = new ItemStack((Item) source.minecraftObject, src.getAmountProperty().getValueWithinRange(), src.getData());
            }
            if (src.getBonusProperty().getSource()) {
                final double chance = src.getBonusProperty().getValueWithinRange();
                if (RangeProperty.RANDOM.nextDouble() <= (chance / 100)) {
                    toDrop.stackSize += src.getBonusProperty().getValueWithinRange();
                }
            }
            drops.add(toDrop);
        }
        harvesters.set(player);
        if (!world.isRemote && !world.restoringBlockSnapshots) {
            final int fortune = EnchantmentHelper.getFortuneModifier(player);
            final float
            modchance =
            ForgeEventFactory.fireBlockHarvesting(drops, world, this, x, y, z, metadata, fortune, 1.0f, false, harvesters.get());
            for (ItemStack is : drops) {
                if (RangeProperty.RANDOM.nextFloat() <= modchance && world.getGameRules().getGameRuleBooleanValue("doTileDrops")) {
                    if (captureDrops.get()) {
                        capturedDrops.get().add(is);
                        return;
                    }
                    final float f = 0.7F;
                    final double d0 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    final EntityItem item = new EntityItem(world, (double) x + d0, (double) y + d1, (double) z + d2, is);
                    item.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(item);
                }
            }
        }
        harvesters.set(null);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item) {
        if (rotationNode.isEnabled() && (rotationNode.isDefaultRotate() || rotationNode.isDefaultMirrorRotate())) {
            final ForgeDirection playerDir = EntityUtils.getEntityFacing(entity, false);

            if (rotationNode.isDefaultMirrorRotate()) {
                final ForgeDirection cameraDir = EntityUtils.getEntityFacing(entity, true);
                world.setBlockMetadataWithNotify(x, y, z, RotationMeta.Rotation.getState(cameraDir, playerDir).getId(), 3);
            } else {
                world.setBlockMetadataWithNotify(x, y, z, RotationMeta.Rotation.getState(ForgeDirection.NORTH, playerDir).getId(), 3);
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        if (!modelContainer.isPresent()) {
            return vanillaBB;
        }
        return modelContainer.get().getPhysics().getCollision(vanillaBB, world, x, y, z);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        if (!modelContainer.isPresent()) {
            return vanillaBB;
        }
        return modelContainer.get().getPhysics().getWireframe(vanillaBB, world, x, y, z);
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        return clippedIcons;
    }

    @Override
    public Map<Integer, List<Integer>> getTextureCoordinates() {
        return textureCoordinates;
    }

    @Override
    public void setTextureCoordinates(Map<Integer, List<Integer>> coordinates) {
        this.textureCoordinates = coordinates;
    }

    @Override
    public ClippedIcon[] getClipIcons() {
        return clippedIcons;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata) {
        return modelContainer;
    }

    @Override
    public Optional<PackModelContainer> getModelContainer() {
        return modelContainer;
    }

    @Override
    public void setModelContainer(PackModelContainer modelContainer) {
        this.modelContainer = Optional.fromNullable(modelContainer);

        if (Configuration.IS_CLIENT && this.modelContainer.isPresent()) {
            if (this.modelContainer.get().getModel().isPresent()) {
                fullBlock = false;
            } else {
                fullBlock = renderNode.isOpaque();
            }
        }
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getTextureName() {
        return textureName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
        if (node.getClass() == BreakNode.class) {
            breakNode = (BreakNode) node;
        } else if (node.getClass() == CollisionNode.class) {
            collisionNode = (CollisionNode) node;
        }
        MinecraftForge.EVENT_BUS.post(new AddNodeEvent(this, node));
        return node;
    }

    @Override
    public void addNodes(INode<?>... nodes) {
        for (INode<?> node : nodes) {
            addNode(node);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T getNode(Class<T> clazz) {
        return (T) nodes.get(clazz);
    }

    @Override
    public <T extends INode<?>> boolean hasNode(Class<T> clazz) {
        return getNode(clazz) != null;
    }

    @Override
    public String toString() {
        return "PackBlock {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + "}";
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);        
        startVec = startVec.addVector((double) (-x), (double) (-y), (double) (-z));
        endVec = endVec.addVector((double) (-x), (double) (-y), (double) (-z));
        Vec3 vec32, vec33, vec34, vec35, vec36, vec37 = null;
        if (modelContainer.isPresent()) {
            modelContainer.get().getPhysics().findRayframe(vanillaBB, world, x, y, z);
            vec32 = startVec.getIntermediateWithXValue(endVec, modelContainer.get().getPhysics().getRayMinX());
            vec33 = startVec.getIntermediateWithXValue(endVec, modelContainer.get().getPhysics().getRayMaxX());
            vec34 = startVec.getIntermediateWithYValue(endVec, modelContainer.get().getPhysics().getRayMinY());
            vec35 = startVec.getIntermediateWithYValue(endVec, modelContainer.get().getPhysics().getRayMaxY());
            vec36 = startVec.getIntermediateWithZValue(endVec, modelContainer.get().getPhysics().getRayMinZ());
            vec37 = startVec.getIntermediateWithZValue(endVec, modelContainer.get().getPhysics().getRayMaxZ());
        } else {
            vec32 = startVec.getIntermediateWithXValue(endVec, minX);
            vec33 = startVec.getIntermediateWithXValue(endVec, maxX);
            vec34 = startVec.getIntermediateWithYValue(endVec, minY);
            vec35 = startVec.getIntermediateWithYValue(endVec, maxY);
            vec36 = startVec.getIntermediateWithZValue(endVec, minZ);
            vec37 = startVec.getIntermediateWithZValue(endVec, maxZ);
        }

        if (!this.isVecInsideYZBounds(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInsideYZBounds(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInsideXZBounds(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInsideXZBounds(vec35)) {
            vec35 = null;
        }

        if (!this.isVecInsideXYBounds(vec36)) {
            vec36 = null;
        }

        if (!this.isVecInsideXYBounds(vec37)) {
            vec37 = null;
        }

        Vec3 vec38 = null;

        if (vec32 != null && (vec38 == null || startVec.squareDistanceTo(vec32) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec32;
        }

        if (vec33 != null && (vec38 == null || startVec.squareDistanceTo(vec33) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec33;
        }

        if (vec34 != null && (vec38 == null || startVec.squareDistanceTo(vec34) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec34;
        }

        if (vec35 != null && (vec38 == null || startVec.squareDistanceTo(vec35) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec35;
        }

        if (vec36 != null && (vec38 == null || startVec.squareDistanceTo(vec36) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec36;
        }

        if (vec37 != null && (vec38 == null || startVec.squareDistanceTo(vec37) < startVec.squareDistanceTo(vec38))) {
            vec38 = vec37;
        }

        if (vec38 == null) {
            return null;
        } else {
            byte b0 = -1;

            if (vec38 == vec32) {
                b0 = 4;
            }

            if (vec38 == vec33) {
                b0 = 5;
            }

            if (vec38 == vec34) {
                b0 = 0;
            }

            if (vec38 == vec35) {
                b0 = 1;
            }

            if (vec38 == vec36) {
                b0 = 2;
            }

            if (vec38 == vec37) {
                b0 = 3;
            }

            return new MovingObjectPosition(x, y, z, b0, vec38.addVector((double) x, (double) y, (double) z));
        }
    }

    private boolean isVecInsideYZBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point == null ? false : point.yCoord >= modelContainer.get().getPhysics().getRayMinY() && point.yCoord <= modelContainer.get().getPhysics().getRayMaxY() && point.zCoord >= modelContainer.get().getPhysics().getRayMinZ() && point.zCoord <= modelContainer.get().getPhysics().getRayMaxZ();
        } else {
            return point == null ? false : point.yCoord >= minY && point.yCoord <= maxY && point.zCoord >= minZ && point.zCoord <= maxZ;    
        }
    }

    private boolean isVecInsideXZBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point == null ? false : point.xCoord >= modelContainer.get().getPhysics().getRayMinX() && point.xCoord <= modelContainer.get().getPhysics().getRayMaxX() && point.zCoord >= modelContainer.get().getPhysics().getRayMinZ() && point.zCoord <= modelContainer.get().getPhysics().getRayMaxZ();
        } else {
            return point == null ? false : point.xCoord >= minX && point.xCoord <= maxX && point.zCoord >= minZ && point.zCoord <= maxZ;    
        }
    }

    private boolean isVecInsideXYBounds(Vec3 point) {
        if (modelContainer.isPresent()) {
            return point == null ? false : point.xCoord >= modelContainer.get().getPhysics().getRayMinX() && point.xCoord <= modelContainer.get().getPhysics().getRayMaxX() && point.yCoord >= modelContainer.get().getPhysics().getRayMinY() && point.yCoord <= modelContainer.get().getPhysics().getRayMaxY();
        } else {
            return point == null ? false : point.xCoord >= minX && point.xCoord <= maxX && point.yCoord >= minY && point.yCoord <= maxY;            
        }
    }
}
