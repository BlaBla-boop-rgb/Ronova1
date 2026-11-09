package dev.ronovamod.cosmetics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Alle Schalter der Mod. Wird als config/ronova/cosmetics.json gespeichert. */
public final class CosmeticsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean halo = true;
    public boolean soulAura = true;
    public boolean crowSwarm = false;
    public boolean deathTrail = false;

    public boolean customSky = true;
    public boolean hideSunMoon = false;
    public boolean rotateSky = true;
    /** Wie oft das Bild einmal rund um den Horizont passt (1-4). */
    public int skyRepeat = 2;
    /** Helligkeit des Himmelsbilds in Prozent (40, 70, 100). */
    public int skyBrightness = 100;

    private static CosmeticsConfig instance = new CosmeticsConfig();

    public static CosmeticsConfig get() {
        return instance;
    }

    public static Path folder() {
        return FabricLoader.getInstance().getConfigDir().resolve("ronova");
    }

    public static Path skyImagePath() {
        return folder().resolve("sky.png");
    }

    public static void load() {
        Path file = folder().resolve("cosmetics.json");
        try {
            Files.createDirectories(folder());
            Path readme = folder().resolve("LIES_MICH.txt");
            if (!Files.exists(readme)) {
                Files.writeString(readme, """
                        Lege hier ein Bild mit dem Namen sky.png ab (PNG, am besten breit, z. B. 2048x1024).
                        Es wird rund um den Horizont als Himmel aufgespannt.
                        In Minecraft: /ronona -> "Bild neu laden".
                        """, StandardCharsets.UTF_8);
            }
            if (Files.exists(file)) {
                CosmeticsConfig loaded = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), CosmeticsConfig.class);
                if (loaded != null) instance = loaded;
            }
        } catch (Exception e) {
            CosmeticsClient.LOGGER.warn("Konnte Ronova-Config nicht laden, nutze Standardwerte", e);
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(folder());
            Files.writeString(folder().resolve("cosmetics.json"), GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            CosmeticsClient.LOGGER.warn("Konnte Ronova-Config nicht speichern", e);
        }
    }
}
