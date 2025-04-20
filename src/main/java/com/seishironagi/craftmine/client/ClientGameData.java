package com.seishironagi.craftmine.client;

import net.minecraft.world.item.ItemStack;

public class ClientGameData {
    private static int remainingSeconds = 0;
    private static boolean gameRunning = false;
    private static boolean redTeam = false;
    private static ItemStack targetItem = ItemStack.EMPTY;
    private static boolean gameJustStarted = false;
    private static boolean gameJustEnded = false;
    private static boolean redTeamWon = false;

    // Add difficulty related fields
    private static int gameDifficulty = 1; // Default to medium (1)
    private static int itemTimeMinutes = 10; // Default time in minutes

    // Constants for difficulty levels
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static void setRemainingSeconds(int seconds) {
        remainingSeconds = seconds;
    }

    public static int getRemainingSeconds() {
        return remainingSeconds;
    }

    public static void setGameRunning(boolean running) {
        if (!gameRunning && running) {
            // Game just started
            gameJustStarted = true;
            // Show announcement for 2 seconds
            AnnouncementOverlay.showAnnouncement("§6§lGame Started!", 0xFFAA00, 5000);

            // Schedule announcement to hide after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    gameJustStarted = false;
                } catch (InterruptedException e) {
                    // Ignore interruption
                }
            }).start();
        } else if (gameRunning && !running) {
            // Game just ended
            gameJustEnded = true;
            AnnouncementOverlay.showGameResult(redTeamWon);
        }

        gameRunning = running;
    }

    public static void setGameResult(boolean redWon) {
        redTeamWon = redWon;
        if (!gameRunning) {
            AnnouncementOverlay.showGameResult(redWon);
        }
    }

    public static boolean isGameRunning() {
        return gameRunning;
    }

    public static String getFormattedTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void setRedTeam(boolean isRedTeam) {
        redTeam = isRedTeam;
    }

    public static boolean isRedTeam() {
        return redTeam;
    }

    public static void setTargetItem(ItemStack item) {
        targetItem = item;

        // Only show announcement when the target item is set for the first time
        if (redTeam && item != null && !item.isEmpty() && (targetItem == null || targetItem.isEmpty())) {
            String itemName = item.getHoverName().getString();
            // Show a temporary announcement instead of persistent message
            AnnouncementOverlay.showAnnouncement("§e§lTarget: §f" + itemName, 0xFFDD00, 5000);
        }
    }

    public static ItemStack getTargetItem() {
        return targetItem;
    }

    public static void reset() {
        remainingSeconds = 0;
        gameRunning = false;
        redTeam = false;
        targetItem = ItemStack.EMPTY;
        gameJustStarted = false;
        gameJustEnded = false;

        // Also reset overlays
        AnnouncementOverlay.reset();
    }

    public static void setGameDifficulty(int difficulty) {
        if (gameDifficulty != difficulty) {
            gameDifficulty = difficulty;
            // Lock the update for time to prevent overwriting
            updateTimeBasedOnCurrentDifficulty();
        }
    }

    private static void updateTimeBasedOnCurrentDifficulty() {
        // Only update time if it hasn't been explicitly set
        switch (gameDifficulty) {
            case DIFFICULTY_EASY:
                itemTimeMinutes = 5;
                break;
            case DIFFICULTY_MEDIUM:
                itemTimeMinutes = 15;
                break;
            case DIFFICULTY_HARD:
                itemTimeMinutes = 35;
                break;
        }
    }

    public static int getGameDifficulty() {
        return gameDifficulty;
    }

    public static String getDifficultyName() {
        switch (gameDifficulty) {
            case DIFFICULTY_EASY:
                return "Easy";
            case DIFFICULTY_HARD:
                return "Hard";
            case DIFFICULTY_MEDIUM:
            default:
                return "Medium";
        }
    }

    public static void setItemTimeMinutes(int minutes) {
        itemTimeMinutes = minutes;
    }

    public static int getItemTimeMinutes() {
        return itemTimeMinutes;
    }
}
