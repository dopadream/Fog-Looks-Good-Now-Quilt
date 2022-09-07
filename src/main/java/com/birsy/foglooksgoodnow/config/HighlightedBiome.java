package com.birsy.foglooksgoodnow.config;

import net.minecraft.data.BuiltinRegistries;

import java.util.ArrayList;
import java.util.List;

public class HighlightedBiome {
	public String name;
	public float fogStart;
	public float fogDensity;

	public HighlightedBiome()
	{
		name = "";
		fogStart = 0.0F;
		fogDensity = 1.0F;
	}


	public HighlightedBiome(String name, Float fogStart, Float fogDensity)
	{
		this.name = name;
		this.fogStart = fogStart;
		this.fogDensity = fogDensity;
	}

	private static List<String> biomes() {
		ArrayList<String> entries = new ArrayList<>();
		BuiltinRegistries.BIOME.stream().forEach(biome -> entries.add(BuiltinRegistries.BIOME.getKey(biome).toString()));
		return entries;
	}
}
