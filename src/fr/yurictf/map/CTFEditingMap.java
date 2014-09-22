package fr.yurictf.map;

import fr.yurictf.YuriCTF;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CTFEditingMap{

    private boolean editingMap;
    public List<String> allowedEditingPlayers;
    public boolean flagPlacerTool;

    public CTFEditingMap(YuriCTF plugin) {
        allowedEditingPlayers = new ArrayList<String>();
        allowedEditingPlayers.addAll(plugin.permissionManager.allowedBuilders);
        String[] devs = plugin.getAllDevMembers().split(",");
        for (String s : devs){
            allowedEditingPlayers.add(s);
        }
        editingMap = plugin.getConfig().getBoolean("editingMap");
    }

    public boolean isPlayerAllowedToBuild(Player player){
        return editingMap && allowedEditingPlayers.contains(player.getName());
    }

    public boolean isMapInEditMode(){
        return editingMap;
    }

    public boolean isFlagPlacerToolActivated(Player player){
        return flagPlacerTool && allowedEditingPlayers.contains(player.getName());
    }
}
