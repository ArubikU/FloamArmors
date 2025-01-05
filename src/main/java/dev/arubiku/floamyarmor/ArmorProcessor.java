package dev.arubiku.floamyarmor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.configuration.file.FileConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ArmorProcessor {

  private static final String INPUT_RP_FOLDER = "rp";
  private static final String OUTPUT_FOLDER = "output";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final FloamyArmor plugin;
  private final FileConfiguration config;
  private final String armorName;
  private final Set<String> usedVariables;
  private static int offset = 1000;
  private final OptifineProcessor optifineProcessor;

  public ArmorProcessor(FloamyArmor plugin, FileConfiguration config, String armorName, Set<String> usedVariables) {
    this.plugin = plugin;
    this.config = config;
    this.armorName = armorName;
    this.usedVariables = usedVariables;
    this.optifineProcessor = new OptifineProcessor(plugin, config, armorName);
  }

  public void process() throws IOException {
    File outputFolder = this.plugin.getDataFolder().toPath().resolve(OUTPUT_FOLDER).resolve("assets/minecraft")
        .toFile();
    if (!outputFolder.exists() && !outputFolder.mkdirs()) {
      throw new IOException("Failed to create output folder: " + outputFolder.getPath());
    }

    if (!config.getBoolean("its-copy", false)) {

      processGlslFiles(outputFolder);
      readdArmorShader(outputFolder);
    }
    copyTextures(outputFolder);
    writeEquipment(outputFolder);
    if (config.getStringList("versions").contains("optifine")) {

      optifineProcessor.process(outputFolder);
    }
  }

  public void readdArmorShader(File outputFolder) throws IOException {
    File shaderFolder = new File(outputFolder, "shaders/include/mods/armor/" + armorName);
    if (!shaderFolder.exists() && !shaderFolder.mkdirs()) {
      throw new IOException("Failed to create shader folder: " + shaderFolder.getPath());
    }

    File glslShader = new File(plugin.getDataFolder(),
        "armors/" + armorName + "/" + config.getString("glsl-file", "armor.glsl"));
    if (glslShader.exists()) {
      Files.copy(glslShader.toPath(), new File(shaderFolder, "armor.glsl").toPath());
      File armorPasted = new File(shaderFolder, "armor.glsl");
      // read and we are gona to replace all <internal-id.[r|g|b]> with the config
      // values
      String content = Files.readString(armorPasted.toPath());
      content = content.replace("<internal-id.r>", String.valueOf(config.getInt("internal-color.r", 0)));
      content = content.replace("<internal-id.g>", String.valueOf(config.getInt("internal-color.g", 0)));
      content = content.replace("<internal-id.b>", String.valueOf(config.getInt("internal-color.b", 0)));
      Files.write(armorPasted.toPath(), content.getBytes());

    }

    File armorsGlsl = this.plugin.getDataFolder().toPath().resolve(OUTPUT_FOLDER)
        .resolve("assets/minecraft/shaders/include/mods/armor/armor.glsl")
        .toFile();

    if (!armorsGlsl.exists()) {

      armorsGlsl.getParentFile().mkdirs();
      armorsGlsl.createNewFile();
      if (!armorsGlsl.exists()) {

        throw new IOException("Failed to create armor.glsl file: " + armorsGlsl.getPath());
      }
    }

    try (FileWriter writer = new FileWriter(armorsGlsl, true)) {
      writer.write("#moj_import<mods/armor/" + armorName + "/armor.glsl>\n");
      plugin.getFLogger().info("Added " + armorName + " to armor.glsl");

    }
  }

  private void processGlslFiles(File outputFolder) throws IOException {
    File shaderFolder = new File(outputFolder, "shaders/include/mods/armor/" + armorName);
    if (!shaderFolder.exists() && !shaderFolder.mkdirs()) {
      throw new IOException("Failed to create shader folder: " + shaderFolder.getPath());
    }

    File partsShader = new File(shaderFolder, "parts.glsl");
    if (partsShader.exists()) {
      if (!partsShader.createNewFile()) {

        throw new IOException("Failed to create armor shader file: " + partsShader.getPath());
      } else {
        plugin.getFLogger().info("Created armor shader file: " + partsShader.getPath());
      }
    }
    try (FileWriter writer = new FileWriter(partsShader)) {
      List<String> glslVariables = config.getStringList("glsl-variables");
      for (String variable : glslVariables) {
        if (!usedVariables.add(variable)) {
          plugin.getFLogger().warning("Variable already used: " + variable);
          continue;
        }
        writer.write("#define " + variable + " " + offset + "\n");
        offset++;
      }
    }

    File partsGlsl = this.plugin.getDataFolder().toPath().resolve(OUTPUT_FOLDER)
        .resolve("assets/minecraft/shaders/include/mods/armor/parts.glsl")
        .toFile();

    if (!partsGlsl.exists()) {

      partsGlsl.getParentFile().mkdirs();
      partsGlsl.createNewFile();
      if (!partsGlsl.exists()) {

        throw new IOException("Failed to create parts.glsl file: " + partsGlsl.getPath());
      }
    }

    try (FileWriter writer = new FileWriter(partsGlsl, true)) {
      writer.write("#moj_import<mods/armor/" + armorName + "/parts.glsl>\n");
      plugin.getFLogger().info("Added " + armorName + " to parts.glsl");

    }
  }

  private void copyTextures(File outputFolder) throws IOException {

    File layer1 = plugin.getDataFolder().toPath()
        .resolve("armors/" + armorName + "/" + config.getString("vanilla_layer_1"))
        .toFile();
    File layer2 = plugin.getDataFolder().toPath()
        .resolve("armors/" + armorName + "/" + config.getString("vanilla_layer_2"))
        .toFile();

    Color color = new Color(
        config.getInt("color.r", 0),
        config.getInt("color.g", 0),
        config.getInt("color.b", 0));
    ColorApplier baseColor = new ColorApplier(63, 31, color);
    if (layer1.exists()) {
      List<ColorApplier> colors = List.of(baseColor, new ColorApplier(63, 30, Color.BLACK));
      editImage(layer1, colors,
          new File(outputFolder,
              "textures/entity/equipment/humanoid/" + config.getString("id").replace(":", "_") + "_layer_1.png"));
    }
    if (layer2.exists()) {
      List<ColorApplier> colors = List.of(baseColor, new ColorApplier(63, 30, Color.WHITE));
      editImage(layer1, colors,
          new File(outputFolder, "textures/entity/equipment/humanoid_leggings/"
              + config.getString("id").replace(":", "_") + "_layer_2.png"));
    }
  }

  public void writeEquipment(File outputFolder) throws IOException {
    File equipmentFolder = new File(outputFolder, "equipment");
    if (!equipmentFolder.exists() && !equipmentFolder.mkdirs()) {
      throw new IOException("Failed to create equipment folder: " + equipmentFolder.getPath());
    }

    File equipmentJson = new File(equipmentFolder, config.getString("id").replace(":", "_") + ".json");
    if (!equipmentJson.exists()) {
      if (!equipmentJson.createNewFile()) {
        throw new IOException("Failed to create equipment json file: " + equipmentJson.getPath());
      } else {
        plugin.getFLogger().info("Created equipment json file: " + equipmentJson.getPath());
      }
    }
    Equipment equipment = new Equipment(Map.of(
        "humanoid", List.of(new Equipment.Layer("minecraft:" + config.getString("id").replace(":", "_") + "_layer_1")),
        "humanoid_leggings",
        List.of(new Equipment.Layer("minecraft:" + config.getString("id").replace(":", "_") + "_layer_2"))));

    try (FileWriter writer = new FileWriter(equipmentJson)) {
      OBJECT_MAPPER.writeValue(writer, equipment);
    }
  }

  public static class Equipment {
    public static class Layer {
      public String texture;

      public Layer(String texture) {
        this.texture = texture;
      }
    }

    public Map<String, List<Layer>> layers;

    public Equipment(Map<String, List<Layer>> layers) {
      this.layers = layers;
    }

  }

  public static class ColorApplier {
    int x;
    int y;
    int rgb;

    public ColorApplier(int x, int y, int rgb) {
      this.x = x;
      this.y = y;
      this.rgb = rgb;
    }

    public ColorApplier(int x, int y, Color color) {
      this.x = x;
      this.y = y;
      this.rgb = color.getRGB();
    }
  }

  private void editImage(File image, List<ColorApplier> colors, File output) throws IOException {
    BufferedImage bufferedImage = ImageIO.read(image);

    if (!output.exists()) {
      output.getParentFile().mkdirs();
    }
    for (ColorApplier color : colors) {
      if (color.x >= 0 && color.x < bufferedImage.getWidth() && color.y >= 0 && color.y < bufferedImage.getHeight()) {
        bufferedImage.setRGB(color.x, color.y, color.rgb);
      } else {
        throw new IllegalArgumentException("Las coordenadas están fuera del rango de la imagen.");
      }
    }

    ImageIO.write(bufferedImage, "png", output);

  }

  public int getOffset() {
    return offset;
  }
}
