package com.seishironagi.craftmine.client;

import net.minecraft.client.Minecraft;

public class ClientGameData {
    private static int remainingSeconds = 0;
    private static boolean gameRunning = false;
    
    public static void setRemainingSeconds(int seconds) {
        remainingSeconds = seconds;
    }
    
    public static int getRemainingSeconds() {
        return remainingSeconds;
    }
    
    public static void setGameRunning(boolean running) {
        gameRunning = running;
    }
    
    public static boolean isGameRunning() {
        return gameRunning;
    }
    
    public static String getFormattedTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
