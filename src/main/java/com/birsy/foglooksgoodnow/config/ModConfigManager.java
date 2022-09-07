package com.birsy.foglooksgoodnow.config;

import it.unimi.dsi.fastutil.Pair;
import org.quiltmc.config.api.values.TrackedValue;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;
import org.quiltmc.loader.api.config.QuiltConfig;

import java.util.List;

public class ModConfigManager {
	public static final ModConfig CONFIG = QuiltConfig.create("foglooksgoodnow", "config", ModConfig.class);

	public static final TrackedValue<Float> DEFAULT_FOG_START = (TrackedValue<Float>) CONFIG.getValue(List.of("defaultFogStart"));
	public static final TrackedValue<Float> DEFAULT_FOG_DENSITY = (TrackedValue<Float>) CONFIG.getValue(List.of("defaultFogDensity"));
	public static final TrackedValue<ValueMap<ValueList<Float>>> BIOME_FOGS = (TrackedValue<ValueMap<ValueList<Float>>>) CONFIG.getValue(List.of("biomeFogs"));
	public static final TrackedValue<Boolean> USE_CAVE_FOG = (TrackedValue<Boolean>) CONFIG.getValue(List.of("useCaveFog"));
	public static final TrackedValue<Integer> CAVE_FOG_COLOR = (TrackedValue<Integer>) CONFIG.getValue(List.of("caveFogColor"));
	public static final TrackedValue<ValueMap<ValueList<Boolean>>> OVERRIDE_FOG_SETTINGS = (TrackedValue<ValueMap<ValueList<Boolean>>>) CONFIG.getValue(List.of("biomeOverrides"));
//	public static final TrackedValue<ValueMap<ValueList<Boolean>>> OVERRIDE_FOG_COLORS = (TrackedValue<ValueMap<ValueList<Boolean>>>) CONFIG.getValue(List.of("colorOverrides"));
//	public static final TrackedValue<ValueMap<Integer>> BIOME_FOG_COLORS = (TrackedValue<ValueMap<Integer>>) CONFIG.getValue(List.of("fogColors"));
	public static final TrackedValue<Float> CAVE_FOG_DENSITY = (TrackedValue<Float>) CONFIG.getValue(List.of("caveFogDensity"));
}
