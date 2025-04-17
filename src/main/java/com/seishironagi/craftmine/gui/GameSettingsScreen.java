package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.gui.util.GuiTheme;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.SetGameTimeC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GameSettingsScreen extends Screen {
    // Panel dimensions
    private int imageWidth = 200;
    private int imageHeight = 222;
    private int leftPos;
    private int topPos;

    // Button dimensions
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_SPACING = 35;

    // Animation
    private final List<AnimatedButton> buttons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    // Current toggle state for item times
    private boolean useItemTimes = Config.useItemSpecificTimes;

    public GameSettingsScreen() {
        super(Component.literal("§a§lGame Settings"));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Add buttons centered correctly
        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        int startY = this.topPos + 40;

        // Clear buttons list
        buttons.clear();

        // Game time settings
        addAnimatedButton(centerX, startY, "§e§l5 Minutes", b -> setGameTime(5), 0xFFDDAA33);
        addAnimatedButton(centerX, startY + BUTTON_SPACING, "§e§l10 Minutes", b -> setGameTime(10), 0xFFDDAA33);
        addAnimatedButton(centerX, startY + BUTTON_SPACING * 2, "§e§l15 Minutes", b -> setGameTime(15), 0xFFDDAA33);

        // Toggle item-specific times
        String toggleText = useItemTimes ? "§b§lItem Times: §a§lON" : "§b§lItem Times: §c§lOFF";
        addAnimatedButton(centerX, startY + BUTTON_SPACING * 3, toggleText, b -> toggleItemTimes(), 0xFF33AADD);

        // Back button
        addAnimatedButton(centerX, startY + BUTTON_SPACING * 4, "§7§lBack", b -> goBack(), 0xFF888888);

        // Start animation
        animationTime = 0;
        animatingIn = true;
    }

    private void addAnimatedButton(int x, int y, String text, Button.OnPress handler, int color) {
        AnimatedButton button = new AnimatedButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal(text), handler, buttons.size(), color);
        buttons.add(button);
        this.addRenderableWidget(button);
    }

    private void setGameTime(int minutes) {
        playClickSound();
        ModMessages.sendToServer(new SetGameTimeC2SPacket(minutes));
        this.minecraft.setScreen(null);
    }

    private void toggleItemTimes() {
        playClickSound();

        // Toggle the local value first
        useItemTimes = !useItemTimes;

        // Send packet to server
        ModMessages.sendToServer(new SetGameTimeC2SPacket(-1));

        // Just refresh this screen instead of recreating it
        this.clearWidgets();
        this.init();
    }

    private void goBack() {
        playClickSound();
        this.minecraft.setScreen(new GameControllerScreen());
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void tick() {
        super.tick();

        // Update animation
        if (animatingIn && animationTime < 1.0f) {
            animationTime += 0.05f;
            if (animationTime > 1.0f)
                animationTime = 1.0f;
        }

        // Update button animations
        for (AnimatedButton button : buttons) {
            button.updateAnimation(animationTime);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background
        this.renderBackground(graphics);

        // Draw panel
        renderPanel(graphics);

        // Draw the title
        graphics.drawCenteredString(font, this.title, this.width / 2, topPos + 15, 0xFFFFAA);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics graphics) {
        GuiTheme.renderPanel(
                graphics, leftPos, topPos, imageWidth, imageHeight,
                0x90105010, 0x90307030);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Custom animated button class with team colors
    private static class AnimatedButton extends Button {
        private final int index;
        private final int buttonColor;
        private float animProgress = 0;
        private int baseX;

        public AnimatedButton(int x, int y, int width, int height, Component component,
                OnPress onPress, int index, int color) {
            super(x - 50, y, width, height, component, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.baseX = x;
            this.buttonColor = color;
            this.active = false; // Start inactive until animation completes
        }

        @Override
        public void renderWidget(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (this.visible) {
                // Custom colored button rendering
                int bgColor = this.buttonColor;
                if (!this.active) {
                    bgColor = (bgColor & 0xFFFFFF) | 0x66000000;
                } else if (this.isHovered) {
                    bgColor = (bgColor & 0xFFFFFF) | 0xFF000000;
                }

                graphics.fill(this.getX(), this.getY(),
                        this.getX() + this.width, this.getY() + this.height,
                        bgColor);

                // Add border
                graphics.fill(this.getX(), this.getY(),
                        this.getX() + this.width, this.getY() + 1, 0xFFFFFFFF);
                graphics.fill(this.getX(), this.getY(),
                        this.getX() + 1, this.getY() + this.height, 0xFFFFFFFF);
                graphics.fill(this.getX() + this.width - 1, this.getY(),
                        this.getX() + this.width, this.getY() + this.height, 0x99FFFFFF);
                graphics.fill(this.getX(), this.getY() + this.height - 1,
                        this.getX() + this.width, this.getY() + this.height, 0x99FFFFFF);

                // Draw text centered
                int textColor = 0xFFFFFF;
                graphics.drawCenteredString(Minecraft.getInstance().font,
                        this.getMessage(), this.getX() + this.width / 2,
                        this.getY() + (this.height - 8) / 2, textColor);
            }
        }

        public void updateAnimation(float globalAnimTime) {
            // Stagger button animations
            float staggeredTime = Math.max(0, globalAnimTime - (index * 0.15f));
            this.animProgress = Math.min(1.0f, staggeredTime * 3.0f);

            // Update position based on animation
            this.setX((int) (baseX - 50 * (1.0f - animProgress)));

            // Enable once animation reaches certain threshold
            this.active = animProgress > 0.7f;

            // Update opacity
            this.setAlpha(animProgress);
        }
    }
}
