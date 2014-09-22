package fr.yurictf.server;

import fr.yurictf.YuriCTF;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    private YuriCTF thePlugin;
    public List<String> allowedBuilders;

    public PermissionManager(YuriCTF plugin){
        thePlugin = plugin;
        allowedBuilders = new ArrayList<String>();
    }

    public void writePermissions(){
        CraftServer serv = (CraftServer) thePlugin.getServer();
        MinecraftServer server = serv.getServer();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(server.d("plugins") + File.separator + "YuriCTF" + File.separator + "builders.txt")));
            for (String s : allowedBuilders){
                writer.write(s);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            thePlugin.getLogger().warning("Error occured while attempting to write builders...");
            //e.printStackTrace();
        }
    }

    public void readPermissions(){
        CraftServer serv = (CraftServer) thePlugin.getServer();
        MinecraftServer server = serv.getServer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(server.d("plugins") + File.separator + "YuriCTF" + File.separator + "builders.txt")));
            String line;
            while ((line = reader.readLine()) != null){
                if (line.equals("")){
                    continue;
                }
                allowedBuilders.add(line);
            }
            reader.close();
        } catch (IOException e) {
            thePlugin.getLogger().warning("Error occured while attempting to read builders...");
            //e.printStackTrace();
        }
    }
}
