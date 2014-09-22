package fr.yurictf.extentions.event.shop;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.shop.ShopItem;

public class EventShopItemPurshased implements Event{

    private boolean canceled;
    private ShopItem theItem;
    private CTFPlayer thePlayer;

    public EventShopItemPurshased(ShopItem item, CTFPlayer p){
        theItem = item;
        thePlayer = p;
    }

    public void setCanceled(boolean b){
        canceled = b;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "ShopItemPurchase";
    }

    public Object[] getEventValues() {
        return new Object[]{canceled};
    }
}
