package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.JoinTeamC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeamSelectionScreen extends Screen {
    // Button dimensions and styling
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_SPACING = 35;

    // Animation properties
    private final List<AnimatedButton> menuButtons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    // Team colors
    private static final int RED_TEAM_COLOR = 0xFFDD3333;
    private static final int BLUE_TEAM_COLOR = 0xFF3333DD;
    private static final int NEUTRAL_COLOR = 0xFF888888;

    // Panel dimensions
    private int imageWidth = 200;
    private int imageHeight = 180;
    private int leftPos;
    private int topPos;

    public TeamSelectionScreen() {
        super(Component.literal("§9§lTeam Selection"));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        int startY = this.topPos + 50;

        // Clear existing buttons
        menuButtons.clear();

        // Add team buttons
        addAnimatedButton(centerX, startY, "§c§lJoin " + Config.redTeamName, b -> joinTeam("red"), RED_TEAM_COLOR);
        addAnimatedButton(centerX, startY + BUTTON_HEIGHT + BUTTON_SPACING, "§9§lJoin " + Config.blueTeamName,
                b -> joinTeam("blue"), BLUE_TEAM_COLOR);

        // Back button
        addAnimatedButton(centerX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 2, "§7§lBack", b -> goBackToMenu(),
                NEUTRAL_COLOR);

        // Start animation
        animationTime = 0;
        animatingIn = true;
    }

    private void addAnimatedButton(int x, int y, String text, Button.OnPress handler, int color) {
        AnimatedButton button = new AnimatedButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal(text), handler, menuButtons.size(), color);
        menuButtons.add(button);
        this.addRenderableWidget(button);
    }

    private void joinTeam(String team) {
        playClickSound();
        // Convert string team name to boolean (true for red team, false for blue team)
        boolean isRedTeam = "red".equalsIgnoreCase(team);
        ModMessages.sendToServer(new JoinTeamC2SPacket(isRedTeam));
        this.onClose();
    }

    private void goBackToMenu() {
        playClickSound();
        Minecraft.getInstance().setScreen(new GameControllerScreen());
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
        for (AnimatedButton button : menuButtons) {
            button.updateAnimation(animationTime);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw a dark, blurred background
        this.renderBackground(graphics);

        // Draw team panel backgrounds
        renderTeamPanels(graphics);

        // Draw decorative elements
        renderDecorations(graphics);

        // Draw the title
        renderTitle(graphics);

        // Render the buttons and other widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderTeamPanels(GuiGraphics graphics) {
        // Main panel with gradient
        graphics.fillGradient(
                leftPos, topPos,
                leftPos + imageWidth, topPos + imageHeight,
                0x90050505, 0x90303050);

        // Border
        int borderSize = 2;
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + borderSize, 0xFF5555AA);
        graphics.fill(leftPos, topPos, leftPos + borderSize, topPos + imageHeight, 0xFF5555AA);
        graphics.fill(leftPos + imageWidth - borderSize, topPos, leftPos + imageWidth, topPos + imageHeight,
                0xFF5555AA);
        graphics.fill(leftPos, topPos + imageHeight - borderSize, leftPos + imageWidth, topPos + imageHeight,
                0xFF5555AA);
    }

    private void renderDecorations(GuiGraphics graphics) {
        // Team emblems
        int emblemSize = 16;
        int emblemY = topPos + 35; // Moved up to avoid overlapping buttons

        // Red team emblem - left side of button
        graphics.fill(leftPos + 20, emblemY, leftPos + 20 + emblemSize, emblemY + emblemSize, RED_TEAM_COLOR);

        // Blue team emblem - right side of button
        graphics.fill(leftPos + imageWidth - 20 - emblemSize, emblemY,
                leftPos + imageWidth - 20, emblemY + emblemSize, BLUE_TEAM_COLOR);

        // VS text - moved up to avoid overlapping buttons
        graphics.drawCenteredString(font, Component.literal("§e§lVS"),
                this.width / 2, emblemY + emblemSize / 2 - 4, 0xFFFFFF);
    }

    private void renderTitle(GuiGraphics graphics) {
        // Draw title with shadow
        graphics.drawCenteredString(font, this.title, this.width / 2, topPos + 15, 0xFFFFAA);

        // Draw subtitle
        graphics.drawCenteredString(font, Component.literal("§7Choose your team"),
                this.width / 2, topPos + 30, 0xBBBBBB);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Custom animated button class with team colors
    public static class AnimatedButton extends Button {
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
