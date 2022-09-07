package com.birsy.foglooksgoodnow.config;

import com.birsy.foglooksgoodnow.FogLooksGoodNowMod;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.Component;

import javax.swing.text.Highlighter;
import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ModConfigScreen implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			ConfigBuilder builder = ConfigBuilder.create()
					.setTitle(Component.translatable("title.foglooksgoodnow.config"));

			ConfigEntryBuilder entryBuilder = builder.entryBuilder();
			ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.foglooksgoodnow.general"));

			ConfigCategory biomeList = builder.getOrCreateCategory(Component.translatable("category.foglooksgoodnow.category.biomeList"));
			AbstractConfigListEntry nonEditableList = createBiomesList(entryBuilder);
			biomeList.addEntry(nonEditableList);

			general.addEntry(entryBuilder.startFloatField(Component.translatable("option.foglooksgoodnow.defaultfogstart"), (float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_START.key()).value())
					.setDefaultValue(1.0F)
					.setTooltip(Component.translatable("option.foglooksgoodnow.defaultfogstart.tooltip"))
					.setMin(0)
					.setMax(100)
					.setSaveConsumer(newValue -> ModConfigManager.DEFAULT_FOG_START.setValue(newValue, true)) // Recommended: Called when user save the config
					.build());

			general.addEntry(entryBuilder.startFloatField(Component.translatable("option.foglooksgoodnow.defaultfogdensity"), (float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_DENSITY.key()).value())
					.setDefaultValue(1.0F)
					.setTooltip(Component.translatable("option.foglooksgoodnow.defaultfogdensity.tooltip"))
					.setMin(0)
					.setMax(100)
					.setSaveConsumer(newValue -> ModConfigManager.DEFAULT_FOG_DENSITY.setValue(newValue, true)) // Recommended: Called when user save the config
					.build());
			general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("option.foglooksgoodnow.usecavefog"), (boolean) ModConfigManager.CONFIG.getValue(ModConfigManager.USE_CAVE_FOG.key()).value())
					.setDefaultValue(true)
					.setTooltip(Component.translatable("option.foglooksgoodnow.usecavefog.tooltip"))
					.setSaveConsumer(newValue -> ModConfigManager.USE_CAVE_FOG.setValue(newValue, true))
					.build());
			general.addEntry(entryBuilder.startFloatField(Component.translatable("option.foglooksgoodnow.cavefogdensity"), (float) ModConfigManager.CONFIG.getValue(ModConfigManager.CAVE_FOG_DENSITY.key()).value())
					.setDefaultValue(1.0F)
					.setTooltip(Component.translatable("option.foglooksgoodnow.cavefogdensity.tooltip"))
					.setMin(0)
					.setMax(100)
					.setSaveConsumer(newValue -> ModConfigManager.CAVE_FOG_DENSITY.setValue(newValue, true)) // Recommended: Called when user save the config
					.build());
			general.addEntry(entryBuilder.startColorField(Component.translatable("option.foglooksgoodnow.cavefogcolor"), (int) ModConfigManager.CONFIG.getValue(ModConfigManager.CAVE_FOG_COLOR.key()).value())
					.setDefaultValue(0)
					.setTooltip(Component.translatable("option.foglooksgoodnow.cavefogcolor.tooltip"))
					.setSaveConsumer(newValue -> ModConfigManager.CAVE_FOG_COLOR.setValue(newValue, true))
					.build());






			builder.setSavingRunnable(() -> {
				ModConfigManager.CONFIG.save();
				ModConfigManager.CONFIG.savePath();
			});
			return builder.setParentScreen(screen).build();
		};
	}
	private static LinkedHashMap<String, HighlightedBiome> biomeListToMap(List<HighlightedBiome> list)
	{
		LinkedHashMap<String, HighlightedBiome> map = new LinkedHashMap<>();
		//  reverses as the list needs to have the newest entries at the top because of ClothConfig, and the map needs newest at the bottom because of the list command.
		Collections.reverse(list);
		list.forEach(p -> map.put(p.name, p));
		return map;
	}

	private static List<HighlightedBiome> mapToBiomeList(LinkedHashMap<String, HighlightedBiome> map)
	{
		var list = new ArrayList<>(map.values());
		//  reverses as the list needs to have the newest entries at the top because of ClothConfig, and the map needs newest at the bottom because of the list command.
		Collections.reverse(list);
		return list;
	}


	public static List<HighlightedBiome> biomes() {
		ArrayList<HighlightedBiome> entries = new ArrayList<>();
		BuiltinRegistries.BIOME.stream().forEach(biome -> entries.add(new HighlightedBiome(BuiltinRegistries.BIOME.getKey(biome).toString(),
				(float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_START.key()).value(),
				(float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_DENSITY.key()).value())));
		return entries;
	}



	private AbstractConfigListEntry createBiomesList(ConfigEntryBuilder entryBuilder)
	{
		return new NestedListListEntry<HighlightedBiome, MultiElementListEntry<HighlightedBiome>>(
				Component.translatable("list.foglooksgoodnow.biomeconfig"),
				biomes(), // initial
				true,
				Optional::empty, //  tool tip
				list -> ModConfigManager.CONFIG.getValue(List.of("biomeFogs")),
				ModConfigScreen::biomes,
				entryBuilder.getResetButtonKey(),
				false,
				false,
				(biomeIn, nestedListListEntry) -> {
					final var biome = biomeIn == null ? new HighlightedBiome() : biomeIn;
					return new MultiElementListEntry<>(
							Component.literal(biome.name.equals("") ? "Biome" : biome.name), biome,
							Arrays.asList(
									entryBuilder.startBooleanToggle(Component.translatable("option.foglooksgoodnow.overridedefaults"), ModConfigManager.CONFIG.biomeOverrides.get(biome.name).get(0))
											.setSaveConsumer(override -> ModConfigManager.CONFIG.biomeOverrides.get(biome.name).set(0, override))
											.setTooltip(Component.translatable("option.foglooksgoodnow.overridedefaults.tooltip"))
											.setDefaultValue(false)
											.build(),
									entryBuilder.startDoubleField(Component.translatable("option.foglooksgoodnow.fogstart"), ModConfigManager.CONFIG.biomeFogs.get(biome.name).get(0))
											.setSaveConsumer(fogStart -> ModConfigManager.CONFIG.biomeFogs.get(biome.name).set(0, fogStart.floatValue()))
											.setTooltip(Component.translatable("Defines the assigned biome's fog start value"))
											.setMin(0)
											.setMax(100)
											.setDefaultValue((float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_START.key()).value())
											.build(),
									entryBuilder.startDoubleField(Component.translatable("option.foglooksgoodnow.fogdensity"), ModConfigManager.CONFIG.biomeFogs.get(biome.name).get(1))
											.setSaveConsumer(fogDensity -> ModConfigManager.CONFIG.biomeFogs.get(biome.name).set(1, fogDensity.floatValue()))
											.setTooltip(Component.translatable("option.foglooksgoodnow.fogdensity.tooltip"))
											.setMin(0)
											.setMax(100)
											.setDefaultValue((float) ModConfigManager.CONFIG.getValue(ModConfigManager.DEFAULT_FOG_DENSITY.key()).value())
											.build()),
//									entryBuilder.startBooleanToggle(Component.translatable("option.foglooksgoodnow.overridecolors"), ModConfigManager.CONFIG.colorOverrides.get(biome.name).get(0))
//											.setSaveConsumer(override -> ModConfigManager.CONFIG.colorOverrides.get(biome.name).set(0, override))
//											.setTooltip(Component.translatable("option.foglooksgoodnow.overridecolors.tooltip"))
//											.setDefaultValue(false)
//											.build(),
//									entryBuilder.startColorField(Component.translatable("option.foglooksgoodnow.fogcolor"),  ModConfigManager.CONFIG.fogColors.get(biome.name).get(0))
//											.setDefaultValue(0)
//											.setTooltip(Component.translatable("option.foglooksgoodnow.fogcolor.tooltip"))
//											.setSaveConsumer(override -> ModConfigManager.CONFIG.fogColors.get(biome.name).set(0, override))
//											.build()),
							biome.name.equals(""));
				}
		);
	}
}
