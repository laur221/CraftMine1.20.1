package com.seishironagi.craftmine.gui;

import com.seishironagi.craftmine.difficulty.DifficultyManager;
import com.seishironagi.craftmine.gui.util.GuiTheme;
import com.seishironagi.craftmine.network.ModMessages;
import com.seishironagi.craftmine.network.packet.SetGameDifficultyC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import com.seishironagi.craftmine.client.ClientGameData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GameSettingsScreen extends Screen {
    // Panel dimensions
    private int imageWidth = 260;
    private int imageHeight = 160;
    private int leftPos;
    private int topPos;

    // Button dimensions
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_SPACING = 10;

    // Animation
    private final List<GameControllerScreen.AnimatedButton> buttons = new ArrayList<>();
    private float animationTime = 0;
    private boolean animatingIn = true;

    // Difficulty selection button
    private Button difficultyButton;
    private static final String[] DIFFICULTY_NAMES = { "Easy", "Medium", "Hard" };
    private static final int[] DIFFICULTY_COLORS = { 0xFF55AA55, 0xFFAAAA55, 0xFFAA5555 };

    public GameSettingsScreen() {
        super(Component.literal("§a§lGame Settings"));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Add buttons for settings
        int centerX = this.width / 2 - BUTTON_WIDTH / 2;
        int startY = this.topPos + 30;

        // Clear button list
        buttons.clear();

        // Add difficulty selection button
        int difficultyIndex = DifficultyManager.getInstance().getCurrentDifficulty();
        String difficultyText = "§l§eDifficulty: §f" + DIFFICULTY_NAMES[difficultyIndex];
        difficultyButton = addAnimatedButton(centerX, startY, difficultyText,
                b -> cycleDifficulty(), DIFFICULTY_COLORS[difficultyIndex]);

        // Add game time setting button
        addAnimatedButton(centerX, startY + BUTTON_HEIGHT + BUTTON_SPACING,
                "§l§bTime per Item: §fVariable",
                b -> {
                    /* Toggle time variation */},
                0xFF55AAAA);

        // Back button at the bottom
        addAnimatedButton(centerX, startY + (BUTTON_HEIGHT + BUTTON_SPACING) * 4,
                "§l§7Back", b -> goBackToMenu(), 0xFF888888);

        // Start animation
        animationTime = 0;
        animatingIn = true;
    }

    private GameControllerScreen.AnimatedButton addAnimatedButton(int x, int y, String text, Button.OnPress handler,
            int color) {
        GameControllerScreen.AnimatedButton button = new GameControllerScreen.AnimatedButton(
                x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal(text), handler, buttons.size(), color);
        buttons.add(button);
        this.addRenderableWidget(button);
        return button;
    }

    private void cycleDifficulty() {
        playClickSound();

        // Cycle difficulty client‑side and server‑side
        int newDifficulty = DifficultyManager.getInstance().cycleDifficulty();
        ClientGameData.setGameDifficulty(newDifficulty); // immediately update client state

        // Update button text and color
        String difficultyText = "§l§eDifficulty: §f" + DIFFICULTY_NAMES[newDifficulty];
        difficultyButton.setMessage(Component.literal(difficultyText));

        if (difficultyButton instanceof GameControllerScreen.AnimatedButton animButton) {
            animButton.setButtonColor(DIFFICULTY_COLORS[newDifficulty]);
        }

        ModMessages.sendToServer(new SetGameDifficultyC2SPacket(newDifficulty));
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
                0x90203040, 0x90304050);

        // Draw title
        graphics.drawCenteredString(font, this.title, this.width / 2, topPos + 15, 0xAAFFAA);

        // Draw settings description
        renderSettingsInfo(graphics);

        // Render the buttons
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSettingsInfo(GuiGraphics graphics) {
        int textX = leftPos + 20;
        int textY = topPos + imageHeight - 60;
        int lineHeight = 12;

        DifficultyManager difficultyManager = DifficultyManager.getInstance();

        // Draw difficulty description with updated timings
        String[] difficultyInfo = {
                "§e§lCurrent Difficulty: §f" + difficultyManager.getDifficultyName(),
                "§7Easy: Simple items, 3-7 minutes",
                "§7Medium: Moderate items, 8-15 minutes",
                "§7Hard: Complex items, 15-35 minutes"
        };

        for (String line : difficultyInfo) {
            graphics.drawString(font, Component.literal(line), textX, textY, 0xFFFFFF);
            textY += lineHeight;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
