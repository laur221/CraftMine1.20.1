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
    private static final int BUTTON_WIDTH = 100;
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
    private int imageWidth = 280;
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

        // Clear existing buttons
        menuButtons.clear();

        // Calculate positions for horizontal team buttons
        int buttonY = this.topPos + 60;
        int redButtonX = this.width / 2 - BUTTON_WIDTH - 30; // 10px spacing between buttons
        int blueButtonX = this.width / 2 + 30;

        // Calculate position for back button - centered and below team buttons
        int backButtonX = this.width / 2 - BUTTON_WIDTH / 2;
        int backButtonY = buttonY + BUTTON_HEIGHT + 40; // More space between team buttons and back

        // Add team buttons side by side
        addAnimatedButton(redButtonX, buttonY, "§c§lJoin " + Config.redTeamName,
                b -> joinTeam("red"), RED_TEAM_COLOR);
        addAnimatedButton(blueButtonX, buttonY, "§9§lJoin " + Config.blueTeamName,
                b -> joinTeam("blue"), BLUE_TEAM_COLOR);

        // Back button centered below
        addAnimatedButton(backButtonX, backButtonY, "§7§lBack", b -> goBackToMenu(),
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
        int emblemY = topPos + 68; // Moved up to avoid overlapping buttons

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
        private int baseY;

        public AnimatedButton(int x, int y, int width, int height, Component component,
                OnPress onPress, int index, int color) {
            super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.baseX = x;
            this.baseY = y;
            this.buttonColor = color;
            this.active = false; // Start inactive until animation completes

            // Start buttons off-screen based on their index
            if (index == 0) { // Red team button comes from left
                this.setX(x - 100);
            } else if (index == 1) { // Blue team button comes from right
                this.setX(x + 100);
            } else { // Back button comes from bottom
                this.setY(y + 50);
            }
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
            // Different animation based on button position
            float staggeredTime = Math.max(0, globalAnimTime - (index * 0.1f));
            this.animProgress = Math.min(1.0f, staggeredTime * 3.0f);

            // Different animation for each button
            if (index == 0) { // Red team
                this.setX((int) (baseX - 100 * (1.0f - animProgress)));
            } else if (index == 1) { // Blue team
                this.setX((int) (baseX + 100 * (1.0f - animProgress)));
            } else { // Back button
                this.setY((int) (baseY + 50 * (1.0f - animProgress)));
            }

            // Enable once animation reaches certain threshold
            this.active = animProgress > 0.7f;

            // Update opacity
            this.setAlpha(animProgress);
        }
    }
}
