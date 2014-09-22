package fr.yurictf.server;

import fr.yurictf.YuriCTF;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamAPI {

    private Scoreboard manager;
    private Objective objective;
    private Team redTeam;
    private Team blueTeam;

    private Map<String, Integer> playerMap;

    private YuriCTF thePlugin;

    public TeamAPI(YuriCTF plugin){
        thePlugin = plugin;
        playerMap = new HashMap<String, Integer>();
        init();
    }

    private void init(){
        ScoreboardManager creator = thePlugin.getServer().getScoreboardManager();
        manager = creator.getNewScoreboard();
        redTeam = manager.registerNewTeam("RedTeam");
        blueTeam = manager.registerNewTeam("BlueTeam");
        redTeam.setPrefix(ChatColor.RED + "[RED] ");
        blueTeam.setPrefix(ChatColor.BLUE + "[BLUE] ");
        redTeam.setCanSeeFriendlyInvisibles(true);
        blueTeam.setCanSeeFriendlyInvisibles(true);

        objective = manager.registerNewObjective("showhealth", "health");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        objective.setDisplayName(ChatColor.YELLOW + "/ 20");

        thePlugin.getServer().getScheduler().scheduleSyncRepeatingTask(thePlugin, new Runnable() {
            public void run() {
                update();
            }
        }, 20L, 20L);
    }

    private void update(){
        for(Player online : thePlugin.getServer().getOnlinePlayers()){
            online.setScoreboard(manager);
            online.setHealth(online.getHealth()); //Update their health
        }
    }

    public void addPlayerToTeam(int teamID, CTFPlayer player){
        switch(teamID){
            case 0:
                redTeam.addPlayer(player.associatedPlayer);
                playerMap.put(player.associatedPlayer.getName(), 0);
                break;
            case 1:
                blueTeam.addPlayer(player.associatedPlayer);
                playerMap.put(player.associatedPlayer.getName(), 1);
                break;
        }
    }

    public void removePlayerFromTeam(int teamID, CTFPlayer player){
        switch(teamID){
            case 0:
                redTeam.removePlayer(player.associatedPlayer);
                playerMap.remove(player.associatedPlayer.getName());
                break;
            case 1:
                blueTeam.removePlayer(player.associatedPlayer);
                playerMap.remove(player.associatedPlayer.getName());
                break;
        }
    }

    public void resetTeams(){
        resetRedTeam();
        resetBlueTeam();
        playerMap.clear();
    }

    public boolean isPlayerInTeam(int teamID, CTFPlayer player){
        if (!playerMap.containsKey(player.associatedPlayer.getName())){
            return false;
        }
        int i = playerMap.get(player.associatedPlayer.getName());
        return i == teamID;
    }

    private void resetRedTeam(){
        List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
        for (OfflinePlayer p : redTeam.getPlayers()){
            players.add(p);
        }
        for (OfflinePlayer p1 : players){
            redTeam.removePlayer(p1);
        }
    }
    private void resetBlueTeam(){
        List<OfflinePlayer> players = new ArrayList<OfflinePlayer>();
        for (OfflinePlayer p : blueTeam.getPlayers()){
            players.add(p);
        }
        for (OfflinePlayer p1 : players){
            blueTeam.removePlayer(p1);
        }
    }
}
