package dev.ronovamod.cosmetics.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ronovamod.cosmetics.CosmeticsConfig;
import dev.ronovamod.cosmetics.sky.SkyPainter;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ersetzt die normale Himmelsfarbe (Overworld) durch das eigene Bild.
 * Sonne, Mond und Sterne werden danach wie gewohnt darübergezeichnet - außer man blendet sie aus.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Shadow
    @Final
    private RenderTarget renderTarget;

    @Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
    private void ronova$replaceSky(int skyColor, CallbackInfo ci) {
        if (!SkyPainter.shouldPaint() || renderTarget == null) return;
        ci.cancel();
        SkyPainter.paint(renderTarget.getColorTextureView(), renderTarget.getDepthTextureView());
    }

    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true)
    private void ronova$hideSun(float brightness, PoseStack poseStack, CallbackInfo ci) {
        if (hideCelestials()) ci.cancel();
    }

    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
    private void ronova$hideMoon(MoonPhase phase, float brightness, PoseStack poseStack, CallbackInfo ci) {
        if (hideCelestials()) ci.cancel();
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    private void ronova$hideStars(float brightness, PoseStack poseStack, CallbackInfo ci) {
        if (hideCelestials()) ci.cancel();
    }

    @Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
    private void ronova$hideSunrise(PoseStack poseStack, float sunAngle, int color, CallbackInfo ci) {
        if (hideCelestials()) ci.cancel();
    }

    private static boolean hideCelestials() {
        return SkyPainter.shouldPaint() && CosmeticsConfig.get().hideSunMoon;
    }
}
