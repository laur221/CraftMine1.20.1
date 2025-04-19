package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.Config;
import com.seishironagi.craftmine.gui.util.GuiTheme;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.SetGameDifficultyC2SPacket;
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
public class GameSettingsScreen extends Screen {
    // Panel dimensions
    private int imageWidth = 220;
    private int imageHeight = 180;
    private int leftPos;
    private int topPos;

    // Button dimensions
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 25;

    // Animation
    private final List<GameControllerScreen.AnimatedButton> buttons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    // Settings values
    private int currentDifficulty = Config.gameDifficulty;

    public GameSettingsScreen() {
        super(Component.literal("§a§lGame Settings"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Clear existing buttons
        buttons.clear();

        // Add difficulty button
        String difficultyText = "§eDifficulty: §f" + Config.getDifficultyName();
        addAnimatedButton(this.leftPos + 30, this.topPos + 50, difficultyText, button -> cycleDifficulty(), 0xFFAA33);

        // Add explanation text as a disabled button
        addAnimatedButton(this.leftPos + 30, this.topPos + 80,
                "§7Time is based on default settings", button -> {
                }, 0x888888);

        // Add current difficulty info as a disabled button
        addAnimatedButton(this.leftPos + 30, this.topPos + 110,
                "§7Current difficulty: §e" + Config.getDifficultyName(), button -> {
                }, 0x888888);

        // Add back button
        addAnimatedButton(this.leftPos + 30, this.topPos + 140, "§7§lBack", button -> goBackToMenu(), 0x555555);
    }

    private void addAnimatedButton(int x, int y, String text,
            net.minecraft.client.gui.components.Button.OnPress handler, int color) {
        GameControllerScreen.AnimatedButton button = new GameControllerScreen.AnimatedButton(
                x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal(text), handler, buttons.size(), color);
        buttons.add(button);
        this.addRenderableWidget(button);
    }

    private void cycleDifficulty() {
        // Play click sound
        playClickSound();

        // Update difficulty
        Config.cycleDifficulty();
        currentDifficulty = Config.gameDifficulty;

        // Send to server
        ModMessages.sendToServer(new SetGameDifficultyC2SPacket(currentDifficulty));

        // Recreate the screen with updated values
        Minecraft.getInstance().setScreen(new GameSettingsScreen());
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
        for (GameControllerScreen.AnimatedButton button : buttons) {
            button.updateAnimation(animationTime);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw background
        this.renderBackground(graphics);

        // Draw panel
        GuiTheme.renderPanel(
                graphics, leftPos, topPos, imageWidth, imageHeight,
                0x90054520, 0x90106530);

        // Draw the title
        graphics.drawCenteredString(font, this.title, this.width / 2, topPos + 15, 0xFFFFAA);

        // Draw subtitle
        graphics.drawCenteredString(font, Component.literal("§7Configure game settings"),
                this.width / 2, topPos + 30, 0xBBBBBB);

        // Render widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw explanatory text
        drawExplanationBox(graphics, mouseX, mouseY);
    }

    private void drawExplanationBox(GuiGraphics graphics, int mouseX, int mouseY) {
        String explanation = getDifficultyDescription();
        graphics.drawCenteredString(font, Component.literal(explanation),
                this.width / 2, this.topPos + this.imageHeight - 25, getDifficultyColor());
    }

    private String getDifficultyDescription() {
        switch (currentDifficulty) {
            case Config.DIFFICULTY_EASY:
                return "§aEasy: §fShorter game times";
            case Config.DIFFICULTY_HARD:
                return "§cHard: §fLonger game times";
            case Config.DIFFICULTY_MEDIUM:
            default:
                return "§6Medium: §fBalanced game times";
        }
    }

    private int getDifficultyColor() {
        switch (currentDifficulty) {
            case Config.DIFFICULTY_EASY:
                return 0xFF55FF55;
            case Config.DIFFICULTY_HARD:
                return 0xFFFF5555;
            case Config.DIFFICULTY_MEDIUM:
            default:
                return 0xFFFFAA00;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
