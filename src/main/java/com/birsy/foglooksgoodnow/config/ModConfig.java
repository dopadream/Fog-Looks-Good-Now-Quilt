package com.birsy.foglooksgoodnow.config;

import com.birsy.foglooksgoodnow.client.FogManager;
import com.ibm.icu.impl.Pair;
import net.minecraft.data.BuiltinRegistries;
import org.quiltmc.config.api.WrappedConfig;
import org.quiltmc.config.api.annotations.Comment;
import org.quiltmc.config.api.annotations.FloatRange;
import org.quiltmc.config.api.annotations.IntegerRange;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.config.api.values.ValueMap;

import java.util.ArrayList;
import java.util.List;

public class ModConfig extends WrappedConfig {


	@Comment("Defines the global default fog start value")
	@FloatRange(min = 0, max = 100)
	public final float defaultFogStart = 0;

	@Comment("Defines the global default fog density value. " +
			"At 1.0, the fog end is at render distance. " +
			"At 0, there is no fog")
	@FloatRange(min = 0, max = 100)
	public final float defaultFogDensity = 1;


	@Comment("Defines a specific fog start and density per biome.")
	@Comment("DO NOT ADD ANY ENTRIES HERE MANUALLY. YOUR GAME WILL CRASH.")
	@Comment("All biomes should exist in this list already.")
	public final ValueMap<ValueList<Float>> biomeFogs = ValueMap.builder(ValueList.create(0.0F))
			.build();

	@Comment("Defines if fog settings are overwritten per biome.")
	@Comment("DO NOT ADD ANY ENTRIES HERE MANUALLY. YOUR GAME WILL CRASH.")
	@Comment("All biomes should exist in this list already.")
	public final ValueMap<ValueList<Boolean>> biomeOverrides = ValueMap.builder(ValueList.create(false))
			.build();

//	@Comment("Defines if fog color settings are overwritten per biome.")
//	@Comment("DO NOT ADD ANY ENTRIES HERE MANUALLY. YOUR GAME WILL CRASH.")
//	@Comment("All biomes should exist in this list already.")
//	public final ValueMap<ValueList<Boolean>> colorOverrides = ValueMap.builder(ValueList.create(false))
//			.build();
//
//	@Comment("Defines the fog color values per biome.")
//	@Comment("DO NOT ADD ANY ENTRIES HERE MANUALLY. YOUR GAME WILL CRASH.")
//	@Comment("All biomes should exist in this list already.")
//	public final ValueMap<ValueList<Integer>> fogColors = ValueMap.builder(ValueList.create(0))
//			.build();

	@Comment("Defines if fog will darken and get more dense when underground.")
	public final boolean useCaveFog = true;

	@Comment("Defines the density of fog in caves. " +
			"If cave fog is active, this will be multiplied with the current fog density.")
	public final float caveFogDensity = 1;

	@Comment("Defines the color of cave fog, in the decimal color format. " +
			"If cave fog is active, this will be multiplied with the current fog color.")
	@IntegerRange(min = 0, max = 16777215)
	public final int caveFogColor = 0;




	public static List<Pair<String, FogManager.BiomeFogDensity>> getDensityConfigs() {
		ArrayList<Pair<String, FogManager.BiomeFogDensity>> entries = new ArrayList<>();
		BuiltinRegistries.BIOME.stream().forEach(biome ->
				entries.add(Pair.of(BuiltinRegistries.BIOME.getKey(biome).toString(),
						new FogManager.BiomeFogDensity(ModConfigManager.CONFIG.biomeFogs.get(BuiltinRegistries.BIOME.getKey(biome).toString()).get(0), ModConfigManager.CONFIG.biomeFogs.get(BuiltinRegistries.BIOME.getKey(biome).toString()).get(1)))));
		return entries;
	}
}
