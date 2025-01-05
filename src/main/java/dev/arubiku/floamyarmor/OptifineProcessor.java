package dev.arubiku.floamyarmor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.configuration.file.FileConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OptifineProcessor {

  private final FloamyArmor plugin;
  private final FileConfiguration config;
  private final String armorName;
  private static int idFollower = 1;
  private final File armorFolder;

  public OptifineProcessor(FloamyArmor plugin, FileConfiguration config, String armorName) {
    this.plugin = plugin;
    this.config = config;
    this.armorName = armorName;
    this.armorFolder = new File(plugin.getDataFolder(), "armors/" + armorName);
  }

  public void process(File outputFolder) throws IOException {

    String armorId = config.getString("id", "");
    String[] parts = armorId.split(":");
    String namespace = parts.length > 0 ? parts[0] : "";
    int id = idFollower;
    copyTextureFiles(outputFolder);
    updatePropertiesFiles(outputFolder, namespace, id);
    copyCemFiles(outputFolder, id);
    editCemFiles(outputFolder, id);
    OptifineProcessor.idFollower++;
  }

  private void copyTextureFiles(File outputFolder) throws IOException {

    File optifinelayer1 = new File(armorFolder,
        this.config.getString("optifine_layer_1_texture", this.config.getString("vanilla_layer_1", "layer_1.png")));
    File optifinelayer2 = new File(armorFolder,
        this.config.getString("optifine_layer_2_texture", this.config.getString("vanilla_layer_2", "layer_2.png")));
    File folder = new File(outputFolder, "textures/armor/" + armorName + "/");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    if (optifinelayer1.exists()) {
      // paste on outputfolder/textures/armor/tourne/sculk/layer_2.png
      Files.copy(optifinelayer1.toPath(),
          new File(outputFolder, "textures/armor/" + armorName + "/layer_1.png").toPath());

    }
    if (optifinelayer2.exists()) {
      // paste on outputfolder/textures/armor/tourne/sculk/layer_2.png
      Files.copy(optifinelayer2.toPath(),
          new File(outputFolder, "textures/armor/" + armorName + "/layer_2.png").toPath());
    }
  }

  private void copyCemFiles(File outputFolder, int id) throws IOException {
    File cemLayer1 = new File(armorFolder, this.config.getString("optifine_layer_1", "outer_armor.jem"));
    File cemLayer2 = new File(armorFolder, this.config.getString("optifine_layer_2", "inner_armor.jem"));
    File folder = new File(outputFolder, "emf/cem/");
    if (!folder.exists()) {
      folder.mkdirs();
    }
    if (cemLayer1.exists()) {
      Files.copy(cemLayer1.toPath(), new File(outputFolder, "emf/cem/outer_armor" + id + ".jem").toPath());
    }
    if (cemLayer2.exists()) {
      Files.copy(cemLayer2.toPath(), new File(outputFolder, "emf/cem/inner_armor" + id + ".jem").toPath());
    }

  }

  private void editCemFiles(File outputFolder, int id) throws IOException {
    File cemLayer1 = new File(outputFolder, "emf/cem/outer_armor" + id + ".jem");
    File cemLayer2 = new File(outputFolder, "emf/cem/inner_armor" + id + ".jem");

    if (cemLayer1.exists()) {
      editJson(cemLayer1, "texture", "textures/armor/" + armorName + "/layer_1.png");
    }
    if (cemLayer2.exists()) {
      editJson(cemLayer2, "texture", "textures/armor/" + armorName + "/layer_2.png");
    }

  }

  private void editJson(File jsonFile, String path, Object value) throws IOException {
    // Crear el ObjectMapper para trabajar con JSON
    ObjectMapper mapper = new ObjectMapper();

    // Leer el archivo JSON y convertirlo en un ObjectNode
    ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonFile);

    // Dividir el path por los puntos para navegar el JSON
    String[] keys = path.split("\\.");
    ObjectNode currentNode = rootNode;

    // Navegar hasta el nodo correspondiente al path
    for (int i = 0; i < keys.length - 1; i++) {
      currentNode = (ObjectNode) currentNode.get(keys[i]);
      if (currentNode == null) {
        throw new IllegalArgumentException("Path inválido: " + path);
      }
    }

    // Actualizar el valor en el último nodo del path
    currentNode.putPOJO(keys[keys.length - 1], value);

    // Escribir el JSON actualizado de nuevo en el archivo
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
  }

  private void updatePropertiesFiles(File outputFolder, String namespace, int id) throws IOException {
    updateInnerArmorProperties(outputFolder, namespace, id);
    updateOuterArmorProperties(outputFolder, namespace, id);
  }

  private void updateInnerArmorProperties(File outputFolder, String namespace, int id) throws IOException {
    File innerArmorFile = new File(outputFolder, "emf/cem/inner_armor.properties");
    if (!innerArmorFile.exists()) {
      innerArmorFile.getParentFile().mkdirs();
      innerArmorFile.createNewFile();
    }
    appendToPropertiesFile(innerArmorFile, namespace, id);
  }

  private void updateOuterArmorProperties(File outputFolder, String namespace, int id) throws IOException {
    File outerArmorFile = new File(outputFolder, "emf/cem/outer_armor.properties");
    if (!outerArmorFile.exists()) {
      outerArmorFile.getParentFile().mkdirs();
      outerArmorFile.createNewFile();
    }
    appendToPropertiesFile(outerArmorFile, namespace, id);
  }

  private void appendToPropertiesFile(File file, String namespace, int id) throws IOException {
    int numericId;
    try {
      numericId = id;
    } catch (NumberFormatException e) {
      plugin.getLogger().warning("Invalid numeric ID for armor: " + id);
      return;
    }

    int referencialNumberId = 4 * (numericId - 2) + 5;

    try (FileWriter writer = new FileWriter(file, true)) {
      for (int i = 0; i < 4; i++) {
        writer.write("models." + (referencialNumberId + i) + "=" + id + "\n");
        writer.write("nbt." + (referencialNumberId + i) + ".Inventory=raw:iregex:.*Slot:" + (103 - i)
            + "b.*?armorcustom:\"" + config.getString("id").replace(":", "") + "\".*?\n");
      }
    }
  }
}
