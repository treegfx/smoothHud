/*package dev.smoothhud.screen;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.ModMenu;
import dev.smoothhud.ConfigManager;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.of("SmoothHud Config"));
        this.parent = parent;
    }

    protected void init() {
        SliderWidget slider = this.addDrawableChild(
                new SliderWidget(this.width / 2 - 100, this.height / 4 + 24, 200, 20, Text.of("Speed: " + (int)ConfigManager.getConfig().speed), ((int)ConfigManager.getConfig().speed - 2) / 98.0F) {
                    @Override
                    protected void updateMessage() {
                        ConfigManager.getConfig().speed = (int) (this.value * 98) + 2;
                        this.setMessage(Text.of("Speed: " + (int)ConfigManager.getConfig().speed));
                    }
                    @Override protected void applyValue() {}
                }
        );
        ButtonWidget CancelButton = (ButtonWidget)this.addDrawableChild(
                ButtonWidget.builder(
                                Text.of("Cancel"),
                                (button) -> {
                                    assert this.client != null;
                                    this.client.setScreen(parent);}
                        )
                        .dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20)
                        .build()
        );
        ButtonWidget SaveButton = (ButtonWidget)this.addDrawableChild(
                ButtonWidget.builder(
                                Text.of("Save & Quit"),
                                (button) -> {
                                    assert this.client != null;
                                    ConfigManager.saveConfig();
                                    this.client.setScreen(parent);
                                }
                )
                .dimensions(this.width / 2 - 100, this.height / 4 + 48, 200, 20)
                .build()
        );
    }
}
*/
package dev.smoothhud.screen;

import dev.smoothhud.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    private final Screen parent;
    private int selectedSlot = 0;
    private float tempSpeed = ConfigManager.getConfig().speed;
    private long lastTickTime = 0;
    private float currentX;

    public ConfigScreen(Screen parent) {
        super(Text.of("SmoothHud Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(
                new SliderWidget(this.width / 2 - 100, this.height / 4 + 24, 200, 20, Text.of("Speed: " + (int) tempSpeed), ((int) tempSpeed - 2) / 38.0F) {
                    @Override
                    protected void updateMessage() {
                        tempSpeed = (int) (this.value * 38) + 2;
                        this.setMessage(Text.of("Speed: " + (int) tempSpeed));
                    }

                    @Override
                    protected void applyValue() {}
                }
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.of("Cancel"), (button) -> {
                            assert this.client != null;
                            this.client.setScreen(parent);
                }).dimensions(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()
        );

        this.addDrawableChild(
                ButtonWidget.builder(Text.of("Save & Quit"), (button) -> {
                            assert this.client != null;
                            ConfigManager.getConfig().speed = tempSpeed;
                            ConfigManager.saveConfig();
                            this.client.setScreen(parent);
                }).dimensions(this.width / 2 - 100, this.height / 4 + 48, 200, 20).build()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //simple ass hotkey feature
        if (keyCode >= 49 && keyCode <= 57) {
            selectedSlot = keyCode - 49;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        verticalAmount = MathHelper.clamp(verticalAmount, -1.0F, 1.0F);
        //scrolling
        if (verticalAmount > 0) {
            selectedSlot = (selectedSlot - 1 + 9) % 9;
        } else if (verticalAmount < 0) {
            selectedSlot = (selectedSlot + 1) % 9;
        }
        return true;
    }

    /*@Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);


        float targetX = selectedSlot * 20;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTickTime) / 1000f;
        lastTickTime = currentTime;

        if (Math.abs(targetX - currentX) > 0.1f) {
            float diff = targetX - currentX;
            currentX += diff * deltaTime * tempSpeed;
        }

        int hotbarX = this.width / 2 - 91, hotbarY = this.height / 4 + 100;
        context.drawTexture(RenderLayer::getGuiTextured, Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar.png"), hotbarX, hotbarY, 0, 0, 182, 22, 182, 22, 182, 22);

        context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("hud/hotbar_selection"), hotbarX + (int)currentX, hotbarY - 1, 24, 23);

    }*/
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

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

        context.drawTexture(
                RenderLayer::getGuiTextured,
                Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar.png"),
                hotbarX, hotbarY,
                0, 0, 182, 22,
                182, 22, 182, 22
        );

        int roundedCurrentX = Math.round(currentX) - 1;

        context.drawGuiTexture(
                RenderLayer::getGuiTextured,
                Identifier.ofVanilla("hud/hotbar_selection"),
                hotbarX + roundedCurrentX, hotbarY - 1,
                24, 23
        );
    }


}