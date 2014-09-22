package fr.yurictf.map;

import fr.yurictf.YuriCTF;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.server.StatusBarAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Random;

public class CTFMapChat {

    private YuriCTF thePlugin;

    private int ticks;

    public int timerDisplayTicks;

    private String[] lines;

    public CTFMapChat(YuriCTF plugin, String[] text){
        thePlugin = plugin;
        lines = text;
    }

    public void updateChat(){
        ticks++;
        if (!thePlugin.ctfMap.mapEditMode.isMapInEditMode()){
            timerDisplayTicks++;
        }
        if (ticks >= 1024){
            Random r = new Random();
            int i = r.nextInt(4);
            thePlugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + lines[i]);
            ticks = 0;
        } else if (timerDisplayTicks >= 512 && !thePlugin.ctfMap.mapTimer.isFinished){
            for (Player p : thePlugin.getServer().getOnlinePlayers()){
                CTFPlayer player = thePlugin.playerList.get(p.getName());
                if (player != null){
                    String s = player.playerTranslator.translate("timer.timeCheck");
                    String s1 = s.replace("#TIME#", thePlugin.ctfMap.mapTimer.getTimeAsString());
                    player.associatedPlayer.sendMessage(ChatColor.LIGHT_PURPLE + s1);
                }
            }
            timerDisplayTicks = 0;
        }

        if (thePlugin.ctfMap.mapTimer.isFinished) {
            int restentSec = 40 - thePlugin.ctfMap.mapTimer.seconds;
            float percentage = (((float)thePlugin.ctfMap.mapTimer.seconds * 100F) / 40F) / 100F;
            StatusBarAPI.removeAllStatusBars();
            StatusBarAPI.setAllStatusBars(ChatColor.LIGHT_PURPLE + "New Game session starting in " + restentSec + "...", percentage);
        }
    }
}
