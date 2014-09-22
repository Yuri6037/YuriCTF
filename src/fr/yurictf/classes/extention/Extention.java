package fr.yurictf.classes.extention;

import fr.yurictf.classes.CTFClass;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.YuriCTF;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Extention {

    protected YuriCTF thePlugin;
    protected CTFPlayer thePlayer;
    protected CTFClass theClass;

    public void onUpdate() {
    }

    public Extention(YuriCTF plugin, CTFPlayer player, CTFClass ctfClass) {
        thePlugin = plugin;
        thePlayer = player;
        theClass = ctfClass;
    }

    public void resetExtention(){

    }

    public static ItemStack renameItem(ItemStack is, String newName) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(newName);
        is.setItemMeta(meta);
        return is;
    }
}
