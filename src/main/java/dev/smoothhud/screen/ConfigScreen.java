package dev.smoothhud.screen;

import dev.smoothhud.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static net.minecraft.util.Mth.clamp;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen{

    private final Screen parent;
    private int selectedSlot = 0;
    private float tempSpeed = ConfigManager.getConfig().speed;
    private long lastTickTime = 0;
    private float currentX;

    public ConfigScreen(Screen parent) {
        super(Component.literal("SmoothHud Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AbstractSliderButton speedButtonWidget = new AbstractSliderButton(
                this.width / 2 - 100, this.height / 4 + 24,
                200, 20,
                Component.literal("Speed: " + (int) tempSpeed),
                ((int) tempSpeed - 2) / 38.f
        ) {
            @Override
            protected void updateMessage() {
                // cursed math
                tempSpeed = (int) (this.value * 38.f) + 2;
                this.setMessage(Component.literal("Speed: " + (int) tempSpeed));
            }

            @Override protected void applyValue() {}
        };

        Button saveButtonWidget = Button.builder(Component.literal("Save & Exit"), (_) -> {
            // just assume everything going fine
            ConfigManager.getConfig().speed = tempSpeed;
            ConfigManager.saveConfig();
            this.minecraft.setScreen(parent);
        }).bounds(
                this.width / 2 - 100, this.height / 4 + 48,
                200, 20
        ).build();

        Button cancelButtonWidget = Button.builder(Component.literal("Cancel"), (_) -> {
            this.minecraft.setScreen(parent);
        }).bounds(
                this.width / 2 - 100, this.height / 4 + 72,
                200, 20
        ).build();


        this.addRenderableWidget(speedButtonWidget);
        this.addRenderableWidget(saveButtonWidget);
        this.addRenderableWidget(cancelButtonWidget);
    }


    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        // todo: refactor to use actual keybinds
        if (keyCode >= 49 && keyCode <= 57) {
            selectedSlot = keyCode - 49;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        verticalAmount = clamp(verticalAmount, -1.0F, 1.0F);
        if (verticalAmount > 0) {
            selectedSlot = (selectedSlot - 1 + 9) % 9;
        } else if (verticalAmount < 0) {
            selectedSlot = (selectedSlot + 1) % 9;
        }
        return true;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        float targetX = selectedSlot * 20;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTickTime) / 1000f;
        lastTickTime = currentTime;

        if (Math.abs(targetX - currentX) > 0.1f) {
            float diff = targetX - currentX;
            currentX += diff * deltaTime * tempSpeed;
        }

        int hotbarX = this.width / 2 - 91;
        int hotbarY = this.height / 4 + 100;
        int roundedCurrentX = Math.round(currentX) - 1;

        // hotbar
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/hotbar.png"),
                hotbarX, hotbarY,
                0, 0, 182, 22,
                182, 22, 182, 22
        );

        // hotbar selection
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/hud/hotbar_selection.png"),
                hotbarX + roundedCurrentX, hotbarY - 1,
                0, 0, 24, 23,
                24, 23, 24, 23
        );

        // text
        int x = this.width / 2;
        int y = hotbarY + 25;

        int color = ((currentTime / 300) % 2 == 0) ? 0xFF555555 : 0xFF666666;
        graphics.centeredText(this.font, "Scroll or press hotkeys to preview", x, y, color);
    }
}
