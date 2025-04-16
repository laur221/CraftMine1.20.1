package com.seishironagi.craftmine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private static final String RED_TEAM_ID = "craftmine_red";
    private static final String BLUE_TEAM_ID = "craftmine_blue";
    
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private UUID redTeamPlayer = null;
    
    public boolean addToRedTeam(Player player) {
        // Check if red team already has a player
        if (redTeamPlayer != null && !redTeamPlayer.equals(player.getUUID())) {
            return false;
        }
        
        // Remove from previous team if any
        removeFromTeam(player);
        
        // Add to red team
        setTeam(player, RED_TEAM_ID);
        redTeamPlayer = player.getUUID();
        playerTeams.put(player.getUUID(), RED_TEAM_ID);
        
        player.sendSystemMessage(Component.literal("You joined the " + Config.redTeamName)
                .withStyle(ChatFormatting.RED));
        return true;
    }
    
    public boolean addToBlueTeam(Player player) {
        // Don't allow joining blue team if player is the red team player
        if (player.getUUID().equals(redTeamPlayer)) {
            redTeamPlayer = null;
        }
        
        // Remove from previous team if any
        removeFromTeam(player);
        
        // Add to blue team
        setTeam(player, BLUE_TEAM_ID);
        playerTeams.put(player.getUUID(), BLUE_TEAM_ID);
        
        player.sendSystemMessage(Component.literal("You joined the " + Config.blueTeamName)
                .withStyle(ChatFormatting.BLUE));
        return true;
    }
    
    public void removeFromTeam(Player player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        
        Scoreboard scoreboard = server.getScoreboard();
        scoreboard.removePlayerFromTeam(player.getScoreboardName());
        
        if (player.getUUID().equals(redTeamPlayer)) {
            redTeamPlayer = null;
        }
        
        playerTeams.remove(player.getUUID());
    }
    
    private void setTeam(Player player, String teamId) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        
        Scoreboard scoreboard = server.getScoreboard();
        
        // Create team if it doesn't exist
        PlayerTeam team = scoreboard.getPlayerTeam(teamId);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamId);
            if (teamId.equals(RED_TEAM_ID)) {
                team.setColor(ChatFormatting.RED);
                team.setDisplayName(Component.literal(Config.redTeamName));
            } else {
                team.setColor(ChatFormatting.BLUE);
                team.setDisplayName(Component.literal(Config.blueTeamName));
            }
        }
        
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }
    
    public boolean isRedTeam(Player player) {
        String team = playerTeams.get(player.getUUID());
        return team != null && team.equals(RED_TEAM_ID);
    }
    
    public boolean isBlueTeam(Player player) {
        String team = playerTeams.get(player.getUUID());
        return team != null && team.equals(BLUE_TEAM_ID);
    }
    
    public void resetTeams(MinecraftServer server) {
        if (server == null) return;
        
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam redTeam = scoreboard.getPlayerTeam(RED_TEAM_ID);
        PlayerTeam blueTeam = scoreboard.getPlayerTeam(BLUE_TEAM_ID);
        
        if (redTeam != null) {
            scoreboard.removePlayerTeam(redTeam);
        }
        
        if (blueTeam != null) {
            scoreboard.removePlayerTeam(blueTeam);
        }
        
        playerTeams.clear();
        redTeamPlayer = null;
    }
    
    public Player getRedTeamPlayer(MinecraftServer server) {
        if (redTeamPlayer == null || server == null) return null;
        
        for (Player player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(redTeamPlayer)) {
                return player;
            }
        }
        
        return null;
    }
}
