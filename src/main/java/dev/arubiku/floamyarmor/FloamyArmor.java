package dev.arubiku.floamyarmor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.mojang.brigadier.arguments.StringArgumentType;

import dev.arubiku.floamyarmor.VanillaOverrides.ArmorOverrides;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class FloamyArmor extends JavaPlugin {

  private ArmorManager armorManager;
  public static Audience console = Bukkit.getServer().getConsoleSender();

  public static class FloamyLogger {
    public void info(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<green>" + message));
    }

    public void severe(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<red>" + message));
    }

    public void warning(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>" + message));
    }

    public void fine(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<gray>" + message));
    }

    public void finer(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<dark_gray>" + message));
    }

    public void dangerous(String message) {
      FloamyArmor.console.sendMessage(MiniMessage.miniMessage().deserialize("<red>" + message));
    }

  }

  private FloamyLogger logger;

  public FloamyLogger getFLogger() {
    return logger;
  }

  private File CompatibilityPlugin = null;

  public static Set<String> armors = new HashSet<>();

  @Override
  public void onEnable() {
    this.logger = new FloamyLogger();
    this.createRequiredFolders();
    this.cleanupFolder("output");
    this.CompatibilityPlugin = Compatibility.comps(this);
    try {
      this.copyContent(getDataFolder().toPath().resolve("rp").toFile(),
          getDataFolder().toPath().resolve("output").toFile());
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.armorManager = new ArmorManager(this);
    this.armorManager.processArmors();

    for (File file : getDataFolder().toPath().resolve("armors").toFile().listFiles()) {
      if (file.isDirectory()) {
        FloamyArmor.armors.add(file.getName());
      }
    }
    try {
      this.zipOutput();
    } catch (IOException e) {
      this.getFLogger().dangerous("Failed to zip output files");
      e.printStackTrace();
    }
    try {

      this.registerCommands();
    } catch (Throwable e) {
      this.getFLogger().dangerous("Tryied to register commands but not paper loaded");
      e.printStackTrace();
    }

    if (this.CompatibilityPlugin != null) {

      if (this.CompatibilityPlugin.exists()) {
        Compatibility.copy(CompatibilityPlugin, this);
      }
    }
  }

  private void registerCommands() {

    LifecycleEventManager<Plugin> manager = this.getLifecycleManager();

    manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      final Commands commands = event.registrar();
      commands.register(
          Commands.literal("floamyarmor")
              .executes(context -> {
                return 1;
              }).then(Commands.literal("zip").executes(context -> {

                zipCommand(context.getSource().getSender());
                return 1;
              })).then(Commands.literal("reload").executes(context -> {

                reloadCommand(context.getSource().getSender());
                return 1;
              })).then(Commands.literal("license").executes(context -> {

                  //editJson(cemLayer2, "security", Utils.toBase64("{{USER_NAME}} , {{USER_IDENTIFIER}} , {{NONCE}} , {{USER}} , {{RESOURCE}}"));
                  //editJson(cemLayer2, "security", Utils.fromBase64("e3tVU0VSX05BTUV9fSAsIHt7VVNFUl9JREVOVElGSUVSfX0gLCB7e05PTkNFfX0gLCB7e1VTRVJ9fSAsIHt7UkVTT1VSQ0V9fQ=="));
                  //verify if is op
                  if (!context.getSource().getSender().isOp()) {
                    return 1;
                  }
                  context.getSource().getSender().sendMessage(
                      MiniMessage.miniMessage().deserialize("<green>FloamyArmor is licensed under the <yellow>MIT</yellow> license."));
                  //write the identifiers to user
                  context.getSource().getSender().sendMessage(
                      MiniMessage.miniMessage().deserialize("<green>Identifiers: <yellow>{{USER_NAME}} , {{USER_IDENTIFIER}} , {{NONCE}} , {{USER}} , {{RESOURCE}}</yellow>"));
                return 1;
              })).then(Commands.literal("getArmor")
                  .then(Commands.argument("id", StringArgumentType.word()).suggests((context, builder) -> {
                    FloamyArmor.armors.forEach(builder::suggest);
                    return builder.buildFuture();
                  })
                      .then(Commands.argument("type", StringArgumentType.word()).suggests((context, builder) -> {
                        List<String> suggestions = Arrays.asList("optifine", "leather", "vanilla", "optifine-vanilla",
                            "leather-vanilla", "optifine-leather", "optifine-leather-vanilla");
                        suggestions.forEach(builder::suggest);
                        return builder.buildFuture();
                      }).executes(context -> {
                        Player player = (Player) context.getSource().getSender();
                        String id = StringArgumentType.getString(context, "id");
                        String type = StringArgumentType.getString(context, "type");
                        getArmorCommand(player, id, type);
                        return 1;
                      }))))
              .build(),
          "some bukkit help description string",
          List.of("floa"));
    });
  }

  private void createRequiredFolders() {
    List<String> folders = Arrays.asList(
        "armors",
        "rp",
        "output");

    for (String folder : folders) {
      createFolder(folder);
    }
  }

  private void createFolder(String folderPath) {
    Path path = getDataFolder().toPath().resolve(folderPath);
    try {
      Files.createDirectories(path);
      getLogger().info("Created folder: " + path);
    } catch (IOException e) {
      getLogger().severe("Failed to create folder: " + path);
      e.printStackTrace();
    }
  }

  // Método para limpiar el contenido de una carpeta
  public void cleanupFolder(File folder) {

    if (!folder.exists() || !folder.isDirectory()) {
      return;
    }

    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        cleanupFolder(file.getAbsolutePath()); // Recursión para subcarpetas
        file.delete();
      } else {
        file.delete();
      }
    }
  }

  // Método para limpiar el contenido de una carpeta
  private void cleanupFolder(String path) {
    File folder = this.getDataFolder().toPath().resolve(path).toFile();

    if (!folder.exists() || !folder.isDirectory()) {
      System.out.println("La carpeta no existe o no es un directorio.");
      return;
    }

    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        cleanupFolder(file.getAbsolutePath()); // Recursión para subcarpetas
        file.delete();
      } else {
        file.delete();
      }
    }
    System.out.println("Folder removed: " + path);
  }

  // Método para copiar el contenido de una carpeta a otra

  public void copyContent(File folder, File destination) throws IOException {
    if (!folder.exists() || !folder.isDirectory()) {
      throw new IOException("Folder not exists or not a directory.");
    }

    if (!destination.exists()) {
      destination.mkdirs(); // Crea la carpeta de destino si no existe
    }

    for (File file : folder.listFiles()) {
      File destFile = new File(destination, file.getName());

      if (file.isDirectory()) {
        copyContent(file, destFile); // Llamada recursiva para subcarpetas
      } else {
        Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private void getArmorCommand(Player player, String id, String type) {
    // verify folder armors/{id}
    File armorFolder = getDataFolder().toPath().resolve("armors").resolve(id).toFile();
    if (!armorFolder.exists() || !armorFolder.isDirectory()) {
      player.sendMessage(ChatColor.RED + "Armor not found.");
      return;
    }
    File config = armorFolder.toPath().resolve("config.yml").toFile();
    if (!config.exists()) {
      config = armorFolder.toPath().resolve("config.yaml").toFile();
    }
    if (!config.exists()) {
      player.sendMessage(ChatColor.RED + "Armor config not found.");
      return;
    }
    FileConfiguration armorConfig = YamlConfiguration.loadConfiguration(config);
    String armortype = armorConfig.getString("vanilla_overrides", "leather");
    ArmorOverrides ov = VanillaOverrides.getOverrides(armortype);
    ItemStack helmet;
    ItemStack chestplate;
    ItemStack leggings;
    ItemStack boots;
    try {
      helmet = new ItemStack(Material.valueOf(ov.getSlot(103)));
      chestplate = new ItemStack(Material.valueOf(ov.getSlot(102)));
      leggings = new ItemStack(Material.valueOf(ov.getSlot(101)));
      boots = new ItemStack(Material.valueOf(ov.getSlot(100)));
    } catch (IllegalArgumentException e) {
      player.sendMessage(ChatColor.RED + "Invalid material in armor config.");
      return;
    }

    String[] types = type.toUpperCase().split("-");
    for (String t : types) {
      switch (t) {
        case "OPTIFINE":
          helmet = processStack(helmet, armorConfig, ArmorType.OPTIFINE);
          chestplate = processStack(chestplate, armorConfig, ArmorType.OPTIFINE);
          leggings = processStack(leggings, armorConfig, ArmorType.OPTIFINE);
          boots = processStack(boots, armorConfig, ArmorType.OPTIFINE);
          break;
        case "LEATHER":
          helmet = processStack(helmet, armorConfig, ArmorType.LEATHER);
          chestplate = processStack(chestplate, armorConfig, ArmorType.LEATHER);
          leggings = processStack(leggings, armorConfig, ArmorType.LEATHER);
          boots = processStack(boots, armorConfig, ArmorType.LEATHER);
          break;
        case "VANILLA":
          helmet = processStack(helmet, armorConfig, ArmorType.VANILLA);
          chestplate = processStack(chestplate, armorConfig, ArmorType.VANILLA);
          leggings = processStack(leggings, armorConfig, ArmorType.VANILLA);
          boots = processStack(boots, armorConfig, ArmorType.VANILLA);
          break;
        default:
          player.sendMessage(ChatColor.RED + "Invalid armor type specified.");
          return;
      }
    }
    Collection<ItemStack> armor = player.getInventory().addItem(helmet, chestplate, leggings, boots).values();
    if (!armor.isEmpty()) {
      player.sendMessage(ChatColor.RED + "Inventory full, armor dropped on the ground.");
      for (ItemStack item : armor) {
        player.getWorld().dropItem(player.getLocation(), item);
      }
    }
  }

  private enum ArmorType {
    OPTIFINE, LEATHER, VANILLA
  }

  private static NamespacedKey pdc = new NamespacedKey("floamyarmor", "armorcustom");

  private ItemStack processStack(ItemStack stack, FileConfiguration config, ArmorType type) {
    switch (type) {
      case OPTIFINE: {
        stack.editMeta(meta -> {
          meta.getPersistentDataContainer().set(pdc, PersistentDataType.STRING,
              config.getString("id").replace(":", "_"));
        });
        return stack;
      }
      case LEATHER: {
        stack.editMeta(LeatherArmorMeta.class, meta -> {

          Color color = Color.fromRGB(config.getInt("color.r"), config.getInt("color.g"), config.getInt("color.b"));
          meta.setColor(color);
        });
        return stack;
      }
      case VANILLA: {
        stack.editMeta(meta -> {
          meta.setCustomModelData(config.getInt("custom_model_data", 0));
        });

        return stack;
      }

      default:
        break;
    }
    return stack;
  }

  private void reloadCommand(CommandSender sender) {
    getFLogger().info("Starting FloamyArmor reload...");

    // Clean up the output folder
    cleanupFolder("output");
    getFLogger().fine("Folder 'output' cleaned up.");

    // Copy the necessary files again
    try {
      copyContent(getDataFolder().toPath().resolve("rp").toFile(),
          getDataFolder().toPath().resolve("output").toFile());
      getFLogger().fine("Content copied from 'rp' to 'output'.");
    } catch (IOException e) {
      getFLogger().severe("Error copying content: " + e.getMessage());
      sender.sendMessage(ChatColor.RED + "Failed to copy resource files.");
      return;
    }
    ArmorProcessor.resetOffset();
    OptifineProcessor.resetFollower();
    this.armorManager = new ArmorManager(this);
    // Reprocess armors
    armorManager.processArmors();
    getFLogger().info("Armor processing completed.");

    // Notify the sender
    sender.sendMessage(ChatColor.GREEN + "FloamyArmor successfully reloaded.");

    if (this.CompatibilityPlugin != null) {
      if (!this.CompatibilityPlugin.exists()) {
        this.CompatibilityPlugin.mkdirs();
      }
      if (this.CompatibilityPlugin.exists()) {
        Compatibility.copy(CompatibilityPlugin, this);
        sender.sendMessage(ChatColor.GREEN + "FloamyArmor added to" + Compatibility.compa.toString() + ".");
      }
    }
  }

  private void zipCommand(CommandSender sender) {
    try {
      zipOutput();
      sender.sendMessage(ChatColor.GREEN + "Output files have been zipped successfully.");
    } catch (IOException e) {
      sender.sendMessage(ChatColor.RED + "Failed to zip output files: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void zipOutput() throws IOException {
    Path zipFile = getDataFolder().toPath().resolve("output.zip");
    if (!zipFile.toFile().exists()) {
      zipFile.toFile().createNewFile();
    }

    File sourceFolder = getDataFolder().toPath().resolve("output").toFile();
    if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
      throw new IOException("La carpeta de origen no existe o no es un directorio.");
    }

    try (FileOutputStream fos = new FileOutputStream(zipFile.toFile());
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      zipFolder(sourceFolder, "", zos);
      System.out.println("Output compressed to: output.zip");
    }
  }

  private void zipFolder(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
    for (File file : folder.listFiles()) {
      String zipEntryName = file.getName();
      if (!parentFolder.equals("")) {
        zipEntryName = parentFolder + "\\" + zipEntryName;
      }
      if (file.isDirectory()) {
        zipFolder(file, zipEntryName, zos); // Recursión para subcarpetas
      } else {
        try (FileInputStream fis = new FileInputStream(file)) {
          zos.putNextEntry(new ZipEntry(zipEntryName));

          byte[] buffer = new byte[1024];
          int length;
          while ((length = fis.read(buffer)) >= 0) {
            zos.write(buffer, 0, length);
          }

          zos.closeEntry();
        }
      }
    }
  }
}
