package fr.yurictf.votifier;

import fr.yurictf.server.CTFPlayer;
import fr.yurictf.YuriCTF;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class VotifierPlayer {

    public int votePoints;

    private YuriCTF thePlugin;
    private CTFPlayer thePlayer;

    public VotifierPlayer(YuriCTF plugin, CTFPlayer player){
        thePlugin = plugin;
        thePlayer = player;
        FileConfiguration file = new YamlConfiguration();
        try {
            file.load(plugin.getDataFolder() + File.separator + "data" + File.separator + player.associatedPlayer.getUniqueId() + ".yml");
            votePoints = file.getInt("votePoints");
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(){
        FileConfiguration file = new YamlConfiguration();
        file.set("votePoints", votePoints);
        try {
            file.save(thePlugin.getDataFolder() + File.separator + "data" + File.separator + thePlayer.associatedPlayer.getUniqueId() + ".yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
