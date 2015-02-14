/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import com.almuradev.almura.client.renderer.accessories.type.*;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccessoryManager {
    private static Map<String, Set<Pair<Accessory, String>>> sacs = new HashMap<String, Set<Pair<Accessory, String>>>();
    private static final RenderPlayer RENDERER_PLAYER = (RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
    private static final ModelBiped MODEL_PLAYER_MAIN = ((RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class)).modelBipedMain;

    private AccessoryManager() {
    }

    public static void addAccessory(String player, Accessory n, String url) {
        TextureManager tm = Minecraft.getMinecraft().getTextureManager();

        Set<Pair<Accessory, String>> acs = sacs.get(player);
        if (acs == null) {
            acs = new HashSet<>();
        }

        Set<Pair<Accessory, String>> toRemove = new HashSet<Pair<Accessory, String>>();
        for (Pair<Accessory, String> pr : acs) {
            if (pr.getLeft().getType().equals(n.getType())) {
                toRemove.add(pr);
            }
        }

        acs.removeAll(toRemove);
        acs.add(Pair.of(n, url));
        sacs.put(player, acs);
    }

    public static void renderAllAccessories(EntityPlayer player, float f, float par2) {
        Set<Pair<Accessory, String>> acs = sacs.get(player.getDisplayName());
        if (acs == null) {
            return;
        }
        for (Pair<Accessory, String> a : acs) {
            RenderManager.instance.renderEngine.bindTexture(new ResourceLocation("accessories/" + a.getLeft().getType().toString()));
            a.getLeft().render(player, f, par2);
        }
    }

    public static void addVIPAccessoriesFor(EntityPlayer player) {
        Map<String, String> vAcs = Maps.newHashMap();
        String that = vAcs.get("tophat");
        String nhat = vAcs.get("notchhat");
        String brace = vAcs.get("bracelet");
        String wings = vAcs.get("wings");
        String ears = vAcs.get("ears");
        String glasses = vAcs.get("sunglasses");
        String tail = vAcs.get("tail");
        if (that != null) {
            addAccessory(player.getDisplayName(), new TopHat(MODEL_PLAYER_MAIN), that);
        }
        if (nhat != null) {
            addAccessory(player.getDisplayName(), new NotchHat(MODEL_PLAYER_MAIN), nhat);
        }
        if (brace != null) {
            addAccessory(player.getDisplayName(), new Bracelet(MODEL_PLAYER_MAIN), brace);
        }
        if (wings != null) {
            addAccessory(player.getDisplayName(), new Wings(MODEL_PLAYER_MAIN), wings);
        }
        if (ears != null) {
            addAccessory(player.getDisplayName(), new Ears(MODEL_PLAYER_MAIN), ears);
        }
        if (glasses != null) {
            addAccessory(player.getDisplayName(), new Sunglasses(MODEL_PLAYER_MAIN), glasses);
        }
        if (tail != null) {
            addAccessory(player.getDisplayName(), new Tail(MODEL_PLAYER_MAIN), tail);
        }
    }

    public static void addAccessoryType(String player, AccessoryType type, String url) {
        Set<Pair<Accessory, String>> acs = sacs.get(player);
        if (acs == null) {
            acs = new HashSet<Pair<Accessory, String>>();
            sacs.put(player, acs);
        }
        Accessory toCreate;
        switch (type) {
            case BRACELET:
                toCreate = new Bracelet(MODEL_PLAYER_MAIN);
                break;
            case EARS:
                toCreate = new Ears(MODEL_PLAYER_MAIN);
                break;
            case NOTCHHAT:
                toCreate = new NotchHat(MODEL_PLAYER_MAIN);
                break;
            case SUNGLASSES:
                toCreate = new Sunglasses(MODEL_PLAYER_MAIN);
                break;
            case TAIL:
                toCreate = new Tail(MODEL_PLAYER_MAIN);
                break;
            case TOPHAT:
                toCreate = new TopHat(MODEL_PLAYER_MAIN);
                break;
            case WINGS:
                toCreate = new Wings(MODEL_PLAYER_MAIN);
                break;
            default:
                toCreate = null;
                break;
        }
        if (toCreate != null) {
            addAccessory(player, toCreate, url);
        }
    }

    public static void removeAccessoryType(String player, AccessoryType type) {
        Set<Pair<Accessory, String>> acs = sacs.get(player);
        if (acs == null) {
            return;
        }
        Pair<Accessory, String> toRemove = null;
        for (Pair<Accessory, String> accessory : acs) {
            if (accessory.getLeft().getType().equals(type)) {
                toRemove = accessory;
                break;
            }
        }
        acs.remove(toRemove);
    }

    public static boolean isHandled(String username) {
        return sacs.containsKey(username);
    }

    public static boolean hasAccessory(String username, AccessoryType type) {
        if (!sacs.containsKey(username)) {
            return false;
        }

        for (Pair<Accessory, String> accessory : sacs.get(username)) {
            if (accessory.getLeft().getType().equals(type)) {
                return true;
            }
        }

        return false;
    }
}
