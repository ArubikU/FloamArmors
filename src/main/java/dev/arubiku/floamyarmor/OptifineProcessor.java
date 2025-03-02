package dev.arubiku.floamyarmor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.configuration.file.FileConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.arubiku.floamyarmor.VanillaOverrides.ArmorOverrides;
import dev.arubiku.floamyarmor.Utils;

public class OptifineProcessor {

  private final FloamyArmor plugin;
  private final FileConfiguration config;
  private final String armorName;
  private static int idFollower = 1;
  private final File armorFolder;

  public static void resetFollower() {
    idFollower = 1;
  }

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
      Files.copy(optifinelayer1.toPath(),
          new File(outputFolder, "textures/armor/" + armorName + "/layer_1.png").toPath());

    }
    if (optifinelayer2.exists()) {
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
      Files.copy(cemLayer1.toPath(), new File(outputFolder, "emf/cem/outer_armor" + (id + 1) + ".jem").toPath());
    }
    if (cemLayer2.exists()) {
      Files.copy(cemLayer2.toPath(), new File(outputFolder, "emf/cem/inner_armor" + (id + 1) + ".jem").toPath());
    }

  }

  private void editCemFiles(File outputFolder, int id) throws IOException {
    File cemLayer1 = new File(outputFolder, "emf/cem/outer_armor" + (id + 1) + ".jem");
    File cemLayer2 = new File(outputFolder, "emf/cem/inner_armor" + (id + 1) + ".jem");

    if (cemLayer1.exists()) {
      editJson(cemLayer1, "texture", "textures/armor/" + armorName + "/layer_1.png");
      editJson(cemLayer1, "security", Utils.toBase64("{{USER_NAME}} , {{USER_IDENTIFIER}} , {{NONCE}} , {{USER}} , {{RESOURCE}}"));
      editJson(cemLayer1, "security", Utils.fromBase64("e3tVU0VSX05BTUV9fSAsIHt7VVNFUl9JREVOVElGSUVSfX0gLCB7e05PTkNFfX0gLCB7e1VTRVJ9fSAsIHt7UkVTT1VSQ0V9fQ=="));
      replaceInFile(cemLayer1, "waist", "body");
      replaceInFile(cemLayer1, "right_shoe", "right_leg");
      replaceInFile(cemLayer1, "left_shoe", "left_leg");
      addHeadwearIfNotExists(cemLayer1);
    }
    if (cemLayer2.exists()) {
      editJson(cemLayer2, "texture", "textures/armor/" + armorName + "/layer_2.png");
      editJson(cemLayer2, "security", Utils.toBase64("{{USER_NAME}} , {{USER_IDENTIFIER}} , {{NONCE}} , {{USER}} , {{RESOURCE}}"));
      editJson(cemLayer2, "security", Utils.fromBase64("e3tVU0VSX05BTUV9fSAsIHt7VVNFUl9JREVOVElGSUVSfX0gLCB7e05PTkNFfX0gLCB7e1VTRVJ9fSAsIHt7UkVTT1VSQ0V9fQ=="));
      replaceInFile(cemLayer2, "waist", "body");
      replaceInFile(cemLayer2, "right_shoe", "right_leg");
      replaceInFile(cemLayer2, "left_shoe", "left_leg");
      addHeadwearIfNotExists(cemLayer2);
    }

  }

  private void addHeadwearIfNotExists(File jsonFile) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonFile);
    ObjectNode headwearNode = mapper.createObjectNode();
    headwearNode.put("part", "headwear");
    headwearNode.put("id", "headwear");
    headwearNode.put("invertAxis", "xy");
    headwearNode.putArray("translate").add(0).add(-24).add(0);

    boolean headwearExists = false;
    if (rootNode.has("models") && rootNode.get("models").isArray()) {
      for (var modelNode : rootNode.withArray("models")) {
        if (modelNode.has("part") && "headwear".equals(modelNode.get("part").asText())) {
          headwearExists = true;
          break;
        }
      }
    }

    if (!headwearExists) {
      rootNode.withArray("models").add(headwearNode);
      mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
    }
  }

  private void addToJsonArray(File jsonFile, String path, Object value) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonFile);
    String[] keys = path.split(".");
    ObjectNode currentNode = rootNode;

    for (int i = 0; i < keys.length - 1; i++) {
      currentNode = (ObjectNode) currentNode.get(keys[i]);
      if (currentNode == null) {
        throw new IllegalArgumentException("Invalid path: " + path);
      }
    }

    String arrayKey = keys[keys.length - 1];
    if (!currentNode.has(arrayKey) || !currentNode.get(arrayKey).isArray()) {
      throw new IllegalArgumentException("Path does not point to a JSON array: " + path);
    }

    currentNode.withArray(arrayKey).addPOJO(value);
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
    }

    private void editJson(File jsonFile, String path, Object value) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = (ObjectNode) mapper.readTree(jsonFile);
    String[] keys = path.split("\\.");
    ObjectNode currentNode = rootNode;

    for (int i = 0; i < keys.length - 1; i++) {
      if (!currentNode.has(keys[i]) || !currentNode.get(keys[i]).isObject()) {
      currentNode.set(keys[i], mapper.createObjectNode());
      }
      currentNode = (ObjectNode) currentNode.get(keys[i]);
    }

    currentNode.putPOJO(keys[keys.length - 1], value);
    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
    }

    public void replaceInFile(File file, String target, String replacement) throws IOException {
    // Read the file content into a string
    String content = new String(Files.readAllBytes(file.toPath()));

    // Replace the target string with the replacement string
    content = content.replace(target, replacement);

    // Write the updated content back to the file
    Files.write(file.toPath(), content.getBytes());
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
        writer.write("models." + (referencialNumberId + i) + "=" + (id + 1) + "\n");

        // vanilla overrides
        if (!config.getString("vanilla_overrides", "false").equals("false")) {

          ArmorOverrides overrides = VanillaOverrides.getOverrides(config.getString("vanilla_overrides", "false"));

          writer.write("nbt." + (referencialNumberId + i) + ".Inventory=raw:iregex:.*Slot:" + (103 - i)
              + "b.*?" + overrides.getSlot((103 - i)) + ".*?\n");
          continue;
        }

        writer.write("nbt." + (referencialNumberId + i) + ".Inventory=raw:iregex:.*Slot:" + (103 - i)
            + "b.*?" + config.getString("id").replace(":", "_") + ".*?\n");
      }
    }
  }
}
