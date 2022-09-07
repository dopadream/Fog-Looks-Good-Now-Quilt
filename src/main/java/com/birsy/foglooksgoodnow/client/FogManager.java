package com.birsy.foglooksgoodnow.client;

import com.birsy.foglooksgoodnow.FogLooksGoodNowMod;
import com.birsy.foglooksgoodnow.config.ModConfig;
import com.birsy.foglooksgoodnow.config.ModConfigManager;
import com.birsy.foglooksgoodnow.config.ModConfigScreen;
import com.birsy.foglooksgoodnow.util.MathUtils;
import com.ibm.icu.impl.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.*;

public class FogManager {
    @Nullable
    public static FogManager densityManager;

	public static FogManager getDensityManager() {
        return Objects.requireNonNull(densityManager, "Attempted to call getDensityManager before it finished loading!");
    }
    public static Optional<FogManager> getDensityManagerOptional() {
        return Optional.ofNullable(densityManager);
    }

	private final Minecraft mc;
	public InterpolatedValue fogStart;
	public InterpolatedValue fogDensity;
	public InterpolatedValue caveFogDensity;
	public InterpolatedValue currentSkyLight;
	public InterpolatedValue currentBlockLight;
	public InterpolatedValue currentLight;
	public InterpolatedValue undergroundness;
	public InterpolatedValue darkness;


    public FogManager() {
        this.mc = Minecraft.getInstance();
        this.fogStart = new InterpolatedValue(0.0F);
        this.fogDensity = new InterpolatedValue(1.0F);
		this.caveFogDensity = new InterpolatedValue(1.0F);

        this.currentSkyLight = new InterpolatedValue(16.0F);
        this.currentBlockLight = new InterpolatedValue(16.0F);
        this.currentLight = new InterpolatedValue(16.0F);
        this.undergroundness = new InterpolatedValue(0.0F, 0.02f);
        this.darkness = new InterpolatedValue(0.0F, 0.1f);

        if (QuiltLoader.isModLoaded("cloth_config")) {
            initializeConfig();
        }
    }


	private static List<Pair<String, FogManager.BiomeFogDensity>> biomes() {
		ArrayList<Pair<String, FogManager.BiomeFogDensity>> entries = new ArrayList<>();
		List<Pair<String, FogManager.BiomeFogDensity>> densityConfigs = ModConfig.getDensityConfigs();
		for (Pair<String, BiomeFogDensity> densityConfig : densityConfigs) {
			if (ModConfigManager.CONFIG.biomeOverrides.get(densityConfig.first).get(0)) {
				entries.add(densityConfig);
			}
		}
		return entries;
	}

	public void initializeConfig() {
		FogLooksGoodNowMod.LOGGER.info("Initialized Config Values");
		this.fogStart.setDefaultValue((float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_START.key()).value());
		this.fogDensity.setDefaultValue((float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_DENSITY.key()).value());
		this.caveFogDensity.setDefaultValue((float) ModConfigManager.CONFIG.getValue(ModConfigManager.CAVE_FOG_DENSITY.key()).value());

	}

    public void tick() {
        BlockPos pos = this.mc.gameRenderer.getMainCamera().getBlockPosition();
		Biome biome = this.mc.level.getBiome(pos).value();
		String key = this.mc.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString();
        if (key == null) {
			return;
		}
		String entry = null;
		BiomeFogDensity currentDensity = null;

		List<Pair<String, FogManager.BiomeFogDensity>> biomes = biomes();

		for (Pair<String, BiomeFogDensity> curBiome : biomes) {
			if (Objects.equals(curBiome.first, key)) {
				entry = key;
				currentDensity = new BiomeFogDensity(ModConfigManager.BIOME_FOGS.value().get(entry).get(0), ModConfigManager.BIOME_FOGS.value().get(entry).get(1));
			}
			else entry = null;
		}


        boolean isFogDense = this.mc.level.effects().isFoggyAt(pos.getX(), pos.getZ()) || this.mc.gui.getBossOverlay().shouldCreateWorldFog();
        float density = isFogDense? 2.0F : 1.0F;

		if (mc.level.getBrightness(LightLayer.SKY, pos) <= 1 || pos.getY() < mc.level.getSeaLevel() - 32.0F) {
			float caveFog = ModConfigManager.CAVE_FOG_DENSITY.value();
			density = isFogDense? 2.0F * caveFog : 1.0F + caveFog;
		}

        float[] darknessAffectedFog;

        if (currentDensity != null && entry != null) {
			darknessAffectedFog = getDarknessEffectedFog(currentDensity.fogStart(), currentDensity.fogDensity() * density);
        } else {
            darknessAffectedFog = getDarknessEffectedFog((float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_START.key()).value(), (float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_DENSITY.key()).value() * density);
        }
        this.darkness.interpolate(darknessAffectedFog[2]);
        this.fogStart.interpolate(darknessAffectedFog[0]);
        this.fogDensity.interpolate(darknessAffectedFog[1]);

        this.currentSkyLight.interpolate(mc.level.getBrightness(LightLayer.SKY, pos));
        this.currentBlockLight.interpolate(mc.level.getBrightness(LightLayer.BLOCK, pos));
        this.currentLight.interpolate(mc.level.getRawBrightness(pos, 0));
        if (mc.level.getBrightness(LightLayer.SKY, pos) >= 1 || pos.getY() < mc.level.getSeaLevel() - 32.0F) { this.undergroundness.interpolate(0.0F, 0.05f); } else { this.undergroundness.interpolate(1.0F); }
    }

    public float getUndergroundFactor(float partialTick) {
        float y = (float) mc.cameraEntity.getY();
        float yFactor = Mth.clamp(MathUtils.mapRange(mc.level.getSeaLevel() - 32.0F, mc.level.getSeaLevel() + 1.0F, 1, 0, y), 0.0F, 1.0F);
        //FogLooksGoodNowMod.LOGGER.info("" + yFactor);
        return Mth.lerp(yFactor, 1 - this.undergroundness.get(partialTick), this.currentSkyLight.get(partialTick) / 16.0F);
    }



    public static Vec3 getCaveFogColor(ClientLevel level, Camera camera) {
        Minecraft mc = Minecraft.getInstance();

        BiomeManager biomemanager = level.getBiomeManager();
        Vec3 biomePos = camera.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
        Vec3 fogColor = CubicSampler.gaussianSampleVec3(biomePos, (x, y, z) -> Vec3.fromRGB24(biomemanager.getBiome(new BlockPos(x, y, z)).value().getFogColor()));
        fogColor = fogColor.multiply(Vec3.fromRGB24((int) ModConfigManager.CONFIG.getValue(ModConfigManager.CAVE_FOG_COLOR.key()).value()));

        float darkness = 1.0F - Mth.clamp(densityManager.darkness.get(mc.getDeltaFrameTime()), 0, 1);
        fogColor = fogColor.multiply(darkness, darkness, darkness);

        return fogColor;
    }

//	public static Vec3 getBiomeFogColor(ClientLevel level, Camera camera) {
//		Minecraft mc = Minecraft.getInstance();
//		BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
//		Biome biome = Minecraft.getInstance().level.getBiome(pos).value();
//
//		BiomeManager biomemanager = level.getBiomeManager();
//		Vec3 biomePos = camera.getPosition().subtract(2.0D, 2.0D, 2.0D).scale(0.25D);
//		Vec3 fogColor = CubicSampler.gaussianSampleVec3(biomePos, (x, y, z) -> Vec3.fromRGB24(biomemanager.getBiome(new BlockPos(x, y, z)).value().getFogColor()));
//		fogColor = fogColor.multiply(Vec3.fromRGB24(ModConfigManager.CONFIG.fogColors.get(Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString()).get(0)));
//
//		return fogColor;
//	}

    public static boolean shouldRenderCaveFog() {
        return Minecraft.getInstance().level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL && (boolean) ModConfigManager.CONFIG.getValue(ModConfigManager.USE_CAVE_FOG.key()).value();
    }

//	public static boolean shouldRenderBiomeFog() {
//		BlockPos pos = Minecraft.getInstance().gameRenderer.getMainCamera().getBlockPosition();
//		Biome biome = Minecraft.getInstance().level.getBiome(pos).value();
//		return Minecraft.getInstance().level.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL && ModConfigManager.CONFIG.colorOverrides.get(Minecraft.getInstance().level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getKey(biome).toString()).get(0);
//	}

    public static float[] getDarknessEffectedFog(float fs, float fd) {
        Minecraft mc = Minecraft.getInstance();
        float renderDistance = mc.gameRenderer.getRenderDistance();

        Entity entity = mc.cameraEntity;
        float fogStart = fs;
        float fogDensity = fd;
        float darknessValue = 0.0F;

        if (entity instanceof LivingEntity e) {
            if (e.hasEffect(MobEffects.BLINDNESS)) {
                MobEffectInstance effect = e.getEffect(MobEffects.BLINDNESS);
                float intensity = Mth.lerp(Math.min(1.0F, (float)effect.getDuration() / 20.0F), renderDistance, 5.0F);
                fogStart = (5.0F * 0.5F) / renderDistance;
                fogDensity = renderDistance / 10.0F;
                darknessValue = 1.0F;
            }else if (e.hasEffect(MobEffects.DARKNESS)) {
                MobEffectInstance effect = e.getEffect(MobEffects.DARKNESS);
                if (!effect.getFactorData().isEmpty()) {
                    float intensity = Mth.lerp(effect.getFactorData().get().getFactor(e, mc.getDeltaFrameTime()), renderDistance, 15.0F);
                    fogStart = (15.0F * 0.2F) / renderDistance;
                    fogDensity = renderDistance / 15.0F;
                    darknessValue = effect.getFactorData().get().getFactor(e, mc.getDeltaFrameTime());
                }
            }
        }

        return new float[]{fogStart, fogDensity, darknessValue};
    }

    public void close() {}

    public record BiomeFogDensity(Float fogStart, Float fogDensity) {};

    public class InterpolatedValue {
        public float defaultValue;

        private final float interpolationSpeed;
        private float previousValue;
        private float currentValue;

        public InterpolatedValue(float defaultValue, float interpolationSpeed) {
            this.defaultValue = defaultValue;
            this.currentValue = defaultValue;
            this.interpolationSpeed = interpolationSpeed;
        }

        public InterpolatedValue(float defaultValue) {
            this(defaultValue, 0.05f);
        }

        public void set(float value) {
            this.previousValue = this.currentValue;
            this.currentValue = value;
        }

        public void setDefaultValue(float value) {
            this.defaultValue = value;
        }
        public void setDefaultValue(double value) {
            this.defaultValue = (float)value;
        }

        public void interpolate(float value, float interpolationSpeed) {
            this.set(Float.isNaN(value) ? Mth.lerp(interpolationSpeed, currentValue, defaultValue) : Mth.lerp(interpolationSpeed, currentValue, value));
        }
        public void interpolate(float value) {
            this.interpolate(value, this.interpolationSpeed);
        }
        public void interpolate() {
            this.set(Mth.lerp(interpolationSpeed, currentValue, defaultValue));
        }

        public float get(float partialTick) {
            return Mth.lerp(partialTick, previousValue, currentValue);
        }
    }

}
