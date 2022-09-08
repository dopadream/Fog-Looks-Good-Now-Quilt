package com.birsy.foglooksgoodnow;

import com.birsy.foglooksgoodnow.config.ModConfigManager;
import net.minecraft.data.BuiltinRegistries;
import org.quiltmc.config.api.values.ValueList;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FogLooksGoodNowMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.




	public static final String MOD_ID = "foglooksgoodnow";
	public static final Logger LOGGER = LoggerFactory.getLogger("Fog Looks Good Now");

	@Override
	public void onInitialize(ModContainer mod) {

		new ModConfigManager();

	}
}
