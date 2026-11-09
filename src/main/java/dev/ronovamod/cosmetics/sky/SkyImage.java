package dev.ronovamod.cosmetics.sky;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.ronovamod.cosmetics.CosmeticsClient;
import dev.ronovamod.cosmetics.CosmeticsConfig;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Lädt config/ronova/sky.png auf die Grafikkarte. */
public final class SkyImage {
    private static GpuTexture texture;
    private static GpuTextureView view;
    private static GpuSampler sampler;
    private static boolean triedLoading = false;
    private static String status = "Noch nicht geladen";

    private SkyImage() {}

    public static boolean ready() {
        if (!triedLoading) {
            triedLoading = true;
            reload();
        }
        return view != null;
    }

    public static GpuTextureView view() { return view; }
    public static GpuSampler sampler() { return sampler; }
    public static String status() { return status; }

    /** Muss auf dem Render-Thread laufen (GUI-Klick / Render-Pass sind das). */
    public static String reload() {
        triedLoading = true;
        close();
        Path path = CosmeticsConfig.skyImagePath();
        if (!Files.exists(path)) {
            status = "Kein Bild gefunden: config/ronova/sky.png";
            return status;
        }
        try (InputStream in = Files.newInputStream(path); NativeImage image = NativeImage.read(in)) {
            var device = RenderSystem.getDevice();
            texture = device.createTexture("Ronova sky image",
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                    GpuFormat.RGBA8_UNORM, image.getWidth(), image.getHeight(), 1, 1);
            device.createCommandEncoder().writeToTexture(texture, image);
            view = device.createTextureView(texture);
            // horizontal wiederholen (rund um den Horizont), vertikal am Rand festhalten
            sampler = RenderSystem.getSamplerCache().getSampler(
                    AddressMode.REPEAT, AddressMode.CLAMP_TO_EDGE,
                    FilterMode.LINEAR, FilterMode.LINEAR, false);
            status = "Geladen: " + image.getWidth() + "x" + image.getHeight();
        } catch (Exception e) {
            close();
            status = "Fehler beim Laden (nur PNG!): " + e.getMessage();
            CosmeticsClient.LOGGER.error("Himmelsbild konnte nicht geladen werden", e);
        }
        return status;
    }

    private static void close() {
        if (view != null) view.close();
        if (texture != null) texture.close();
        view = null;
        texture = null;
    }
}
