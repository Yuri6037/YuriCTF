package fr.yurictf.extentions.api;

import fr.yurictf.shop.ShopItem;

import java.util.HashMap;
import java.util.Map;

public class ShopRegistry {

    private static Map<Integer, ShopItem> items;

    public static ShopItem getItemByID(int id){
        return items.get(id);
    }

    public static int getItemsCount(){
        return items.size();
    }

    protected static void addShopItemMapping(ShopItem item){
        int id = items.size();
        items.put(id, item);
    }

    protected static void removeShopItemMapping(ShopItem item){
        ShopItem si = null;
        for (Map.Entry e : items.entrySet()){
            if (e.getValue().equals(item)){
                si = (ShopItem) e.getKey();
            }
        }
        items.remove(si);
    }

    static {
        items = new HashMap<Integer, ShopItem>(27);
        //items.put(0, new ShopItemTest());
    }
}
