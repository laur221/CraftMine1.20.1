package com.seishironagi.craftmine.client;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClientGameData {
    private static int remainingSeconds = 0;
    private static boolean gameRunning = false;
    private static boolean redTeam = false;
    private static ItemStack targetItem = ItemStack.EMPTY;
    private static boolean gameJustStarted = false;
    private static boolean gameJustEnded = false;
    private static boolean redTeamWon = false;

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
            AnnouncementOverlay.showAnnouncement("§6§lGame Started!", 0xFFAA00, 2000);

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
        if (redTeam && item != null && !item.isEmpty()) {
            // For red team, show a more prominent message about target item
            String itemName = item.getHoverName().getString();
            AnnouncementOverlay.showAnnouncement("§e§lYour Target: §f" + itemName, 0xFFDD00, 5000);
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
}
