package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.gui.GameSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.OpenMainMenuC2SPacket;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GameMenuScreen extends Screen {
    // Button dimensions and colors
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_SPACING = 6;
    
    // Panel dimensions
    private int backgroundWidth = 200;
    private int backgroundHeight = 180;
    private int leftPos;
    private int topPos;
    
    // Button list for animations
    private final List<AnimatedButton> menuButtons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    public GameMenuScreen() {
        super(Component.literal("§6§lCraftMine Menu"));
    }

    @Override
    protected void init() {
        super.init();
        
        this.leftPos = (this.width - this.backgroundWidth) / 2;
        this.topPos = (this.height - this.backgroundHeight) / 2;
        
        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        int startY = this.topPos + 40;
        int totalHeight = BUTTON_HEIGHT * 4 + BUTTON_SPACING * 3;
        startY = this.height / 2 - totalHeight / 2;
        
        // Clear existing buttons
        menuButtons.clear();
        
        // Add stylized animated buttons
        addAnimatedButton(centerX, startY, "§l§eStart Game", b -> startGame());
        addAnimatedButton(centerX, startY + BUTTON_HEIGHT + BUTTON_SPACING, "§l§bChoose Team", b -> openTeamScreen());
        addAnimatedButton(centerX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 2, "§l§aSettings", b -> openSettingsScreen());
        addAnimatedButton(centerX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 3, "§l§cReturn to Game", b -> this.onClose());
        
        // Start animation
        animationTime = 0;
        animatingIn = true;
    }
    
    private void addAnimatedButton(int x, int y, String text, Button.OnPress handler) {
        AnimatedButton button = new AnimatedButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, 
                Component.literal(text), handler, menuButtons.size());
        menuButtons.add(button);
        this.addRenderableWidget(button);
    }
    
    private void startGame() {
        if (Minecraft.getInstance().player != null) {
            // Play button click sound
            Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                
            ModMessages.sendToServer(new OpenMainMenuC2SPacket());
            this.onClose();
        }
    }
    
    private void openTeamScreen() {
        // Use direct screen opening instead of container approach
        playClickSound();
        Minecraft.getInstance().setScreen(new TeamSelectionScreen());
    }
    
    private void openSettingsScreen() {
        // Use direct screen opening instead of container approach
        playClickSound();
        Minecraft.getInstance().setScreen(new GameSettingsScreen());
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
            if (animationTime > 1.0f) animationTime = 1.0f;
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
        
        // Draw a fancy panel background with gradient
        renderGradientPanel(graphics);
        
        // Draw decorative elements
        renderDecorations(graphics);
        
        // Draw the title with shadow and gradient
        renderTitle(graphics);
        
        // Render the buttons and other widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }
    
    private void renderGradientPanel(GuiGraphics graphics) {
        // Draw gradient background for panel
        graphics.fillGradient(
                leftPos, topPos, 
                leftPos + backgroundWidth, topPos + backgroundHeight,
                0x90050505, 0x90352828);
                
        // Draw shiny border
        int borderSize = 2;
        int time = (int)(System.currentTimeMillis() / 100 % 60);
        int borderColor = 0xFF901010 + (time * 4 << 16);
        
        // Top border
        graphics.fill(leftPos, topPos, leftPos + backgroundWidth, topPos + borderSize, borderColor);
        // Left border
        graphics.fill(leftPos, topPos, leftPos + borderSize, topPos + backgroundHeight, borderColor);
        // Right border
        graphics.fill(leftPos + backgroundWidth - borderSize, topPos, leftPos + backgroundWidth, topPos + backgroundHeight, borderColor);
        // Bottom border
        graphics.fill(leftPos, topPos + backgroundHeight - borderSize, leftPos + backgroundWidth, topPos + backgroundHeight, borderColor);
    }
    
    private void renderDecorations(GuiGraphics graphics) {
        // Draw decorative corners
        int cornerSize = 8;
        graphics.fill(leftPos, topPos, leftPos + cornerSize, topPos + cornerSize, 0xFFFF5555);
        graphics.fill(leftPos + backgroundWidth - cornerSize, topPos, leftPos + backgroundWidth, topPos + cornerSize, 0xFFFF5555);
        graphics.fill(leftPos, topPos + backgroundHeight - cornerSize, leftPos + cornerSize, topPos + backgroundHeight, 0xFFFF5555);
        graphics.fill(leftPos + backgroundWidth - cornerSize, topPos + backgroundHeight - cornerSize, leftPos + backgroundWidth, topPos + backgroundHeight, 0xFFFF5555);
    }
    
    private void renderTitle(GuiGraphics graphics) {
        // Draw title with shadow for better readability
        graphics.drawCenteredString(font, title, this.width / 2, topPos + 15, 0xFFFFAA);
        
        // Draw subtitle
        graphics.drawCenteredString(font, Component.literal("§7Select an option"), 
                this.width / 2, topPos + 28, 0xBBBBBB);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    // Custom animated button class
    private class AnimatedButton extends Button {
        private final int index;
        private float animProgress = 0;
        private int baseX;
        
        public AnimatedButton(int x, int y, int width, int height, Component component, OnPress onPress, int index) {
            super(x - 50, y, width, height, component, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.baseX = x;
            this.active = false; // Start inactive until animation completes
        }
        
        public void updateAnimation(float globalAnimTime) {
            // Stagger button animations
            float staggeredTime = Math.max(0, globalAnimTime - (index * 0.15f));
            this.animProgress = Math.min(1.0f, staggeredTime * 3.0f);
            
            // Update position based on animation
            this.setX((int)(baseX - 50 * (1.0f - animProgress)));
            
            // Enable once animation reaches certain threshold
            this.active = animProgress > 0.7f;
            
            // Update opacity
            this.setAlpha(animProgress);
        }
    }
}
