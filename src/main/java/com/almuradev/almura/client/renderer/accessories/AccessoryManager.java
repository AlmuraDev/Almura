/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Filesystem;
import com.almuradev.almura.client.renderer.accessories.type.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AccessoryManager {
    public static final RenderPlayer PLAYER_RENDERER = (RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
    private static Map<String, Class<? extends IAccessory<?>>> ACCESSORY_TYPES_BY_NAME = Maps.newHashMap();
    private static Map<String, Set<TexturedAccessory>> ACCESSORIES_BY_PLAYERS = Maps.newHashMap();

    static {
        register("bracelet", Bracelet.class);
        register("ears", Ears.class);
        register("notchhat", NotchHat.class);
        register("sunglasses", Sunglasses.class);
        register("tail", Tail.class);
        register("tophat", TopHat.class);
        register("wings", Wings.class);
        register("cloak", Cloak.class);
        register("skin", Skin.class);
        register("cathat", CatHat.class);

        try {
            for (Path path : Files.newDirectoryStream(Paths.get(Filesystem.CONFIG_IMAGES_PATH.toString(), "accessories"), Filesystem.IMAGE_FILES_ONLY_FILTER)) {
                Filesystem.registerTexture(Almura.MOD_ID, "accessory_" + path.getFileName().toString().split(".png")[0].split(".jpg")[0], path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AccessoryManager() {
    }

    public static void register(String identifier, Class<? extends IAccessory<?>> clazz) {
        ACCESSORY_TYPES_BY_NAME.put(identifier, clazz);
    }

    @SuppressWarnings("unchecked")
    public static void addAccessory(EntityPlayer player, ResourceLocation textureLocation, String accessoryIdentifier) {
        final Class<? extends IAccessory<? extends ModelBase>> accessoryClazz = ACCESSORY_TYPES_BY_NAME.get(accessoryIdentifier);
        final IAccessory accessory;

        if (accessoryClazz != null) {
            try {
                accessory = accessoryClazz.newInstance();
            } catch (Exception e) {
                Almura.LOGGER.info("Could not create instance of accessory [" + accessoryIdentifier + "]. Does it have an empty constructor?");
                return;
            }
            Set<TexturedAccessory> playerAccessories = ACCESSORIES_BY_PLAYERS.get(player.getCommandSenderName());
            if (playerAccessories == null) {
                playerAccessories = Sets.newHashSet();
                ACCESSORIES_BY_PLAYERS.put(player.getCommandSenderName(), playerAccessories);
            }
            playerAccessories.add(new TexturedAccessory(textureLocation, accessory));
            accessory.onAttached(player, textureLocation, PLAYER_RENDERER.modelBipedMain);
        }
    }

    public static void removeAccessory(EntityPlayer player, String accessoryIdentifier) {
        final Set<TexturedAccessory> playerAccessories = ACCESSORIES_BY_PLAYERS.get(player.getCommandSenderName());
        if (playerAccessories != null) {
            final Iterator<TexturedAccessory> iterator = playerAccessories.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().accessoryType.getName().equalsIgnoreCase(accessoryIdentifier)) {
                    iterator.remove();
                }
            }
        }
    }

    public static Set<TexturedAccessory> getAccessories(String player) {
        return ACCESSORIES_BY_PLAYERS.get(player);
    }

    @SuppressWarnings("unchecked")
    public static void onRenderSpecialTick(RenderPlayerEvent.Specials.Post event) {
        final Set<AccessoryManager.TexturedAccessory> playerAccessories = AccessoryManager.getAccessories(event.entityPlayer.getCommandSenderName());
        if (playerAccessories != null) {
            for (AccessoryManager.TexturedAccessory accessory : playerAccessories) {
                accessory.accessoryType.onRender(event.entityPlayer, accessory.textureLocation, AccessoryManager.PLAYER_RENDERER.modelBipedMain, 0.0625F, event.partialRenderTick);
            }
        } else {
            addAccessory(event.entityPlayer, new ResourceLocation(Almura.MOD_ID, "accessory_sunglasses_black"), "sunglasses");
            addAccessory(event.entityPlayer, new ResourceLocation(Almura.MOD_ID, "accessory_cathat_in_the_hat"), "cathat");
        }
    }

    public static final class TexturedAccessory {
        public final ResourceLocation textureLocation;
        public final IAccessory accessoryType;

        public TexturedAccessory(ResourceLocation textureLocation, IAccessory accessoryType) {
            this.textureLocation = textureLocation;
            this.accessoryType = accessoryType;
        }
    }
}
