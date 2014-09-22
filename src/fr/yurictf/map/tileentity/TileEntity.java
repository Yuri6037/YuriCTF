package fr.yurictf.map.tileentity;

import fr.yurictf.map.CTFMap;
import fr.yurictf.YuriCTF;
import fr.yurictf.server.CTFPlayer;
import org.bukkit.Location;

public interface TileEntity {

    public void onUpdate(YuriCTF plugin, CTFMap map);

    public void onBlockDestroy(YuriCTF plugin, CTFMap map, Location blockLocation);

    public void onBlockPlaced(YuriCTF plugin, CTFMap map, Location blockLocation);

    public void onPlayerMoveOnBlockTileEntity(CTFPlayer player, Location blockLocation);

    public void onRightClickBlock(CTFPlayer player, Location blockLocation);

    public String getArguments();

}
