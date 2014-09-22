package fr.yurictf.map;

import fr.yurictf.server.CTFPlayer;
import fr.yurictf.YuriCTF;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CTFMapScoreBoard {

    public YuriCTF thePlugin;
    public Scoreboard scoreboard;

    private ScoreboardScore redScore;
    private ScoreboardScore blueScore;

    public CTFMapScoreBoard(YuriCTF plugin){
        thePlugin = plugin;

        scoreboard = new Scoreboard();

        scoreboard.registerObjective("Scores", new ScoreboardBaseCriteria("flag"));

        redScore = scoreboard.getPlayerScoreForObjective(Bukkit.getOfflinePlayer(ChatColor.BOLD + "" + ChatColor.RED + "Red Score").getName(), scoreboard.getObjective("Scores"));
        blueScore = scoreboard.getPlayerScoreForObjective(Bukkit.getOfflinePlayer(ChatColor.BOLD + "" + ChatColor.BLUE + "Blue Score").getName(), scoreboard.getObjective("Scores"));
    }

    public void updateScoreBoard(){
        redScore.setScore(thePlugin.redScore);
        blueScore.setScore(thePlugin.blueScore);

        for (Player player : Bukkit.getOnlinePlayers()){
            CTFPlayer var = thePlugin.playerList.get(player.getName());
            if (var == null){
                return;
            }
            if (!var.hasScoreboard){
                PacketPlayOutScoreboardObjective packet0 = new PacketPlayOutScoreboardObjective(scoreboard.getObjective("Scores"), 0);//Create Scoreboard create packet
                PacketPlayOutScoreboardDisplayObjective display0 = new PacketPlayOutScoreboardDisplayObjective(1, scoreboard.getObjective("Scores"));//Create display packet set to sidebar mode(0 = list, 1 = sidebar, 2 = belowName)

                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet0);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(display0);
                var.hasScoreboard = true;
            }
            PacketPlayOutScoreboardScore scoreRed = new PacketPlayOutScoreboardScore(redScore, 0);//Create scoreboard item packet
            PacketPlayOutScoreboardScore scoreBlue = new PacketPlayOutScoreboardScore(blueScore, 0);//Create scoreboard item packet

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(scoreRed);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(scoreBlue);
        }
    }
}
