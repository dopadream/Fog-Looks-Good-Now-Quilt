package com.birsy.foglooksgoodnow.mixin;

import com.birsy.foglooksgoodnow.config.ModConfigManager;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.data.BuiltinRegistries;
import org.quiltmc.config.api.values.ValueList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(method = "init", at = @At("RETURN"))
	public void init(CallbackInfo ci) {
		BuiltinRegistries.BIOME.stream().forEach(biome -> ModConfigManager.CONFIG.biomeOverrides.putIfAbsent(BuiltinRegistries.BIOME.getKey(biome).toString(), ValueList.create(false, false)));
		BuiltinRegistries.BIOME.stream().forEach(biome -> ModConfigManager.CONFIG.biomeFogs.putIfAbsent(BuiltinRegistries.BIOME.getKey(biome).toString(), ValueList.create(1.0F, 0.0F, 1.0F)));
	}
}
