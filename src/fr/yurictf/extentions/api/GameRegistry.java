package fr.yurictf.extentions.api;

import fr.yurictf.classes.CTFClass;
import fr.yurictf.classes.etc.EnumClasses;
import fr.yurictf.extentions.ExtentionEntry;
import fr.yurictf.language.LanguagesRegistrys;
import fr.yurictf.map.tileentity.TileEntity;
import fr.yurictf.shop.ShopItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameRegistry {
    private static final Map<ExtentionEntry, Object> registeredThings = new HashMap<ExtentionEntry, Object>();

    public static void registerCTFClass(CTFClass ctfClass, ExtentionEntry entry){
        int i = 0;
        for (CTFClass c : EnumClasses.pluginClasses){
            if (c != null){
                i++;
            }
        }
        EnumClasses.pluginClasses[i] = ctfClass;

        registeredThings.put(entry, ctfClass);
    }

    public static void deleteExtention(ExtentionEntry entry){
        Object obj;
        while((obj = registeredThings.get(entry)) != null){
            if (obj instanceof Set){
                Set<Map.Entry<String, String>> set = (Set<Map.Entry<String, String>>) obj;
                for (Map.Entry e : set){
                    LanguagesRegistrys.englishMap.remove(e.getKey());
                    LanguagesRegistrys.frenchMap.remove(e.getKey());
                }
            } else if (obj instanceof Class){
                Class<? extends TileEntity> c = (Class<? extends TileEntity>) obj;
                TileEntityAPIHandler.removeTileEntityMapping(c);
            } else if (obj instanceof ShopItem){
                ShopItem item = (ShopItem) obj;
                ShopRegistry.removeShopItemMapping(item);
            }
        }
    }

    public static void registerExtendedLanguageSet(int lang, Set<Map.Entry<String, String>> languageSet, ExtentionEntry entry){
        for (Map.Entry en : languageSet){
            String key = (String) en.getKey();
            String value = (String) en.getValue();

            switch (lang){
                case 0:
                    LanguagesRegistrys.englishMap.put(key, value);
                    break;
                case 1:
                    LanguagesRegistrys.frenchMap.put(key, value);
                    break;
            }
        }

        registeredThings.put(entry, languageSet);
    }

    public static void registerCTFTileEntity(Class<? extends TileEntity> c, ExtentionEntry entry){
        TileEntityAPIHandler.addTileEntityMapping(c);
        registeredThings.put(entry, c);
    }

    public static void registerCTFShopItem(ShopItem item, ExtentionEntry entry){
        ShopRegistry.addShopItemMapping(item);
        registeredThings.put(entry, item);
    }
}
