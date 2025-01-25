package dev.smoothhud.mixin;

import dev.smoothhud.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Unique
	private float currentX = 0;
	@Unique
	private long lastTickTime = 0;

	@Inject(
			method = "renderHotbar",
			at = @At("HEAD")
	)
	private void onRenderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player != null) {
			float targetX = player.getInventory().selectedSlot * 20;

			long currentTime = System.currentTimeMillis();
			float deltaTime = (currentTime - lastTickTime) / 1000f;
			lastTickTime = currentTime;

			if (Math.abs(targetX - currentX) > 0.1f) {
				float diff = targetX - currentX;
				currentX += diff * deltaTime * ConfigManager.getConfig().speed;
			}
		}
	}

	@ModifyArgs(
			method = "renderHotbar",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1)
	)
	private void mod(Args args) {
		int baseX = (MinecraftClient.getInstance().getWindow().getScaledWidth() - 182) / 2 - 1;
		args.set(2, Math.round(baseX + currentX));
	}
}
