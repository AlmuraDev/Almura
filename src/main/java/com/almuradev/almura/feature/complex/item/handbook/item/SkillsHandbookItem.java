/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex.item.handbook.item;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.complex.item.ComplexItem;
import com.almuradev.almura.feature.complex.item.handbook.network.ClientboundSkillsHandbookPacket;
import com.almuradev.almura.feature.skills.ServerSkillsManager;
import com.almuradev.almura.feature.skills.SkillsHandler;
import com.almuradev.almura.feature.skills.SkillsRuleReader;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.content.type.itemgroup.ItemGroup;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

import javax.inject.Inject;

/**
 * Lists what each skill gates by level and rewards with experience, read from the server's Skills plugin.
 */
public final class SkillsHandbookItem extends ComplexItem {

    public static final String PERMISSION = "almura.item.skills_handbook";

    @Inject
    @ChannelId(NetworkConfig.CHANNEL)
    private static ChannelBinding.IndexedMessageChannel network;

    @Inject
    private static SkillsHandler skillsHandler;

    public SkillsHandbookItem() {
        super(new ResourceLocation(Almura.ID, "normal/tool/skills_handbook"), "skills_handbook");
        this.setMaxStackSize(1);
        Sponge.getRegistry().getType(ItemGroup.class, Almura.ID + ":tool").ifPresent((itemGroup) -> this.setCreativeTab((CreativeTabs) itemGroup));
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing,
            final float hitX, final float hitY, final float hitZ) {
        if (!world.isRemote) {
            this.open(player, hand);
        }
        // Handled on both sides, so the client doesn't go on to right click the item as well and open it twice
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (!world.isRemote) {
            this.open(player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private void open(final EntityPlayer player, final EnumHand hand) {
        final Player spongePlayer = (Player) player;
        if (!spongePlayer.hasPermission(PERMISSION)) {
            spongePlayer.sendMessage(Text.of("Access denied, missing permission: ", TextColors.AQUA, PERMISSION, TextColors.WHITE, "."));
            return;
        }
        if (!Sponge.getPluginManager().isLoaded(SkillsHandler.PLUGIN_ID)) {
            spongePlayer.sendMessage(Text.of("The ", TextColors.AQUA, "Skills Handbook", TextColors.WHITE, " needs skills, which this server doesn't have."));
            return;
        }

        final ClientboundSkillsHandbookPacket packet = new ClientboundSkillsHandbookPacket();
        for (final String skill : ServerSkillsManager.SKILLS) {
            packet.levels.put(skill, skillsHandler == null ? -1 : Math.max(-1, skillsHandler.getLevelForSkill(spongePlayer, skill)));
        }
        SkillsRuleReader.read(packet.rules);
        player.swingArm(hand);
        network.sendTo(spongePlayer, packet);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add("Shows what each skill needs and rewards");
        super.addInformation(stack, world, tooltip, advanced);
    }
}
