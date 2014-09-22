package fr.yurictf.extentions.event.shop;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventShopGuiOpen implements Event {
    private boolean canceled;

    public CTFPlayer player;

    public EventShopGuiOpen(CTFPlayer p){
        player = p;
    }

    public void setCanceled(boolean b){
        canceled = b;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "ShopGuiOpen";
    }

    public Object[] getEventValues() {
        return new Object[]{canceled};
    }
}
