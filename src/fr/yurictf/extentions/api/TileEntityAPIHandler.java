package fr.yurictf.extentions.api;

import fr.yurictf.map.tileentity.TileEntity;

import java.util.HashMap;
import java.util.Map;

public class TileEntityAPIHandler {
    private static Map<String, Class<? extends TileEntity>> apiTileEntityMap;

    protected static void addTileEntityMapping(Class<? extends TileEntity> c){
        String name = c.getSimpleName();
        apiTileEntityMap.put(name, c);
    }

    protected static void removeTileEntityMapping(Class<? extends TileEntity> c){
        String name = c.getSimpleName();
        apiTileEntityMap.remove(name);
    }

    public static Class<? extends TileEntity> findTileEntityFromExtentions(String className){
        return apiTileEntityMap.get(className);
    }

    static {
        apiTileEntityMap = new HashMap<String, Class<? extends TileEntity>>(16);
    }
}
