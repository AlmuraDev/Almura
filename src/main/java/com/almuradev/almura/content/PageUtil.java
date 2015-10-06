/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2015 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content;

import com.almuradev.almura.Almura;
import com.almuradev.almura.util.Color;
import com.almuradev.almura.util.Colors;
import com.almuradev.almura.util.FileSystem;
import cpw.mods.fml.common.FMLCommonHandler;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PageUtil {

    public static final Path PATH_CONFIG = Paths.get("config", "guide");
    public static final Path PATH_PAGES = Paths.get(PATH_CONFIG.toString(), "pages");
    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");

    static {
        if (Files.notExists(PATH_PAGES)) {
            try {
                Files.createDirectories(PATH_PAGES);

            } catch (Exception e) {
                throw new RuntimeException("Failed to create pages directory!", e);
            }
        }
    }

    private PageUtil() {
    }

    public static void loadAll() {
        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            PageRegistry.clear();
            PageUtil.loadPages(PageUtil.PATH_PAGES, FileSystem.FILTER_YAML_FILES_ONLY);
            Almura.LOGGER.info("Loaded [" + PageRegistry.getAll().size() + "] page(s).");
        }
    }

    public static void loadPages(Path path, DirectoryStream.Filter<Path> filter) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, filter)) {
            for (Path p : stream) {
                final ConfigurationNode root =
                        YAMLConfigurationLoader.builder().setFile(p.toFile()).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build().load();
                try {
                    PageRegistry.putPage(createPage(p.getFileName().toString().replace(".yml", ""), root));
                } catch (ParseException e) {
                    Almura.LOGGER.warn("Failed parsing file [" + p.getFileName() + "] due to invalid date format. Should be MM/dd/yyyy");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed filtering page files from [" + path + "].", e);
        }
    }

    public static Page createPage(String identifier, ConfigurationNode root) throws ParseException {
        final String name = root.getNode("title").getString("No title");
        final int index = root.getNode("index").getInt(0);
        final Date created = DATE_FORMATTER.parse(root.getNode("created").getString("1/1/1900"));
        final String author = root.getNode("author").getString("Unknown");
        final Date lastModified = DATE_FORMATTER.parse(root.getNode("last-modified").getString("1/1/1900"));
        final String lastContributor = root.getNode("last-contributor").getString("Unknown");
        final String contents = replaceColorCodes("&", root.getNode("contents").getString(""), true);

        return new Page(identifier, index, name, created, author, lastModified, lastContributor, contents);
    }

    public static void savePage(String identifier, Page page) throws IOException {
        final Path p = Paths.get(PATH_PAGES.toString(), identifier + ".yml");
        if (Files.notExists(p)) {
            Files.createFile(p);
        }
        final YAMLConfigurationLoader loader =
                YAMLConfigurationLoader.builder().setFile(p.toFile()).setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
        final ConfigurationNode root = loader.load();
        root.getNode("index").setValue(page.getIndex());
        root.getNode("title").setValue(page.getName());
        root.getNode("created").setValue(DATE_FORMATTER.format(page.getCreated()));
        root.getNode("author").setValue(page.getAuthor());
        root.getNode("last-modified").setValue(DATE_FORMATTER.format(page.getLastModified()));
        root.getNode("last-contributor").setValue(page.getLastContributor());
        root.getNode("contents").setValue(page.getContents());
        loader.save(root);
    }

    public static void deletePage(String identifier) throws IOException {
        Files.deleteIfExists(Paths.get(PATH_PAGES.toString(), identifier + ".yml"));
    }

    /**
     * Replaces all '§' with the provided char.
     * @param colorCodeStart The char replacement
     * @param rawText The text to replace
     * @return The replaced text
     */
    public static String replaceColorCodes(String colorCodeStart, String rawText, boolean toColorsList) {
        for (Color c : Colors.getBuiltinColors()) {
            //Replace color map -> char + charcode
            if (!toColorsList) {
                rawText = rawText.replaceAll("" + c, colorCodeStart + "" + c.getChatCode());
                //Replace char + charcode -> color map
            } else {
                rawText = rawText.replaceAll(colorCodeStart + "" + c.getChatCode(), "" + c);
            }
        }

        return rawText;
    }
}
