package fr.yurictf.map.tileentity;

import fr.yurictf.map.CTFMap;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.YuriCTF;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TileEntityBreakableBlock implements TileEntity{

    public Material blockMaterial;
    private CTFPlayer player;
    private Location loc;

    public TileEntityBreakableBlock(CTFPlayer player, Material material){
        blockMaterial = material;
        this.player = player;
    }

    public TileEntityBreakableBlock(YuriCTF plugin, String args){
            Material m = Material.getMaterial(args);
            blockMaterial = m;
            player = null;
    }

    public void onUpdate(YuriCTF plugin, CTFMap map) {
        if (player != null && !player.associatedPlayer.isOnline()){
            player.thePlugin.ctfMap.removeBlockTileEntity(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), null, true);
        }
    }

    public void onBlockDestroy(YuriCTF plugin, CTFMap map, Location blockLocation) {
        Block block = blockLocation.getWorld().getBlockAt(blockLocation);
        block.setType(Material.AIR);
    }

    public void onBlockPlaced(YuriCTF plugin, CTFMap map, Location blockLocation) {
        Block block = blockLocation.getWorld().getBlockAt(blockLocation);
        block.setType(blockMaterial);
        loc = blockLocation;
    }

    public void onPlayerMoveOnBlockTileEntity(CTFPlayer player, Location blockLocation) {
    }

    public void onRightClickBlock(CTFPlayer player, Location blockLocation) {
    }

    public String getArguments() {
        return blockMaterial.name();
    }
}
