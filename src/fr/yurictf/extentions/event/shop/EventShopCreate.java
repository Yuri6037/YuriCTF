package fr.yurictf.extentions.event.shop;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.map.CTFMap;

public class EventShopCreate implements Event {
    private boolean canceled;

    public CTFMap theMap;

    public EventShopCreate(CTFMap map){
        theMap = map;
    }

    public void setCanceled(boolean b){
        canceled = b;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "ShopCreate";
    }

    public Object[] getEventValues() {
        return new Object[]{canceled};
    }
}
