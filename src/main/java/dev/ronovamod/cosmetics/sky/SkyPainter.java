package dev.ronovamod.cosmetics.sky;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ronovamod.cosmetics.CosmeticsClient;
import dev.ronovamod.cosmetics.CosmeticsConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Zeichnet das Himmelsbild als ein einziges bildschirmfüllendes Dreieck.
 * Der Shader rechnet für jeden Pixel die Blickrichtung aus und liest daraus die Bildposition.
 *
 * Die Einstellungen (Wiederholung, Helligkeit, Drehung) laufen über ColorModulator,
 * so braucht es keinen eigenen Uniform-Block.
 */
public final class SkyPainter {
    private static RenderPipeline pipeline;
    private static GpuBuffer triangle;
    private static boolean failed = false;

    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector3f ZERO = new Vector3f();

    private SkyPainter() {}

    public static boolean shouldPaint() {
        return !failed && CosmeticsConfig.get().customSky && SkyImage.ready();
    }

    public static void resetFailure() {
        failed = false;
    }

    private static RenderPipeline pipeline() {
        if (pipeline == null) {
            RenderPipeline.Builder b = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET);
            b.withLocation(CosmeticsClient.id("pipeline/ronova_sky"));
            b.withVertexShader(CosmeticsClient.id("core/ronova_sky"));
            b.withFragmentShader(CosmeticsClient.id("core/ronova_sky"));
            b.withCull(false);
            b.withBindGroupLayout(BindGroupLayout.builder().withSampler("Sampler0").build());
            b.withVertexBinding(0, DefaultVertexFormat.POSITION);
            b.withPrimitiveTopology(PrimitiveTopology.TRIANGLES);
            b.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT));
            b.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false));
            pipeline = b.build();
        }
        return pipeline;
    }

    private static GpuBuffer triangle(GpuDevice device) {
        if (triangle == null) {
            ByteBuffer data = ByteBuffer.allocateDirect(3 * 3 * Float.BYTES).order(ByteOrder.nativeOrder());
            data.putFloat(-1f).putFloat(-1f).putFloat(0f);
            data.putFloat(3f).putFloat(-1f).putFloat(0f);
            data.putFloat(-1f).putFloat(3f).putFloat(0f);
            data.flip();
            triangle = device.createBuffer(() -> "Ronova sky triangle", GpuBuffer.USAGE_VERTEX, data);
        }
        return triangle;
    }

    public static void paint(GpuTextureView color, GpuTextureView depth) {
        if (color == null) return;
        try {
            CosmeticsConfig cfg = CosmeticsConfig.get();
            Minecraft mc = Minecraft.getInstance();

            // nur die Kamera-Drehung - so wackelt der Himmel beim Laufen nicht mit
            Matrix4f view = new Matrix4f();
            Camera camera = mc.gameRenderer.mainCamera();
            if (camera != null) {
                view.rotation(camera.rotation().conjugate(new Quaternionf()));
            }

            float spin = cfg.rotateSky ? (float) ((System.currentTimeMillis() % 600_000L) / 600_000.0) : 0f;
            Vector4f settings = new Vector4f(
                    Math.max(1, Math.min(4, cfg.skyRepeat)),
                    cfg.skyBrightness / 100f,
                    spin,
                    1f);

            GpuDevice device = RenderSystem.getDevice();
            CommandEncoder encoder = device.createCommandEncoder();
            var transforms = RenderSystem.getDynamicUniforms().writeTransform(view, settings, ZERO, IDENTITY);

            try (RenderPass pass = encoder.createRenderPass(() -> "Ronova sky", color,
                    Optional.<Vector4fc>empty(), depth, OptionalDouble.empty())) {
                pass.setPipeline(pipeline());
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transforms);
                pass.bindTexture("Sampler0", SkyImage.view(), SkyImage.sampler());
                pass.setVertexBuffer(0, triangle(device).slice());
                pass.draw(3, 1, 0, 0);
            }
        } catch (Exception e) {
            failed = true;
            CosmeticsClient.LOGGER.error("Ronova-Himmel konnte nicht gezeichnet werden und ist für diese Sitzung aus", e);
        }
    }
}
