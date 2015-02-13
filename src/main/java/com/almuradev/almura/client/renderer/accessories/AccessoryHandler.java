/*
 * This file is part of Spoutcraft.
 *
 * Copyright (c) 2011 SpoutcraftDev <http://spoutcraft.org/>
 * Spoutcraft is licensed under the GNU Lesser General Public License.
 *
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.almuradev.almura.client.renderer.accessories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import com.almuradev.almura.client.ChatColor;
import com.almuradev.almura.client.HDImageBufferDownload;
import com.almuradev.almura.client.VIP;
import com.flowpowered.cerealization.config.yaml.YamlConfiguration;

import org.apache.commons.lang3.tuple.Pair;

public class AccessoryHandler {
	private static Map<String, Set<Pair<Accessory, String>>> sacs = new HashMap<String, Set<Pair<Accessory, String>>>();
	private static Set<String> downloaded = new HashSet<String>();
	private static RenderPlayer renderer = (RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class);
	private static ModelBiped modelBipedMain = ((RenderPlayer) RenderManager.instance.entityRenderMap.get(EntityPlayer.class)).modelBipedMain;

	private AccessoryHandler() {
	}

	public static void addAccessory(String player, Accessory n, String url) {
		TextureManager tm= Minecraft.getMinecraft().getTextureManager();
		Object texture = new ThreadDownloadImageData(null, url, (ResourceLocation)null, new HDImageBufferDownload());       
        tm.loadTexture(new ResourceLocation("accessories/" + n.getType().toString()), (ITextureObject)texture);
		
		Set<Pair<Accessory, String>> acs = sacs.get(player);
		if (acs == null) {
			acs = new HashSet<Pair<Accessory, String>>();
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
		String cleanUserName = ChatColor.stripColor(player.getDisplayName());
		VIP vip = getVIP(cleanUserName);
		
		if (vip == null) {
			return;
		}
		Map<String, String> vAcs = vip.Accessories();
		if (vAcs == null) {
			return;
		}
		String that = vAcs.get("tophat");
		String nhat = vAcs.get("notchhat");
		String brace = vAcs.get("bracelet");
		String wings = vAcs.get("wings");
		String ears = vAcs.get("ears");
		String glasses = vAcs.get("sunglasses");
		String tail = vAcs.get("tail");
		if (that != null) {
			addAccessory(player.getDisplayName(), new TopHat(modelBipedMain), that);
		}
		if (nhat != null) {
			addAccessory(player.getDisplayName(), new NotchHat(modelBipedMain), nhat);
		}
		if (brace != null) {
			addAccessory(player.getDisplayName(), new Bracelet(modelBipedMain), brace);
		}
		if (wings != null) {
			addAccessory(player.getDisplayName(), new Wings(modelBipedMain), wings);
		}
		if (ears != null) {
			addAccessory(player.getDisplayName(), new Ears(modelBipedMain), ears);
		}
		if (glasses != null) {
			addAccessory(player.getDisplayName(), new Sunglasses(modelBipedMain), glasses);
		}
		if (tail != null) {
			addAccessory(player.getDisplayName(), new Tail(modelBipedMain), tail);
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
				toCreate = new Bracelet(modelBipedMain);
				break;
			case EARS:
				toCreate = new Ears(modelBipedMain);
				break;
			case NOTCHHAT:
				toCreate = new NotchHat(modelBipedMain);
				break;
			case SUNGLASSES:
				toCreate = new Sunglasses(modelBipedMain);
				break;
			case TAIL:
				toCreate = new Tail(modelBipedMain);
				break;
			case TOPHAT:
				toCreate = new TopHat(modelBipedMain);
				break;
			case WINGS:
				toCreate = new Wings(modelBipedMain);
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
	
	private static Map<String, VIP> vips;
    @SuppressWarnings("unchecked")
    public static VIP getVIP(String username) {
        return null;
        /*
        if (vips == null) {
            vips = new ConcurrentHashMap<String, VIP>();
            YamlConfiguration config = VIP.getYAML();
            for (String key : config.getKeys()) {
                try {
                    Map<String, Object> values = (Map<String, Object>) config.getProperty(key);
                    key = key.toLowerCase();
                    String title = (String) values.get("title");
                    title = VIP.formatChatColors(title);
                    String cape = (String) values.get("cape");
                    String armor = (String) values.get("armor");
                    float scale = 1f;
                    if (values.containsKey("scale")) {
                        scale = ((Number) values.get("scale")).floatValue();
                    }
                    Map<String, Integer> particles = (Map<String, Integer>) values.get("particles");
                    Map<String, String> acs = (Map<String, String>) values.get("accessories");
                    VIP vip = new VIP(key, title, cape, particles, acs, armor, scale);
                    vips.put(key, vip);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return vips.get(username.toLowerCase()); */
    }
}
