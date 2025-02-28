package dev.arubiku.floamyarmor;

public class VanillaOverrides {

  public static String getArmorTexturePath(String armorType, String layer, boolean post1_21_3) {
    String basePath;
    if (post1_21_3) {
      basePath = layer.equals("layer_2") ? "textures/entity/equipment/humanoid_leggings/"
          : "textures/entity/equipment/humanoid/";
    } else {
      basePath = "textures/models/armor/";
    }
    String fileName = post1_21_3 ? armorType + ".png"
        : armorType + (layer.equals("layer_1") ? "" : "_layer_2") + ".png";
    return basePath + fileName;
  }

  public static ArmorOverrides getOverrides(String armorType) {
    switch (armorType.toLowerCase()) {
      case "netherite":
        return new ArmorOverrides("NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS");
      case "diamond":
        return new ArmorOverrides("DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS");
      case "gold":
        return new ArmorOverrides("GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS");
      case "iron":
        return new ArmorOverrides("IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS");
      case "chainmail":
        return new ArmorOverrides("CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS");
      case "leather":
        return new ArmorOverrides("LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS");
      default:
        throw new IllegalArgumentException("Unsupported armor type: " + armorType);
    }
  }

  public static class ArmorOverrides {
    private final String helmetSlot;
    private final String chestplateSlot;
    private final String leggingsSlot;
    private final String bootsSlot;

    public ArmorOverrides(String helmetSlot, String chestplateSlot, String leggingsSlot, String bootsSlot) {
      this.helmetSlot = helmetSlot;
      this.chestplateSlot = chestplateSlot;
      this.leggingsSlot = leggingsSlot;
      this.bootsSlot = bootsSlot;
    }

    public String getSlot(int slot) {
      switch (slot) {
        case 103:
          return helmetSlot;
        case 102:
          return chestplateSlot;
        case 101:
          return leggingsSlot;
        case 100:
          return bootsSlot;
        default:
          throw new IllegalArgumentException("Unsupported slot: " + slot);
      }
    }
  }
}
