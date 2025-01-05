package dev.arubiku.floamyarmor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArmorManager {

  private static final String ARMOR_FOLDER = "armors";
  private static final String INPUT_RP_FOLDER = "rp";
  public static final String OUTPUT_FOLDER = "output";
  private static final int CROP_WIDTH = 64;
  private static final int CROP_HEIGHT = 32;

  private final FloamyArmor plugin;
  private final Set<String> usedVariables;
  private final Map<Color, BufferedImage> layer1Crops;
  private final Map<Color, BufferedImage> layer2Crops;
  private final Map<Color, Color> layer1Colors;
  private final Map<Color, Color> layer2Colors;

  public ArmorManager(FloamyArmor plugin) {
    this.plugin = plugin;
    this.usedVariables = new HashSet<>();
    this.layer1Crops = new HashMap<>();
    this.layer2Crops = new HashMap<>();
    this.layer1Colors = new HashMap<>();
    this.layer2Colors = new HashMap<>();
  }

  public void processArmors() {
    processInputLayers();
    processFormatsLayers();

    File armorFolder = plugin.getDataFolder().toPath().resolve(ARMOR_FOLDER).toFile();
    if (!armorFolder.exists() || !armorFolder.isDirectory()) {
      plugin.getFLogger().warning("Armor folder not found: " + armorFolder.toPath().toAbsolutePath());
      return;
    }

    File[] armorConfigs = armorFolder.listFiles((dir, name) -> new File(dir, name).isDirectory());
    if (armorConfigs == null || armorConfigs.length == 0) {
      plugin.getFLogger().warning("No armors found in folder: " + armorFolder.toPath().toAbsolutePath());
      return;
    }

    for (File armorDir : armorConfigs) {
      File configFile = new File(armorDir, "config.yml");
      if (!configFile.exists()) {
        configFile = new File(armorDir, "config.yaml");
      }
      if (!configFile.exists()) {
        plugin.getFLogger().warning("Config file not found for armor: " + armorDir.getName());
        plugin.getFLogger().dangerous("The path is: " + armorDir.getAbsolutePath() + configFile.getAbsolutePath());
        continue;
      }

      FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

      if (!config.getBoolean("enabled", false)) {
        plugin.getFLogger().info("Armor disabled in config: " + armorDir.getName());
        continue;
      }

      try {
        processArmor(config, armorDir.getName());
      } catch (IOException e) {
        plugin.getFLogger().severe("Failed to process armor: " + armorDir.getName());
        e.printStackTrace();
      }
    }

    generateArmorCordsFile();
  }

  private void processInputLayers() {

    File armorLayer1 = plugin.getDataFolder().toPath().resolve(INPUT_RP_FOLDER)
        .resolve("assets/minecraft/textures/models/armor/leather_layer_1.png").toFile();
    File armorLayer2 = plugin.getDataFolder().toPath().resolve(INPUT_RP_FOLDER)
        .resolve("assets/minecraft/textures/models/armor/leather_layer_2.png").toFile();
    if (!armorLayer1.exists()) {
      // try to fetch from
      // INPUT_RP_FOLDER/assets/minecraft/textures/entity/equipment/humanoid/leather.png
      armorLayer1 = plugin.getDataFolder().toPath().resolve(INPUT_RP_FOLDER)
          .resolve("assets/minecraft/textures/entity/equipment/humanoid/leather.png").toFile();
    }
    if (!armorLayer2.exists()) {
      // try to fetch from
      // INPUT_RP_FOLDER/assets/minecraft/textures/entity/equipment/humanoid_leggings/leather.png
      armorLayer2 = plugin.getDataFolder().toPath().resolve(INPUT_RP_FOLDER)
          .resolve("assets/minecraft/textures/entity/equipment/humanoid_leggings/leather.png").toFile();
    }

    if (armorLayer1.exists()) {
      processLayer(armorLayer1, 1);
    }
    if (armorLayer2.exists()) {
      processLayer(armorLayer2, 2);
    }
  }

  private void processFormatsLayers() {
    File armorsDir = plugin.getDataFolder().toPath().resolve(ARMOR_FOLDER).toFile();
    File[] armorDirs = armorsDir.listFiles(File::isDirectory);
    if (armorDirs == null) {
      plugin.getFLogger().warning("No armor directories found in: " + armorsDir.getAbsolutePath());
      return;
    }

    for (File armorDir : armorDirs) {
      File configFile = new File(armorDir, "config.yml");
      if (!configFile.exists()) {
        configFile = new File(armorDir, "config.yaml");
      }
      if (!configFile.exists()) {
        plugin.getFLogger().warning("Config file not found for armor: " + armorDir.getName());
        plugin.getFLogger().warning("Skipping armor: " + armorDir.getName());
        continue;
      }

      FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
      processFormatLayer(armorDir, config, 1);
      processFormatLayer(armorDir, config, 2);
    }
  }

  private void processFormatLayer(File armorDir, FileConfiguration config, int layerNum) {
    String layerKey = "vanilla_layer_" + layerNum;
    String layerFileName = config.getString(layerKey);
    if (layerFileName == null) {
      plugin.getFLogger().warning("Layer " + layerNum + " not specified in config for armor: " + armorDir.getName());
      return;
    }

    File layerFile = new File(armorDir, layerFileName);
    if (!layerFile.exists()) {
      plugin.getFLogger().warning("Layer file not found: " + layerFile.getAbsolutePath());
      return;
    }

    Color internalcolor = new Color(
        config.getInt("internal-color.r", 0),
        config.getInt("internal-color.g", 0),
        config.getInt("internal-color.b", 0));
    Color color = new Color(
        config.getInt("color.r", 0),
        config.getInt("color.g", 0),
        config.getInt("color.b", 0));

    try {
      BufferedImage layerImage = ImageIO.read(layerFile);
      layerImage.setRGB(63, 31, internalcolor.getRGB());
      if (layerNum == 1) {
        layerImage.setRGB(63, 30, Color.BLACK.getRGB());
        layer1Colors.put(color, color);
        layer1Crops.put(color, layerImage);
      } else {
        layerImage.setRGB(63, 30, Color.WHITE.getRGB());
        layer2Colors.put(color, color);
        layer2Crops.put(color, layerImage);
      }
    } catch (IOException e) {
      plugin.getFLogger().severe("Failed to process format layer: " + layerFile.getPath());
      e.printStackTrace();
    }
  }

  private void processLayer(File image, int layer) {
    try {
      BufferedImage img = ImageIO.read(image);
      for (int y = 0; y < img.getHeight(); y += CROP_HEIGHT) {
        for (int x = 0; x < img.getWidth(); x += CROP_WIDTH) {
          BufferedImage crop = img.getSubimage(x, y, CROP_WIDTH, CROP_HEIGHT);
          Color color = new Color(crop.getRGB(0, 0), true);
          if (color.getAlpha() == 0 || (color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0)) {
            continue;
          }
          if (layer == 1) {
            layer1Colors.put(color, color);
            layer1Crops.put(color, crop);
          } else {
            layer2Colors.put(color, color);
            layer2Crops.put(color, crop);
          }
        }
      }
    } catch (IOException e) {
      plugin.getFLogger().severe("Failed to process layer: " + image.getAbsolutePath());
      plugin.getFLogger().dangerous(e.getMessage());
    }
  }

  private void processArmor(FileConfiguration config, String armorName) throws IOException {
    ArmorProcessor processor = new ArmorProcessor(plugin, config, armorName, usedVariables);
    processor.process();
  }

  private void generateArmorCordsFile() {
    Map<Color, ArmorColor> sortedColors = createSortedColorMap();
    List<Point> layout = createLayout(sortedColors.size());

    if (layout.isEmpty()) {
      plugin.getFLogger().warning("No colors found. Creating blank armorcords.glsl");
      createBlankArmorCordsFile();
      return;
    }

    BufferedImage outputLayer1 = createOutputImage(sortedColors.values().stream()
        .map(ac -> ac.layer1 != null ? ac.layer1 : Color.BLACK)
        .toList(), layout, 0);
    BufferedImage outputLayer2 = createOutputImage(sortedColors.values().stream()
        .map(ac -> ac.layer2 != null ? ac.layer2 : Color.BLACK)
        .toList(), layout, 1);

    saveOutputImage(outputLayer1, "assets/minecraft/textures/models/armor/leather_layer_1.png");
    saveOutputImage(outputLayer2, "assets/minecraft/textures/models/armor/leather_layer_2.png");

    saveOutputImage(outputLayer1, "assets/minecraft/textures/entity/equipment/humanoid/leather.png");
    saveOutputImage(outputLayer2, "assets/minecraft/textures/entity/equipment/humanoid_leggings/leather.png");

    String glslContent = generateGlsl(sortedColors, layout);
    saveArmorCordsFile(glslContent);

  }

  private Map<Color, ArmorColor> createSortedColorMap() {
    Map<Color, ArmorColor> sortedColors = new LinkedHashMap<>();

    // Prioritize white color
    Color white = new Color(255, 255, 255);
    if (layer1Colors.containsKey(white) || layer2Colors.containsKey(white)) {
      sortedColors.put(white, new ArmorColor(layer1Colors.get(white), layer2Colors.get(white)));
    }

    // Colors present in both layers
    Set<Color> allColors = new HashSet<>(layer1Colors.keySet());
    allColors.addAll(layer2Colors.keySet());
    allColors.stream()
        .filter(color -> !color.equals(white))
        .sorted(Comparator.comparingInt(Color::getRGB))
        .forEach(color -> sortedColors.put(color, new ArmorColor(layer1Colors.get(color), layer2Colors.get(color))));

    return sortedColors;
  }

  private List<Point> createLayout(int numImages) {
    if (numImages == 0)
      return Collections.emptyList();
    int cols = (int) Math.max(1, Math.ceil(Math.sqrt(numImages)));
    int rows = (int) Math.ceil((double) numImages / cols);
    List<Point> layout = new ArrayList<>();
    for (int i = 0; i < numImages; i++) {
      layout.add(new Point(i % cols, i / cols));
    }
    return layout;
  }

  private BufferedImage createOutputImage(List<Color> colors, List<Point> layout, int layer) {
    int cols = layout.stream().mapToInt(p -> p.x).max().orElse(0) + 1;
    int rows = layout.stream().mapToInt(p -> p.y).max().orElse(0) + 1;
    BufferedImage outputImage = new BufferedImage(cols * CROP_WIDTH, rows * CROP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = outputImage.createGraphics();

    for (int i = 0; i < colors.size(); i++) {
      Color color = colors.get(i);
      Point pos = layout.get(i);
      BufferedImage crop = (layer == 0 ? layer1Crops : layer2Crops).get(color);
      if (crop != null) {
        g2d.drawImage(crop, pos.x * CROP_WIDTH, pos.y * CROP_HEIGHT, null);
      }
    }

    g2d.dispose();
    return outputImage;
  }

  private void saveOutputImage(BufferedImage image, String fileName) {
    try {
      File toWrite = plugin.getDataFolder().toPath().resolve(OUTPUT_FOLDER).resolve(fileName).toFile();
      toWrite.getParentFile().mkdirs();

      ImageIO.write(image, "PNG", toWrite);
    } catch (IOException e) {
      plugin.getFLogger().severe("Failed to save output image: " + fileName);
      e.printStackTrace();
    }
  }

  private String generateGlsl(Map<Color, ArmorColor> sortedColors, List<Point> layout) {
    StringBuilder glslContent = new StringBuilder();
    int i = 0;
    for (Map.Entry<Color, ArmorColor> entry : sortedColors.entrySet()) {
      Color color = entry.getKey();
      Point pos = layout.get(i++);
      glslContent
          .append(String.format("COLOR_ARMOR(%d, %d, %d) {\n", color.getRed(), color.getGreen(), color.getBlue()));
      glslContent.append(String.format("    cords = vec2(%d, %d);\n", pos.x, pos.y));
      glslContent.append("}\n");
    }
    return glslContent.toString();
  }

  private void saveArmorCordsFile(String content) {
    File armorCordsFile = plugin.getDataFolder().toPath().resolve(OUTPUT_FOLDER)
        .resolve("assets/minecraft/shaders/include/armorcords.glsl")
        .toFile();
    try (java.io.FileWriter writer = new java.io.FileWriter(armorCordsFile)) {
      writer.write(content);
    } catch (IOException e) {
      plugin.getFLogger().severe("Failed to save armorcords.glsl: " + e.getMessage());
    }
  }

  private void createBlankArmorCordsFile() {
    saveArmorCordsFile("// No colors found\n");
  }

  private static class ArmorColor {
    Color layer1;
    Color layer2;

    ArmorColor(Color layer1, Color layer2) {
      this.layer1 = layer1;
      this.layer2 = layer2;
    }
  }
}
