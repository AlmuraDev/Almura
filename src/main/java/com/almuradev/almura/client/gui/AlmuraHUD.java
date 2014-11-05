package com.almuradev.almura.client.gui;

import java.awt.Color;

import com.almuradev.almura.Almura;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class AlmuraHUD extends MalisisGui {
    
    private static final Color greenBar = new Color(0,1f,0,1f);
    private static final Color orangeBar = new Color(0.8039f,0.6784f,0f,1f);
    private static final Color redBar = new Color(0.69f,0.09f,0.12f,1f);
    private static final Color grayBar = new Color(0.69f,0.09f,0.12f,0.3f);
    public final UIContainer hudContainer, healthBar, hungerBar, staminaBar, xpBar, armorBar;
    public final UILabel almuraTitle, playerTitle;
    public final GuiTexture barTexture, heartTexture, armorTexture, xpTexture, hungerTexture, staminaTexture;
    public final UIImage healthBarTextureImage, heartTextureImage, hungerBarTextureImage, staminaBarTextureImage, xpBarTextureImage, armorBarTextureImage, armorTextureImage, hungerTextureImage, staminaTextureImage, xpTextureImage;
        
    @SuppressWarnings("rawtypes")
    public AlmuraHUD() {
        guiscreenBackground = false;
        
        // Contruct Hud with all elements
        hudContainer = new UIContainer(this);
        hudContainer.setSize(super.width, super.height);
        
        // Bar Setups
        barTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/barwidget.png"));       
        heartTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/health.png"));
        hungerTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/hunger.png"));
        armorTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/armor.png"));
        staminaTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/stamina.png"));
        xpTexture = new GuiTexture(new ResourceLocation(Almura.MOD_ID.toLowerCase(), "textures/gui/xp.png"));
        
        // Health Texture
        heartTextureImage = new UIImage(this, heartTexture, null);
        heartTextureImage.setPosition(5, 15, Anchor.LEFT | Anchor.TOP);
        heartTextureImage.setSize(7, 7); // Keep this.
        
        // Almura Title
        playerTitle = new UILabel(this,"PlayerName");
        playerTitle.setPosition(6, 2, Anchor.LEFT | Anchor.TOP);
        playerTitle.setColor(0xffffffff);
        playerTitle.setSize(7, 7); // Keep this.
        
        // Health Bar
        healthBarTextureImage = new UIImage(this, barTexture, null);
        healthBarTextureImage.setPosition(20, 15, Anchor.LEFT | Anchor.TOP);
        healthBarTextureImage.setSize(103, 7); // Keep this.        
        healthBar = new UIContainer(this, 0,0);
        healthBar.setPosition(21, 15, Anchor.LEFT | Anchor.TOP);
                      
        // Armor Bar
        armorBarTextureImage = new UIImage(this, barTexture, null);
        armorBarTextureImage.setPosition(20, 25, Anchor.LEFT | Anchor.TOP);
        armorBarTextureImage.setSize(103, 7);        
        armorBar = new UIContainer(this, 0,0);
        armorBar.setPosition(21, 25, Anchor.LEFT | Anchor.TOP);
        
        // Armor Texture
        armorTextureImage = new UIImage(this, armorTexture, null);
        armorTextureImage.setPosition(5, 25, Anchor.LEFT | Anchor.TOP);
        armorTextureImage.setSize(7, 7); // Keep this.
        
        // Almura Title
        almuraTitle = new UILabel(this,"Almura");
        almuraTitle.setPosition(-15, 2, Anchor.CENTER | Anchor.TOP);
        almuraTitle.setColor(0xffffffff);
        almuraTitle.setSize(7, 7); // Keep this.
        
        // Hunger Bar
        hungerBarTextureImage = new UIImage(this, barTexture, null);
        hungerBarTextureImage.setPosition(0, 15, Anchor.CENTER | Anchor.TOP);
        hungerBarTextureImage.setSize(103, 7);        
        hungerBar = new UIContainer(this, 0,0);
        hungerBar.setPosition(0, 15, Anchor.CENTER | Anchor.TOP);
                        
        // Hunger Texture
        hungerTextureImage = new UIImage(this, hungerTexture, null);
        hungerTextureImage.setPosition(-60, 15, Anchor.CENTER | Anchor.TOP);
        hungerTextureImage.setSize(7, 7); // Keep this.
        
        // Stamina Bar
        staminaBarTextureImage = new UIImage(this, barTexture, null);
        staminaBarTextureImage.setPosition(0, 25, Anchor.CENTER | Anchor.TOP);
        staminaBarTextureImage.setSize(103, 7);        
        staminaBar = new UIContainer(this, 0,0);
        staminaBar.setPosition(1, 25, Anchor.CENTER | Anchor.TOP);
       
        // Stamina Texture
        staminaTextureImage = new UIImage(this, staminaTexture, null);
        staminaTextureImage.setPosition(-60, 25, Anchor.CENTER | Anchor.TOP);
        staminaTextureImage.setSize(7, 7); // Keep this.
        
        // XP Bar
        xpBarTextureImage = new UIImage(this, barTexture, null);
        xpBarTextureImage.setPosition(-5, 25, Anchor.RIGHT | Anchor.TOP);
        xpBarTextureImage.setSize(103, 7);        
        xpBar = new UIContainer(this, 0,0);
        xpBar.setPosition(-6, 25, Anchor.RIGHT | Anchor.TOP);
        
        // XP Texture
        xpTextureImage = new UIImage(this, xpTexture, null);
        xpTextureImage.setPosition(-110, 25, Anchor.RIGHT | Anchor.TOP);
        xpTextureImage.setSize(7, 7); // Keep this.
                        
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
        
        healthBar.setSize(playerHealth, 7);
        hungerBar.setSize(playerHunger, 7);
        staminaBar.setSize(playerStamina,  7);
        
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
        
        hungerBar.setPosition(0-(50-(playerHunger/2)), 15, Anchor.CENTER | Anchor.TOP); //Keep bar from center point rendering
        staminaBar.setPosition(0-(50-(playerStamina/2)), 25, Anchor.CENTER | Anchor.TOP); //Keep bar from center point rendering
        playerTitle.setText(Minecraft.getMinecraft().thePlayer.getDisplayName());
        
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
}
