package com.almuradev.almura.client.command;

import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.PackCreator;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.util.FileSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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

        switch (args[0].toUpperCase()) {
            case "RELOAD":
                onCommandReload((EntityPlayer) sender, Arrays.copyOfRange(args, 1, args.length));
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
        // Handle textures
        Minecraft.getMinecraft().refreshResources();
    }
}
