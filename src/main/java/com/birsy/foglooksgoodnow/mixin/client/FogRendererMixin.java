package com.birsy.foglooksgoodnow.mixin.client;


import com.birsy.foglooksgoodnow.client.FogManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Shadow
	private static float fogRed;

	@Shadow
	private static float fogGreen;

	@Shadow
	private static float fogBlue;



	@Inject(method = "setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V", at = @At("TAIL"), cancellable = false)
	private static void setupColor(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
		if (camera.getFluidInCamera() == FogType.NONE) {

			FogManager densityManager = FogManager.getDensityManager();

			float undergroundFogMultiplier = 1.0F;
			if (FogManager.shouldRenderCaveFog()) {
				undergroundFogMultiplier = Mth.lerp(densityManager.getUndergroundFactor(tickDelta), 2.2F, 1.0F);
			}
			float darkness = densityManager.darkness.get(tickDelta);
			undergroundFogMultiplier = Mth.lerp(darkness, 1.0F, undergroundFogMultiplier);

			RenderSystem.setShaderFogStart(viewDistance * densityManager.fogStart.get(tickDelta));
			RenderSystem.setShaderFogEnd(viewDistance / (densityManager.fogDensity.get(tickDelta) * undergroundFogMultiplier));
			RenderSystem.setShaderFogShape(FogShape.SPHERE);
		}
	}

	@Inject(method = "setupColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)V", at = @At("TAIL"), cancellable = false)
	private static void setupColor(Camera camera, float tickDelta, ClientLevel world, int viewDistance, float skyDarkness, CallbackInfo ci) {
		if (camera.getFluidInCamera() == FogType.NONE && FogManager.shouldRenderCaveFog()) {
			FogManager densityManager = FogManager.getDensityManager();

			Vec3 fogColor = FogManager.getCaveFogColor(world, camera);

			float undergroundFactor = 1 - densityManager.getUndergroundFactor(tickDelta);
			fogRed = ((float) Mth.lerp(undergroundFactor, fogRed, fogColor.x));
			fogGreen = ((float) Mth.lerp(undergroundFactor, fogGreen, fogColor.y));
			fogBlue = ((float) Mth.lerp(undergroundFactor, fogBlue, fogColor.z));
		}
//		if (camera.getFluidInCamera() == FogType.NONE && FogManager.shouldRenderBiomeFog()) {
//			FogManager densityManager = FogManager.getDensityManager();
//
//			Vec3 fogColor = FogManager.getBiomeFogColor(world, camera);
//
//			float undergroundFactor = 1 + densityManager.getUndergroundFactor(tickDelta);
//			fogRed = ((float) Mth.lerp(undergroundFactor, fogRed, fogColor.x));
//			fogGreen = ((float) Mth.lerp(undergroundFactor, fogGreen, fogColor.y));
//			fogBlue = ((float) Mth.lerp(undergroundFactor, fogBlue, fogColor.z));
//		}
	}
}
