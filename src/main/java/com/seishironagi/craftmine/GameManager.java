package com.seishironagi.craftmine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class GameManager {
    private static GameManager INSTANCE;
    
    private final TeamManager teamManager;
    private Item targetItem;
    private int gameTimeMinutes = Config.defaultGameTime;
    private long gameEndTimeMillis;
    private boolean gameRunning = false;
    private final Timer gameTimer = new Timer("GameTimer");
    private TimerTask displayTask;
    
    private GameManager() {
        this.teamManager = new TeamManager();
    }
    
    public static GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }
    
    public TeamManager getTeamManager() {
        return teamManager;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public void setGameTime(int minutes) {
        if (minutes > 0) {
            this.gameTimeMinutes = minutes;
        }
    }
    
    public int getGameTime() {
        return gameTimeMinutes;
    }
    
    public long getRemainingTimeMillis() {
        if (!gameRunning) return 0;
        return Math.max(0, gameEndTimeMillis - System.currentTimeMillis());
    }
    
    public int getRemainingTimeSeconds() {
        return (int) (getRemainingTimeMillis() / 1000);
    }
    
    public Item getTargetItem() {
        return targetItem;
    }
    
    public void startGame() {
        if (gameRunning) return;
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        Player redPlayer = teamManager.getRedTeamPlayer(server);
        if (redPlayer == null) {
            broadcastMessage("Cannot start game: No red team player assigned.", ChatFormatting.RED);
            return;
        }
        
        // Select random item
        List<Item> items = new ArrayList<>(Config.gameItems);
        if (items.isEmpty()) {
            broadcastMessage("Cannot start game: No items configured.", ChatFormatting.RED);
            return;
        }
        
        targetItem = items.get(new Random().nextInt(items.size()));
        
        // Calculate game time
        if (Config.useItemSpecificTimes && Config.itemTimes.containsKey(targetItem)) {
            gameTimeMinutes = Config.itemTimes.get(targetItem);
        }
        
        // Set end time
        gameEndTimeMillis = System.currentTimeMillis() + (gameTimeMinutes * 60 * 1000);
        
        // Send messages
        broadcastMessage(Config.gameStartMessage, ChatFormatting.GOLD);
        
        // Send target message to red team only
        String itemName = targetItem.getDescription().getString();
        String redMessage = String.format(Config.redTeamTaskMessage, itemName, gameTimeMinutes);
        redPlayer.sendSystemMessage(Component.literal(redMessage).withStyle(ChatFormatting.GOLD));
        
        // Start timer display
        startTimerDisplay();
        
        gameRunning = true;
    }
    
    private void startTimerDisplay() {
        if (displayTask != null) {
            displayTask.cancel();
        }
        
        displayTask = new TimerTask() {
            @Override
            public void run() {
                checkGameStatus();
            }
        };
        
        gameTimer.scheduleAtFixedRate(displayTask, 0, 1000);
    }
    
    private void checkGameStatus() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        if (getRemainingTimeMillis() <= 0) {
            endGame(false);
            return;
        }
        
        Player redPlayer = teamManager.getRedTeamPlayer(server);
        if (redPlayer != null) {
            // Check if red player has the item
            for (ItemStack stack : redPlayer.getInventory().items) {
                if (!stack.isEmpty() && stack.getItem() == targetItem) {
                    endGame(true);
                    return;
                }
            }
        }
    }
    
    public void endGame(boolean redTeamWin) {
        if (!gameRunning) return;
        
        if (displayTask != null) {
            displayTask.cancel();
            displayTask = null;
        }
        
        if (redTeamWin) {
            broadcastMessage(Config.redTeamWinMessage, ChatFormatting.GOLD);
        } else {
            broadcastMessage(Config.blueTeamWinMessage, ChatFormatting.GOLD);
        }
        
        gameRunning = false;
        targetItem = null;
    }
    
    public void resetGame() {
        if (gameRunning) {
            endGame(false);
        }
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            teamManager.resetTeams(server);
        }
        
        gameTimeMinutes = Config.defaultGameTime;
        targetItem = null;
        gameRunning = false;
    }
    
    private void broadcastMessage(String message, ChatFormatting color) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        Component component = Component.literal(message).withStyle(color);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(component);
        }
    }
}
