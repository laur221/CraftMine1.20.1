package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.gui.util.GuiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
public class GameInfoScreen extends Screen {
    // Panel dimensions
    private int imageWidth = 260;
    private int imageHeight = 250;
    private int leftPos;
    private int topPos;

    // Button dimensions
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 25;

    // Animation
    private final List<TeamSelectionScreen.AnimatedButton> buttons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    public GameInfoScreen() {
        super(Component.literal("§e§lGame Information"));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Add buttons for navigation
        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        int startY = this.topPos + this.imageHeight + 5;

        // Clear button list
        buttons.clear();

        // Add back button at the bottom
        TeamSelectionScreen.AnimatedButton backButton = new TeamSelectionScreen.AnimatedButton(
                centerX, startY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("§7§lBack"), b -> goBackToMenu(), 0, 0xFF888888);
        this.addRenderableWidget(backButton);
        buttons.add(backButton);

        // Start animation
        animationTime = 0;
        animatingIn = true;
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
        for (int i = 0; i < buttons.size(); i++) {
            TeamSelectionScreen.AnimatedButton button = buttons.get(i);
            button.updateAnimation(animationTime);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background
        this.renderBackground(graphics);

        // Draw panel
        renderPanel(graphics);

        // Draw information content
        renderInfoContent(graphics);

        // Draw the title
        graphics.drawCenteredString(font, this.title, this.width / 2, topPos + 10, 0xFFFFAA);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics graphics) {
        // Use the theme to render panel
        GuiTheme.renderPanel(
                graphics, leftPos, topPos, imageWidth, imageHeight,
                0x90151515, 0x90303030);
    }

    private void renderInfoContent(GuiGraphics graphics) {
        int textX = leftPos + 20;
        int textY = topPos + 30;
        int lineHeight = 12;

        // Game information text
        graphics.drawString(font, Component.literal("§e§lHow to Play:"), textX, textY, 0xFFFFFF);
        textY += lineHeight * 2;

        // Updated content with more accurate game information
        String[] infoLines = {
                "§f1. Choose your team (red or blue)",
                "§f2. Red team: Find target items in overworld",
                "§f3. Blue team: Stop red team from finding items",
                "§f4. Red team wins if they find the target item",
                "§f5. Blue team wins if time runs out",
                "",
                "§c§lRed Team Tips:",
                "§f• Explore efficiently to find your target",
                "§f• Watch out for blue team players",
                "§f• If blue team kills you, your team wins!",
                "",
                "§9§lBlue Team Tips:",
                "§f• Hunt down red team players",
                "§f• Block access to valuable resources",
                "§f• Prevent red team from reaching their goal"
        };

        for (String line : infoLines) {
            graphics.drawString(font, Component.literal(line), textX, textY, 0xFFFFFF);
            textY += lineHeight;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
