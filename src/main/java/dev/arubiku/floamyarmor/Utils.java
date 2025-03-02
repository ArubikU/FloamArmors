package dev.arubiku.floamyarmor;

import java.util.Base64;

public class Utils {
  public static String toBase64(String input) {
    return Base64.getEncoder().encodeToString(input.getBytes());
  }
  public static String fromBase64(String input) {
    return new String(Base64.getDecoder().decode(input));
  }
}
