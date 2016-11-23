/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.command;

import com.almuradev.almura.Almura;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.ITextureContainer;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackCreator;
import com.almuradev.almura.pack.PackKeys;
import com.almuradev.almura.pack.PackUtil;
import com.almuradev.almura.pack.item.PackItem;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.FileSystem;
import com.almuradev.almura.util.Functions;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClientCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "almura";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "almura";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer)) {
            return;
        }
        
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[Almura Commands] - " + Colors.GRAY + "missing required arguments."));
            return;
        }

        EntityPlayer player = (EntityPlayer)sender;
        
        switch (args[0].toUpperCase()) {
            case "RELOAD":
                if (player.worldObj.isRemote) {
                    onCommandReload((EntityPlayer) sender, Arrays.copyOfRange(args, 1, args.length));
                }
                break;
                
            case "LOOKUP":
                if (player.getHeldItem() != null) {                        
                    ItemStack item  = player.getHeldItem();
                    sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[A/C] - Unlocalized Name: " + Colors.GRAY + item.getUnlocalizedName()));
                    sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[A/C] - Display Name: " + Colors.GRAY + item.getDisplayName()));
                } else {
                    sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[A/C] - Item Lookup: " + Colors.GRAY + "Player hand is empty."));
                }
                break;
                
            case "TRY":

                break;
                
            case "REFRESHSKIN":
                if (player.worldObj.isRemote) {
                    Minecraft.getMinecraft().getSkinManager().func_152790_a(Minecraft.getMinecraft().getSession().getProfile(), Minecraft.getMinecraft().thePlayer, true);
                    sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[A/C] - Refresh Skin: " + Colors.GRAY + "Skin refresh initiated."));
                    break;
                }

            default:
                sender.addChatMessage(new ChatComponentText(Colors.WHITE + "[Almura Commands] - " + Colors.GRAY + "specified argument is not valid."));
                break;
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    private void onCommandReload(EntityPlayer player, String[] args) {
        // Handle models
        try {
            for (Path root : Files.newDirectoryStream(FileSystem.CONFIG_MODELS_PATH, FileSystem.FILTER_MODEL_FILES_ONLY)) {
                String name = root.getFileName().toString();
                name = name.split(".shape")[0];

                PackModelContainer found = null;
                for (PackModelContainer container : Pack.getModelContainers()) {
                    if (container.getIdentifier().equals(name)) {
                        found = container;
                        break;
                    }
                }
                if (found != null) {
                    final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(root.toFile()).build().load();
                    PackCreator.loadShapeIntoModelContainer(found, name, reader);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Pack> entry : Pack.getPacks().entrySet()) {
            for (Block block : entry.getValue().getBlocks()) {
                if (block instanceof IPackObject && block instanceof ITextureContainer) {
                    final Pack pack = entry.getValue();
                    final String name = ((IPackObject) block).getIdentifier();
                    final Path root = FileSystem.CONFIG_YML_PATH.resolve(entry.getKey()).resolve(name + ".yml");
                    try {
                        final ConfigurationNode reader = YAMLConfigurationLoader.builder().setFile(root.toFile()).build().load();

                        Map<Integer, List<Integer>> textureCoordinates;
                        try {
                            textureCoordinates = PackUtil.parseCoordinatesFrom(reader.getNode(PackKeys.TEXTURE_COORDINATES.getKey()).getList(Functions
                                    .FUNCTION_STRING_TRANSFORMER));
                        } catch (NumberFormatException nfe) {
                            if (!reader.getNode(PackKeys.TEXTURE_COORDINATES.getKey()).isVirtual()) {
                                Almura.LOGGER.warn("Failed parsing texture coordinates in [" + name + "] in pack [" + pack.getName() + "]. " + nfe.getMessage());
                            }
                            textureCoordinates = Maps.newHashMap();
                        }
                        
                        ((ITextureContainer) block).setTextureCoordinates(textureCoordinates);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Handle textures
        Minecraft.getMinecraft().refreshResources();
    }
}
