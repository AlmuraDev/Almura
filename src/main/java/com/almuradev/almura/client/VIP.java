/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import java.util.HashMap;
import java.util.Map;

public class VIP {
    private final String username;
    private final String title;
    private final String cape;
    private final String armorBaseUrl;
    private final float scale;
    private final Map<String, Integer> particles;
    private final Map<String, String> acs;

    public VIP(String username, String title, String cape, Map<String, Integer> particles, Map<String, String> acs, String armor, float scale) {
        this.username = username;
        this.title = title;
        this.cape = cape;
        this.armorBaseUrl = armor;
        this.scale = scale;
        this.acs = acs;
        if (particles != null) {
            this.particles = new HashMap<String, Integer>(particles);
        } else {
            this.particles = new HashMap<String, Integer>(1);
        }
    }

    public static String formatChatColors(String message) {
        message = message.replaceAll("(&([a-fA-F0-9]))", "\u00A7$2");
        message = message.replaceAll("(&([k-oK-O0-9]))", "\u00A7$2");
        message = message.replaceAll("(&([r|R]))", "\u00A7$2");
        return message;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Integer> getParticles() {
        return particles;
    }

    public String getCape() {
        return cape;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VIP) {
            VIP other = (VIP) obj;
            if (other.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    public String getUsername() {
        return username;
    }

    public float getScale() {
        return scale;
    }

    public String getArmor(int id) {
        if (armorBaseUrl != null) {
            return armorBaseUrl + "_" + id + ".png";
        } else {
            return null;
        }
    }

    public Map<String, String> Accessories() {
        return acs;
    }
}
