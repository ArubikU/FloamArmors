import math

# Definimos los valores disponibles de rotación en múltiplos de PI para cada eje
X_MULTIPLIERS = {
    0.5: "PIX",
    0.25: "PIX25",
    0.125: "PIX12",
    0.0625: "PIX62",
    1: "PIX2",
    1.5: "PIX3",
    -0.5: "NIX",
    -0.25: "NIX25",
    -0.125: "NIX12",
    -0.0625: "NIX62",
    -1: "NIX1",
    -1.5: "NIX3",
}

Y_MULTIPLIERS = {
    0.5: "PIY",
    0.25: "PIY25",
    0.125: "PIY12",
    0.0625: "PIY62",
    1: "PIY2",
    1.5: "PIY3",
    -0.5: "NIY",
    -0.25: "NIY25",
    -0.125: "NIY12",
    -0.0625: "NIY62",
    -1: "NIY1",
    -1.5: "NIY3",
}

Z_MULTIPLIERS = {
    0.5: "PIZ",
    0.25: "PIZ25",
    0.125: "PIZ12",
    0.0625: "PIZ62",
    1: "PIZ2",
    1.5: "PIZ3",
    -0.5: "NIZ",
    -0.25: "NIZ25",
    -0.125: "NIZ12",
    -0.0625: "NIZ62",
    -1: "NIZ1",
    -1.5: "NIZ3",
}

AXES_MULTIPLIERS = [X_MULTIPLIERS, Y_MULTIPLIERS, Z_MULTIPLIERS]
AXES_LABELS = ["X", "Y", "Z"]

# Tolerancia para aproximar
TOLERANCE = 0.05

def closest_macro(angle_rad, multipliers):
    """Encuentra el macro más cercano para un ángulo dado en radianes usando los multiplicadores del eje."""
    for multiplier, macro in multipliers.items():
        if abs(angle_rad - math.pi * multiplier) < TOLERANCE:
            return macro
    return None

def normalize_angle(deg):
    """Normaliza un ángulo en grados para que esté en el rango [-180, 180]."""
    if(deg >180):
      return deg-360
    return deg


def format_angle_as_pi(angle_rad):
    """Formatea un ángulo en radianes como un múltiplo de PI."""
    factor = angle_rad / math.pi
    return f"PI*{round(factor,4)}"

def convert_rotation(deg_x, deg_y, deg_z):
    """Convierte una rotación dada en grados (deg_x, deg_y, deg_z) a una secuencia de macros de rotación."""
    # Normalizar ángulos y convertir a radianes
    angles_rad = [math.radians(normalize_angle(deg)) for deg in [deg_x, deg_y, deg_z]]
    result = []

    for angle_rad, multipliers, axis in zip(angles_rad, AXES_MULTIPLIERS, AXES_LABELS):
        macro = closest_macro(angle_rad, multipliers)
        if macro:
            result.append(macro)
        else:
            result.append(f"Rotate3({format_angle_as_pi(angle_rad)}, {axis})")

    return "*".join(result) if result else "No matching rotation macros found"

if __name__ == "__main__":
    # Ejemplo de uso
    deg_x = 0
    deg_y = 22.5
    deg_z = 0

    macros = convert_rotation(deg_x, deg_y, deg_z)
    print(macros)
