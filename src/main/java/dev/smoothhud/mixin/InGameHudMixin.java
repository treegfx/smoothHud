package dev.smoothhud.mixin;

import dev.smoothhud.ConfigManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
	@Shadow
	@Nullable
	protected abstract Player getCameraPlayer();

	@Unique
	private float currentX = 0;
	@Unique
	private long lastTickTime = 0;

	@Inject(
			method = "extractItemHotbar",
			at = @At("HEAD")
	)
	private void onRenderHotbar(CallbackInfo info) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			float targetX = player.getInventory().getSelectedSlot() * 20;

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
			method = "extractItemHotbar",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
					ordinal = 1
			)
	)
	private void mod(Args args, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		int baseX = (graphics.guiWidth() - 182) / 2 - 1;
		args.set(2, Math.round(baseX + currentX));
	}
}