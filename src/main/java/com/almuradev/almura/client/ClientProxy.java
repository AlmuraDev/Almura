/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client;

import com.almuradev.almura.client.network.play.B03ChunkRegenWand;

import com.almuradev.almura.Almura;
import com.almuradev.almura.CommonProxy;
import com.almuradev.almura.Configuration;
import com.almuradev.almura.client.command.ClientCommand;
import com.almuradev.almura.client.gui.SimpleGui;
import com.almuradev.almura.client.gui.guide.ViewPagesGui;
import com.almuradev.almura.client.gui.ingame.HUDData;
import com.almuradev.almura.client.gui.ingame.IngameDebugHUD;
import com.almuradev.almura.client.gui.ingame.IngameDied;
import com.almuradev.almura.client.gui.ingame.IngameOptions;
import com.almuradev.almura.client.gui.ingame.hud.IngameAlmuraHUD;
import com.almuradev.almura.client.gui.ingame.hud.IngameLessHUD;
import com.almuradev.almura.client.gui.ingame.residence.IngameResidenceHUD;
import com.almuradev.almura.client.gui.menu.DynamicMainMenu;
import com.almuradev.almura.client.network.play.B00PlayerDeathConfirmation;
import com.almuradev.almura.client.network.play.B01ResTokenConfirmation;
import com.almuradev.almura.client.network.play.B02ClientDetailsResponse;
import com.almuradev.almura.client.renderer.accessories.AccessoryManager;
import com.almuradev.almura.content.Page;
import com.almuradev.almura.content.PageRegistry;
import com.almuradev.almura.event.PageDeleteEvent;
import com.almuradev.almura.event.PageInformationEvent;
import com.almuradev.almura.pack.block.PackBlock;
import com.almuradev.almura.pack.container.PackContainerBlock;
import com.almuradev.almura.pack.crop.PackCrops;
import com.almuradev.almura.pack.renderer.BlockRenderer;
import com.almuradev.almura.pack.renderer.ItemRenderer;
import com.almuradev.almura.pack.tree.PackLeaves;
import com.almuradev.almura.pack.tree.PackSapling;
import com.almuradev.almura.server.network.play.S00PageInformation;
import com.almuradev.almura.server.network.play.S01PageDelete;
import com.almuradev.almura.server.network.play.S02PageOpen;
import com.almuradev.almura.server.network.play.bukkit.B00PlayerDisplayName;
import com.almuradev.almura.server.network.play.bukkit.B01PlayerCurrency;
import com.almuradev.almura.server.network.play.bukkit.B02AdditionalWorldInformation;
import com.almuradev.almura.server.network.play.bukkit.B03ResidenceInformation;
import com.almuradev.almura.server.network.play.bukkit.B04PlayerAccessories;
import com.almuradev.almura.server.network.play.bukkit.B05GuiController;
import com.almuradev.almura.server.network.play.bukkit.B06ClientDetailsRequest;
import com.google.common.base.Optional;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class ClientProxy extends CommonProxy {

    public static final String CLASSPATH = "com.almuradev.almura.client.ClientProxy";
    public static final BlockRenderer PACK_BLOCK_RENDERER = new BlockRenderer();
    public static final ItemRenderer PACK_ITEM_RENDERER = new ItemRenderer();
    public static final SimpleNetworkWrapper NETWORK_BUKKIT = new SimpleNetworkWrapper("AM|BUK");

    public static IngameDebugHUD HUD_DEBUG;
    public static SimpleGui HUD_INGAME;
    public static IngameResidenceHUD HUD_RESIDENCE;

    @Override
    public void onPreInitialization(FMLPreInitializationEvent event) {
        super.onPreInitialization(event);
        if (Configuration.FIRST_LAUNCH) {
            Configuration.setOptimizedConfig();
        }
        // Register handlers for Bukkit packets
        NETWORK_BUKKIT.registerMessage(B00PlayerDisplayName.class, B00PlayerDisplayName.class, 0, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B01PlayerCurrency.class, B01PlayerCurrency.class, 1, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B02AdditionalWorldInformation.class, B02AdditionalWorldInformation.class, 2, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B03ResidenceInformation.class, B03ResidenceInformation.class, 3, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B04PlayerAccessories.class, B04PlayerAccessories.class, 4, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B05GuiController.class, B05GuiController.class, 5, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B00PlayerDeathConfirmation.class, B00PlayerDeathConfirmation.class, 6, Side.SERVER);
        NETWORK_BUKKIT.registerMessage(B01ResTokenConfirmation.class, B01ResTokenConfirmation.class, 7, Side.SERVER);
        NETWORK_BUKKIT.registerMessage(B06ClientDetailsRequest.class, B06ClientDetailsRequest.class, 8, Side.CLIENT);
        NETWORK_BUKKIT.registerMessage(B02ClientDetailsResponse.class, B02ClientDetailsResponse.class, 9, Side.SERVER);
        NETWORK_BUKKIT.registerMessage(B03ChunkRegenWand.class, B03ChunkRegenWand.class, 10, Side.SERVER);

        // Register renderers
        PACK_BLOCK_RENDERER.registerFor(PackBlock.class);
        PACK_BLOCK_RENDERER.registerFor(PackCrops.class);
        PACK_BLOCK_RENDERER.registerFor(PackContainerBlock.class);
        PACK_BLOCK_RENDERER.registerFor(PackSapling.class);
        PACK_BLOCK_RENDERER.registerFor(PackLeaves.class);
        // Setup bindings
        Bindings.register();
        // Setup client permissibles
        PermissionsHelper.register();
        ClientCommandHandler.instance.registerCommand(new ClientCommand());
        // Hook into event bus
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu) {
            if (Configuration.FIRST_LAUNCH) {
                Configuration.setOptimizedConfig();
                try {
                    Configuration.setFirstLaunch(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            event.setCanceled(true);
            new DynamicMainMenu(null).display();

        }

        if (event.gui instanceof GuiIngameMenu) {
            event.setCanceled(true);
            new IngameOptions().display();
        }

        if (event.gui instanceof GuiGameOver) {
            event.setCanceled(true);
            new IngameDied().display();

        }
    }

    @SubscribeEvent
    public void onKeyInputEvent(KeyInputEvent event) {
        if (Keyboard.isKeyDown(Bindings.BINDING_OPEN_BACKPACK.getKeyCode())) {
            Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C01PacketChatMessage("/backpack"));
        }
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
        Almura.LOGGER
                .info("This computer can handle a maximum stitched texture size of width [" + Minecraft.getGLMaximumTextureSize() + "] and length ["
                        + Minecraft.getGLMaximumTextureSize() + "].");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNameFormatEvent(net.minecraftforge.event.entity.player.PlayerEvent.NameFormat event) {
        final Optional<String> displayNameOpt = DisplayNameManager.getDisplayName(event.username);
        if (displayNameOpt.isPresent()) {
            event.displayname = displayNameOpt.get();
        }
    }

    @SubscribeEvent
    public void onRenderPlayerSpecialPostEvent(RenderPlayerEvent.Specials.Post event) {
        AccessoryManager.onRenderPlayerSpecialEventPost(event);
    }

    @SubscribeEvent
    public void onRenderGameOverlayEventPost(RenderGameOverlayEvent.Pre event) {
        // Use proper HUD based on configuration
        if (Configuration.HUD_TYPE.equalsIgnoreCase("almura")) {
            if (!(HUD_INGAME instanceof IngameAlmuraHUD)) {
                if (HUD_INGAME != null) {
                    HUD_INGAME.closeOverlay();
                }
                HUD_INGAME = new IngameAlmuraHUD();
                HUD_INGAME.displayOverlay();
            }
            switch (event.type) {
                case HEALTH:
                case ARMOR:
                case FOOD:
                case EXPERIENCE:
                    event.setCanceled(true);
                    break;
                default:
            }
        } else if (Configuration.HUD_TYPE.equalsIgnoreCase("less")) {
            if (!(HUD_INGAME instanceof IngameLessHUD)) {
                if (HUD_INGAME != null) {
                    HUD_INGAME.closeOverlay();
                }
                HUD_INGAME = new IngameLessHUD();
                HUD_INGAME.displayOverlay();
            }
        } else if (Configuration.HUD_TYPE.equalsIgnoreCase("vanilla") && HUD_INGAME != null) {
            HUD_INGAME.closeOverlay();
            HUD_INGAME = null;
        }

        // Toggle residence off/on based on config. Creation of this HUD happens in the packet handler from Bukkit
        if (Configuration.DISPLAY_RESIDENCE_HUD && HUDData.WITHIN_RESIDENCE) {
            if (HUD_RESIDENCE == null) {
                HUD_RESIDENCE = new IngameResidenceHUD();
            }

            HUD_RESIDENCE.displayOverlay();
            HUD_RESIDENCE.updateWidgets();
        } else if (HUD_RESIDENCE != null) {
            HUD_RESIDENCE.closeOverlay();
            HUD_RESIDENCE = null;
        }

        // Toggle enhanced debug off/on based on config.
        if (event.type == ElementType.DEBUG) {
            if (Configuration.DISPLAY_ENHANCED_DEBUG) {
                event.setCanceled(true);
                if (HUD_DEBUG == null) {
                    HUD_DEBUG = new IngameDebugHUD();
                }

                HUD_DEBUG.displayOverlay();
            } else if (HUD_DEBUG != null) {
                HUD_DEBUG.closeOverlay();
                HUD_DEBUG = null;
            }
        }

        // User turned off debug mode, handle it.
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo && HUD_DEBUG != null) {
            HUD_DEBUG.closeOverlay();
            HUD_DEBUG = null;
        }
    }


    @Override
    public void handlePageInformation(MessageContext ctx, S00PageInformation message) {
        Page page = PageRegistry.getPage(message.identifier).orNull();

        if (page == null) {
            page = new Page(message.identifier, message.index, message.title, message.created, message.author,
                    message.lastModified, message.lastContributor, message.contents);
            PageRegistry.putPage(page);
        } else {
            page
                    .setIndex(message.index)
                    .setTitle(message.title)
                    .setCreated(message.created)
                    .setAuthor(message.author)
                    .setLastModified(message.lastModified)
                    .setLastContributor(message.lastContributor)
                    .setContents(message.contents);
        }
        MinecraftForge.EVENT_BUS.post(new PageInformationEvent(page));
    }

    @Override
    public void handlePageDelete(MessageContext ctx, S01PageDelete message) {
        super.handlePageDelete(ctx, message);
        if (ctx.side.isClient()) {
            MinecraftForge.EVENT_BUS.post(new PageDeleteEvent(message.identifier));
        } else if (!MinecraftServer.getServer().isDedicatedServer()) {
            CommonProxy.NETWORK_FORGE.sendToAll(new S01PageDelete(message.identifier));
        }
    }

    @Override
    public void handlePageOpen(MessageContext ctx, S02PageOpen message) {
        final Page page = PageRegistry.getPage(message.identifier).orNull();
        if (page != null && PermissionsHelper.hasPermission(PermissionsHelper.PERMISSIBLE_GUIDE, "auto")) {
            if (!(Minecraft.getMinecraft().currentScreen instanceof ViewPagesGui)) {
                new ViewPagesGui().display();
            }
            ((ViewPagesGui) Minecraft.getMinecraft().currentScreen).selectPage(page);
        }
    }

    @Override
    public boolean canSavePages() {
        return FMLCommonHandler.instance().getEffectiveSide().isServer() && !MinecraftServer.getServer().isDedicatedServer();
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen == null) {
            if (PermissionsHelper.hasPermission(PermissionsHelper.PERMISSIBLE_GUIDE, "open") && Keyboard.isKeyDown(
                    Bindings.BINDING_OPEN_GUIDE.getKeyCode()
            )) {
                new ViewPagesGui().display();
            }
        }
    }
}
