package fr.yurictf.extentions.event.player;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventPlayerDeath implements Event {

    public final CTFPlayer deadPlayer;
    public final CTFPlayer killingPlayer;

    public EventPlayerDeath(CTFPlayer p, CTFPlayer p1){
        deadPlayer = p;
        killingPlayer = p1;
    }

    public void onEventStart(String addonName) {
    }


    public String getEventName() {
        return "PlayerDeath";
    }

    public Object[] getEventValues() {
        return new Object[]{};
    }
}
