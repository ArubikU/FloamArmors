package dev.arubiku.floamyarmor;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

public class Compatibility {

  public static enum CompiPlugin {
    ITEMSADDER,
    NEXO,
    CRAFTENGINE,
    NONE
  }

  public static CompiPlugin compa = CompiPlugin.NONE;

  @Nullable
  public static File comps(FloamyArmor plugin) {
    if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
      Compatibility.compa = CompiPlugin.NEXO;
      return Bukkit.getPluginManager().getPlugin("Nexo").getDataFolder();
    }
    if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
      Compatibility.compa = CompiPlugin.ITEMSADDER;
      return Bukkit.getPluginManager().getPlugin("ItemsAdder").getDataFolder();
    }
    if (Bukkit.getPluginManager().isPluginEnabled("CraftEngine")) {
      Compatibility.compa = CompiPlugin.CRAFTENGINE;
      return Bukkit.getPluginManager().getPlugin("CraftEngine").getDataFolder();
    }

    return null;
  }

  public static void copy(File pluginFolder, FloamyArmor plugin) {
    switch (compa) {
      case ITEMSADDER: {
        plugin.cleanupFolder(pluginFolder.toPath().resolve("contents/floamyarmor/resourcepack/").toFile());

        try {
          plugin.copyContent(plugin.getDataFolder().toPath().resolve("output").toFile(),
              pluginFolder.toPath().resolve("contents/floamyarmor/resourcepack/").toFile());
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }
      case CRAFTENGINE: {
        plugin.cleanupFolder(pluginFolder.toPath().resolve("contents/floamyarmor/resourcepack/").toFile());

        try {
          plugin.copyContent(plugin.getDataFolder().toPath().resolve("output").toFile(),
              pluginFolder.toPath().resolve("resources/floamyarmor/resourcepack/").toFile());
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }

      case NEXO: {
        plugin.cleanupFolder(pluginFolder.toPath().resolve("pack/external_packs/floamyarmor/").toFile());

        try {
          plugin.copyContent(plugin.getDataFolder().toPath().resolve("output").toFile(),
              pluginFolder.toPath().resolve("pack/external_packs/floamyarmor/").toFile());
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }

      default:
        break;
    }
  }
}
