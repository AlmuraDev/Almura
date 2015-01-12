/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.crop;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IBlockClipContainer;
import com.almuradev.almura.pack.IBlockModelContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.BreakNode;
import com.almuradev.almura.pack.node.GrowthNode;
import com.almuradev.almura.pack.node.INode;
import com.almuradev.almura.pack.node.LightNode;
import com.almuradev.almura.pack.node.ToolsNode;
import com.almuradev.almura.pack.node.event.AddNodeEvent;
import com.almuradev.almura.pack.node.property.DropProperty;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.almuradev.almura.pack.renderer.PackIcon;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class PackCrops extends BlockCrops implements IPackObject, IBlockClipContainer, IBlockModelContainer, INodeContainer {

    public static int renderId;
    private final Pack pack;
    private final String identifier;
    private final String textureName;
    private final ConcurrentMap<Class<? extends INode<?>>, INode<?>> nodes = Maps.newConcurrentMap();
    private final Map<Integer, Stage> stages;

    public PackCrops(Pack pack, String identifier, String textureName, int levelRequired, Map<Integer, Stage> stages) {
        this.pack = pack;
        this.identifier = identifier;
        this.stages = stages;
        this.textureName = textureName;
        setBlockName(pack.getName() + "\\" + identifier);
        setBlockTextureName(Almura.MOD_ID + ":images/" + textureName);
        setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        final int metadata = world.getBlockMetadata(x, y, z);

        if (metadata >= stages.size() - 1) {
            return;
        }

        final Stage stage = stages.get(metadata);

        if (stage == null) {
            return;
        }

        final LightNode lightNode = stage.getNode(LightNode.class);
        final int minLightLevel = lightNode.getValue().getMin();
        final int maxLightLevel = lightNode.getValue().getMax();
        final int areaBlockLight = world.getSavedLightValue(EnumSkyBlock.Block, x, y, z);
        final int worldLight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) - world.skylightSubtracted;
        boolean enoughLight = false;
        if (areaBlockLight >= minLightLevel && areaBlockLight <= maxLightLevel) {
            enoughLight = true;
        } else if (worldLight >= minLightLevel && worldLight <= maxLightLevel) {
            enoughLight = true;
        }

        if (enoughLight) {
            stage.onTick(world, x, y, z, random);
            final double
                    chance =
                    stage.getNode(GrowthNode.class).getValue().getValueWithinRange();
            if (random.nextDouble() <= (chance / 100)) {
                stage.onGrowth(world, x, y, z, random);
                final Stage newStage = stages.get(metadata + 1);
                newStage.onGrown(world, x, y, z, random);
                world.setBlockMetadataWithNotify(x, y, z, metadata + 1, 3);
            }
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
        if (access != null) {
            final int metadata = access.getBlockMetadata(x, y, z);
            final Stage stage = stages.get(metadata);
            if (stage != null) {
                final Optional<PackModelContainer> modelContainer = stage.getModelContainer();
                if (modelContainer.isPresent()) {
                    final AxisAlignedBB
                            blockBoundsBB =
                            modelContainer.get().getPhysics().getBlockBounds(AxisAlignedBB.getBoundingBox(0.5, 0, 0, 1, 0.25, 1),
                                                                             access, x, y, z);
                    setBlockBounds((float) blockBoundsBB.minX, (float) blockBoundsBB.minY, (float) blockBoundsBB.minZ, (float) blockBoundsBB.maxX,
                                   (float) blockBoundsBB.maxY, (float) blockBoundsBB.maxZ);
                }
            }
        }
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        final Stage stage = stages.get(world.getBlockMetadata(x, y, z));
        return stage != null ? stage.getLightOpacity(world, x, y, z) : super.getLightOpacity(world, x, y, z);
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        final Stage stage = stages.get(metadata);
        return stage != null ? stage.getExpDrop(world, metadata, fortune) : super.getExpDrop(world, metadata, fortune);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = new PackIcon(this, textureName).register((TextureMap) register);
        for (Stage stage : stages.values()) {
            stage.registerBlockIcons(blockIcon, textureName, register);
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        final Stage stage = stages.get(world.getBlockMetadata(x, y, z));
        return stage != null ? stage.getLightValue(world, x, y, z) : super.getLightValue(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int type) {
        final Stage stage = stages.get(type);

        return stage != null ? stage.getIcon(blockIcon, side, type) : blockIcon;
    }

    //TODO Check this come 1.8
    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        final Stage stage = stages.get(metadata);
        if (stage == null) {
            return;
        }
        player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
        final ItemStack held = player.getHeldItem();
        ToolsNode found = null;
        final BreakNode breakNode = stage.getNode(BreakNode.class);
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getCollisionBoundingBoxFromPool(world, x, y, z);
        final int metadata = world.getBlockMetadata(x, y, z);
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            final Optional<PackModelContainer> modelContainer = stage.getModelContainer(world, x, y, z, metadata);
            if (modelContainer.isPresent()) {
                return modelContainer.get().getPhysics().getCollisionBoundingBoxFromPool(vanillaBB, world, x, y, z);
            }
        }

        return vanillaBB;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final AxisAlignedBB vanillaBB = super.getSelectedBoundingBoxFromPool(world, x, y, z);
        final int metadata = world.getBlockMetadata(x, y, z);
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            final Optional<PackModelContainer> modelContainer = stage.getModelContainer(world, x, y, z, metadata);
            if (modelContainer.isPresent()) {
                return modelContainer.get().getPhysics().getSelectedBoundingBox(vanillaBB, world, x, y, z);
            }
        }

        return vanillaBB;
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
    public ClippedIcon[] getClipIcons() {
        final Stage stage = stages.get(0);
        return stage == null ? null : stage.getClipIcons();
    }

    @Override
    public ClippedIcon[] getClipIcons(IBlockAccess access, int x, int y, int z, int metadata) {
        final Stage stage = stages.get(metadata);
        if (stage != null) {
            return stage.getClipIcons(access, x, y, z, metadata);
        }
        return new ClippedIcon[0];
    }

    @Override
    public Optional<PackModelContainer> getModelContainer(IBlockAccess access, int x, int y, int z, int metadata) {
        final Stage stage = stages.get(metadata);
        return stage == null ? Optional.<PackModelContainer>absent() : stage.getModelContainer(access, x, y, z, metadata);
    }

    @Override
    public Optional<PackModelContainer> getModelContainer() {
        final Stage stage = stages.get(0);
        return stage == null ? Optional.<PackModelContainer>absent() : stage.getModelContainer();
    }

    @Override
    public void setModelContainer(PackModelContainer modelContainer) {
    }

    @Override
    public String getModelName() {
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends INode<?>> T addNode(T node) {
        nodes.put((Class<? extends INode<?>>) node.getClass(), node);
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

    public Map<Integer, Stage> getStages() {
        return Collections.unmodifiableMap(stages);
    }

    @Override
    public String toString() {
        return "PackCrops {pack= " + pack.getName() + ", registry_name= " + pack.getName() + "\\" + identifier + ", stages= " + stages + "}";
    }
}
