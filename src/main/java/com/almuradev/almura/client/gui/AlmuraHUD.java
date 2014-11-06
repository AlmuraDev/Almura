package com.almuradev.almura.client.gui;

import java.awt.Color;
import com.almuradev.almura.Almura;
import com.almuradev.almura.client.ChatColor;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class AlmuraHUD extends MalisisGui {
    
    private static final Color greenBar = new Color(0,1f,0,1f);
    private static final Color orangeBar = new Color(0.8039f,0.6784f,0f,1f);
    private static final Color redBar = new Color(0.69f,0.09f,0.12f,1f);
    private static final Color grayBar = new Color(0.69f,0.09f,0.12f,0.3f);
    public final UIContainer hudContainer, healthBar, hungerBar, staminaBar, xpBar, armorBar;
    public final UILabel almuraTitle, playerTitle, serverCount, playerCoords, playerCompass, worldDisplay, worldTime, xpLevel;
    public final GuiTexture barTexture, heartTexture, armorTexture, xpTexture, hungerTexture, staminaTexture, playerTexture, compassTexture, coordsTexture, worldTexture, timeTexture;
    public final UIImage healthBarTextureImage, heartTextureImage, hungerBarTextureImage, staminaBarTextureImage, xpBarTextureImage, armorBarTextureImage, armorTextureImage, hungerTextureImage, staminaTextureImage, xpTextureImage, playerTextureImage, compassTextureImage, coordsTextureImage, worldTextureImage, timeTextureImage;
    public int currentHealth, currentStamina, currentHunger, currentArmor, currentXP;
        
    @SuppressWarnings("rawtypes")
    public AlmuraHUD() {
        guiscreenBackground = false;

        // Contruct Hud with all elements
        hudContainer = new UIContainer(this);
        hudContainer.setSize(super.width, super.height);

        // Texture Setups
        barTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/barwidget.png"));
        heartTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/health.png"));
        hungerTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/hunger.png"));
        armorTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/armor.png"));
        staminaTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/stamina.png"));
        xpTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/xp.png"));
        playerTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/player.png"));
        compassTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/compass.png"));
        coordsTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/map.png"));
        worldTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/world.png"));
        timeTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/clock.png"));

        // Player Display Name
        playerTitle = new UILabel(this,"PlayerName");
        playerTitle.setPosition(6, 2, Anchor.LEFT | Anchor.TOP);
        playerTitle.setColor(0xffffffff);
        playerTitle.setSize(7, 7);

        //////////////////////////////// Left Column //////////////////////////////////////

        // Health Texture
        heartTextureImage = new UIImage(this, heartTexture, null);
        heartTextureImage.setPosition(5, 17, Anchor.LEFT | Anchor.TOP);
        heartTextureImage.setSize(7, 7);

        // Health Bar
        healthBarTextureImage = new UIImage(this, barTexture, null);
        healthBarTextureImage.setPosition(20, 17, Anchor.LEFT | Anchor.TOP);
        healthBarTextureImage.setSize(103, 7);
        healthBar = new UIContainer(this, 0,0);
        healthBar.setPosition(21, 17, Anchor.LEFT | Anchor.TOP);

        // Armor Bar
        armorBarTextureImage = new UIImage(this, barTexture, null);
        armorBarTextureImage.setPosition(20, 28, Anchor.LEFT | Anchor.TOP);
        armorBarTextureImage.setSize(103, 7);
        armorBar = new UIContainer(this, 0,0);
        armorBar.setPosition(21, 28, Anchor.LEFT | Anchor.TOP);

        // Armor Texture
        armorTextureImage = new UIImage(this, armorTexture, null);
        armorTextureImage.setPosition(5, 28, Anchor.LEFT | Anchor.TOP);
        armorTextureImage.setSize(7, 7);

        //////////////////////////////// Right Column //////////////////////////////////////

        // Almura Title
        almuraTitle = new UILabel(this,"Almura");
        almuraTitle.setPosition(-15, 2, Anchor.CENTER | Anchor.TOP);
        almuraTitle.setColor(0xffffffff);
        almuraTitle.setSize(7, 7);
        almuraTitle.setFontScale(1.2F);

        // Hunger Bar
        hungerBarTextureImage = new UIImage(this, barTexture, null);
        hungerBarTextureImage.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);
        hungerBarTextureImage.setSize(103, 7);
        hungerBar = new UIContainer(this, 0,0);
        hungerBar.setPosition(0, 17, Anchor.CENTER | Anchor.TOP);

        // Hunger Texture
        hungerTextureImage = new UIImage(this, hungerTexture, null);
        hungerTextureImage.setPosition(-60, 17, Anchor.CENTER | Anchor.TOP);
        hungerTextureImage.setSize(7, 7);

        // Stamina Bar
        staminaBarTextureImage = new UIImage(this, barTexture, null);
        staminaBarTextureImage.setPosition(0, 28, Anchor.CENTER | Anchor.TOP);
        staminaBarTextureImage.setSize(103, 7);
        staminaBar = new UIContainer(this, 0,0);
        staminaBar.setPosition(1, 28, Anchor.CENTER | Anchor.TOP);

        // Stamina Texture
        staminaTextureImage = new UIImage(this, staminaTexture, null);
        staminaTextureImage.setPosition(-60, 28, Anchor.CENTER | Anchor.TOP);
        staminaTextureImage.setSize(7, 7);

        //////////////////////////////// Right Column //////////////////////////////////////

        // Coords Texture
        coordsTextureImage = new UIImage(this, coordsTexture, null);
        coordsTextureImage.setPosition(-205, 5, Anchor.RIGHT | Anchor.TOP);
        coordsTextureImage.setSize(8, 8);

        // Player Coords
        playerCoords = new UILabel(this,"x: 0 y: 0 z: 0");
        playerCoords.setPosition(-95, 5, Anchor.RIGHT | Anchor.TOP);
        playerCoords.setColor(0xffffffff);
        playerCoords.setSize(15, 7);
        playerCoords.setFontScale(0.8F);

        // World Texture
        worldTextureImage = new UIImage(this, worldTexture, null);
        worldTextureImage.setPosition(-45, 5, Anchor.RIGHT | Anchor.TOP);
        worldTextureImage.setSize(8, 8);

        // World Display Label
        worldDisplay = new UILabel(this,"WORLD");
        worldDisplay.setPosition(-15, 5, Anchor.RIGHT | Anchor.TOP);
        worldDisplay.setColor(0xffffffff);
        worldDisplay.setSize(15, 7);
        worldDisplay.setFontScale(0.8F);

        // Player Texture
        playerTextureImage = new UIImage(this, playerTexture, null);
        playerTextureImage.setPosition(-125, 16, Anchor.RIGHT | Anchor.TOP);
        playerTextureImage.setSize(8, 8);

        // Player Count
        serverCount = new UILabel(this,"0 / 50");
        serverCount.setPosition(-105, 16, Anchor.RIGHT | Anchor.TOP);
        serverCount.setColor(0xffffffff);
        serverCount.setSize(15, 7);
        serverCount.setFontScale(0.8F);

        // Compass Texture
        compassTextureImage = new UIImage(this, compassTexture, null);
        compassTextureImage.setPosition(-85, 16, Anchor.RIGHT | Anchor.TOP);
        compassTextureImage.setSize(8, 8);

        // Player Compass
        playerCompass = new UILabel(this,"");
        playerCompass.setPosition(-65, 16, Anchor.RIGHT | Anchor.TOP);
        playerCompass.setColor(0xffffffff);
        playerCompass.setSize(35, 7);
        playerCompass.setFontScale(0.8F);

        // Time Texture
        timeTextureImage = new UIImage(this, timeTexture, null);
        timeTextureImage.setPosition(-35, 16, Anchor.RIGHT | Anchor.TOP);
        timeTextureImage.setSize(7, 7);

        // World Time
        worldTime = new UILabel(this,"7pm");
        worldTime.setPosition(-15, 16, Anchor.RIGHT | Anchor.TOP);
        worldTime.setColor(0xffffffff);
        worldTime.setSize(15, 7);
        worldTime.setFontScale(0.8F);

        // XP Orb Texture
        xpTextureImage = new UIImage(this, xpTexture, null);
        xpTextureImage.setPosition(-140, 28, Anchor.RIGHT | Anchor.TOP);
        xpTextureImage.setSize(7, 7);

        // XP Bar
        xpBarTextureImage = new UIImage(this, barTexture, null);
        xpBarTextureImage.setPosition(-30, 28, Anchor.RIGHT | Anchor.TOP);
        xpBarTextureImage.setSize(103, 7);
        xpBar = new UIContainer(this, 0,0);
        xpBar.setPosition(-6, 28, Anchor.RIGHT | Anchor.TOP);

        // XP Level
        xpLevel = new UILabel(this,"1");
        xpLevel.setPosition(-15, 27, Anchor.RIGHT | Anchor.TOP);
        xpLevel.setColor(0xffffffff);
        xpLevel.setSize(15, 7);
        xpLevel.setFontScale(0.8F);

        hudContainer.add(playerTitle);
        hudContainer.add(healthBar);
        hudContainer.add(heartTextureImage);
        hudContainer.add(healthBarTextureImage);
        hudContainer.add(armorBar);
        hudContainer.add(armorTextureImage);
        hudContainer.add(armorBarTextureImage);
        hudContainer.add(almuraTitle);
        hudContainer.add(hungerBar);
        hudContainer.add(hungerTextureImage);
        hudContainer.add(hungerBarTextureImage);
        hudContainer.add(staminaBar);
        hudContainer.add(staminaTextureImage);
        hudContainer.add(staminaBarTextureImage);
        hudContainer.add(xpBar);
        hudContainer.add(xpTextureImage);
        hudContainer.add(xpBarTextureImage);
        hudContainer.add(coordsTextureImage);
        hudContainer.add(playerCoords);
        hudContainer.add(worldTextureImage);
        hudContainer.add(worldDisplay);
        hudContainer.add(playerTextureImage);
        hudContainer.add(serverCount);
        hudContainer.add(playerCompass);
        hudContainer.add(compassTextureImage);
        hudContainer.add(timeTextureImage);
        hudContainer.add(worldTime);
        hudContainer.add(xpLevel);

        addToScreen(hudContainer);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (Minecraft.getMinecraft().currentScreen != null) {
            return;
        }
        // Draw sidebar gradients - draw two at full width and one at 1 pixel wide to get desired look
        drawGradientRect(0, 0, width, 40, Integer.MIN_VALUE, Integer.MIN_VALUE);
        drawGradientRect(0, 0, width, 40, Integer.MIN_VALUE, Integer.MIN_VALUE);
        drawGradientRect(0, 40, width, 3, Integer.MAX_VALUE, Integer.MAX_VALUE);

        renderer.enableBlending();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        //Almura HUD replaces these GUI elements of Vanilla
        switch (event.type) {
            case HEALTH:
            case FOOD:
            case ARMOR:
            case EXPERIENCE:
                event.setCanceled(true);
            case DEBUG:
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) {
            updateWidgets();

            setWorldAndResolution(Minecraft.getMinecraft(), event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
            drawScreen(event.mouseX, event.mouseY, event.partialTicks);
        }
    }

    public void updateWidgets() {
        int playerHealth = Math.max(0, Math.min( 100, (int) (Minecraft.getMinecraft().thePlayer.getHealth()*5)));
        int playerArmor = getArmorLevel();
        int playerHunger = Math.max(0, Math.min( 100, (Minecraft.getMinecraft().thePlayer.getFoodStats().getFoodLevel()*5)));
        int playerStamina = (int) Math.max(0, Math.min( 100, (Minecraft.getMinecraft().thePlayer.getFoodStats().getSaturationLevel()*5)));
        int currentExp = Math.max(0, Math.min( 100, (int) (Minecraft.getMinecraft().thePlayer.experience*100)));
        healthBar.setSize(playerHealth, 7);
        hungerBar.setSize(playerHunger, 7);
        staminaBar.setSize(playerStamina, 7);
        xpBar.setSize(currentExp, 7);
        xpBar.setBackgroundColor(greenBar.getRGB());
        
        if (currentExp > 0) {
            xpBar.setSize(currentExp, 7);
            xpBar.setVisible(true);
        } else {
            xpBar.setVisible(false);
        }
        
        if (playerArmor > 0) {
            armorBar.setSize(playerArmor, 7);
            armorBar.setVisible(true);
        } else {
            armorBar.setVisible(false);
        }
        
        if (playerStamina > 0) {
            staminaBar.setSize(playerStamina, 7);
            staminaBar.setVisible(true);
        } else {
            staminaBar.setVisible(false);
        }
        
        if (playerHealth>66) {
            healthBar.setBackgroundColor(greenBar.getRGB());
          } else if (playerHealth>=33) {
              healthBar.setBackgroundColor(orangeBar.getRGB());
          } else {
              healthBar.setBackgroundColor(redBar.getRGB());
        }
        
        if (playerArmor>66) {
            armorBar.setBackgroundColor(greenBar.getRGB());
          } else if (playerArmor>=33) {
              armorBar.setBackgroundColor(orangeBar.getRGB());
          } else {
              armorBar.setBackgroundColor(redBar.getRGB());
        }
        
        if (playerHunger>66) {
              hungerBar.setBackgroundColor(greenBar.getRGB());
          } else if (playerArmor>=33) {
              hungerBar.setBackgroundColor(orangeBar.getRGB());
          } else {
              hungerBar.setBackgroundColor(redBar.getRGB());              
        }
        
        if (playerStamina>66) {
            staminaBar.setBackgroundColor(greenBar.getRGB());
        } else if (playerArmor>=33) {
            staminaBar.setBackgroundColor(orangeBar.getRGB());
        } else {
            staminaBar.setBackgroundColor(redBar.getRGB());              
      }
        serverCount.setText(MinecraftServer.getServer().getCurrentPlayerCount() + "/" + MinecraftServer.getServer().getMaxPlayers());//MinecraftServer.getServer().getCurrentPlayerCount()
        hungerBar.setPosition(0-(50-(playerHunger/2)), 17, Anchor.CENTER | Anchor.TOP); //Keep bar from center point rendering
        staminaBar.setPosition(0-(50-(playerStamina/2)), 28, Anchor.CENTER | Anchor.TOP); //Keep bar from center point rendering
        playerTitle.setText(Minecraft.getMinecraft().thePlayer.getDisplayName());
        playerCoords.setText(String.format("x:%d, y:%d, z:%d", (int) Minecraft.getMinecraft().thePlayer.posX, (int) Minecraft.getMinecraft().thePlayer.posY, (int) Minecraft.getMinecraft().thePlayer.posZ)); 
        playerCompass.setText(getCompass());
        coordsTextureImage.setPosition(0-(80+Minecraft.getMinecraft().fontRenderer.getStringWidth(playerCoords.getText())), 5, Anchor.RIGHT | Anchor.TOP);
        worldTextureImage.setPosition(0-(15+Minecraft.getMinecraft().fontRenderer.getStringWidth(worldDisplay.getText())), 5);
        //worldDisplay.setText(Minecraft.getMinecraft().thePlayer.worldObj.getWorldInfo().getWorldName());
        worldDisplay.setText(MinecraftServer.getServer().getWorldName());
        worldTime.setText(getTime());
        xpLevel.setText(""+Minecraft.getMinecraft().thePlayer.experienceLevel);
        xpBar.setPosition(0-(132-(currentExp)), 28, Anchor.RIGHT | Anchor.TOP); //Keep bar from center point rendering
             
    }
    
    public int getArmorLevel() {        
        int armor = 0;
        int armorTotal = 0;       
        final net.minecraft.item.ItemStack[] inv = Minecraft.getMinecraft().thePlayer.inventory.armorInventory;
        
        if (inv != null) {
            for (int i = 0; i < inv.length; i++) {    
                final ItemStack armorItem = inv[i];
                if (armorItem != null) {
                    int max = armorItem.getMaxDamage();
                    armorTotal = armorTotal+max;
                    int current = armorItem.getItemDamage();
                    armor = armor + current;                    
                }
            }
        }
        
        if (armorTotal != 0 && armor != 0) {
            double par1 = armorTotal - armor;
            double par2 = par1 / armorTotal;
            double par3 = par2 * 100;
            return ((int)par3);
        } else {
            return 0;
        }
    }
    
    public String getCompass() {
        int angle = (int) (((Minecraft.getMinecraft().thePlayer.rotationYaw + 360 + 11.25) / 22.5) % 16) + 3;
        // String dirs = "|.|N|.|E|.|S|.|W|.|N|.";
        String dirs = "|.|S|.|W|.|N|.|E|.|S|."; //Match the compass to the in-game compass.
        if (angle >= 2) {
            try {
                return "" + ChatColor.DARK_GRAY + dirs.charAt(angle - 3)
                        + ChatColor.DARK_GRAY + dirs.charAt(angle - 2)
                        + ChatColor.GRAY + dirs.charAt(angle - 1)
                        + ChatColor.WHITE + dirs.charAt(angle)
                        + ChatColor.GRAY + dirs.charAt(angle + 1)
                        + ChatColor.DARK_GRAY + dirs.charAt(angle + 2)
                        + ChatColor.DARK_GRAY + dirs.charAt(angle + 3); 
            } catch (Exception e) {
                //ignore exception being thrown for some dumb reason.
            }
        }
        return "";
    }
    
    public String getTime() {
        int hours = (int)((Minecraft.getMinecraft().thePlayer.worldObj.getWorldInfo().getWorldTime() / 1000L + 6L) % 24L);
        if (hours == 1)
            return (String.format("12am"));
        if (hours > 1 && hours <= 11)
            return (String.format(hours + "am"));
        if (hours == 12)
            return (String.format(hours + "pm"));
        if (hours >= 13) 
            return(String.format((hours-12) + "pm"));
        return "0pm";
    }
}
