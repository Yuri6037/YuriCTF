package fr.yurictf.extentions.event.flag;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventFlagDropped implements Event {
    public String message = null;

    public final CTFPlayer playerWhoDropped;

    public EventFlagDropped(CTFPlayer player){
        playerWhoDropped = player;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "FlagDropped";
    }

    public Object[] getEventValues() {
        return new Object[]{message};
    }
}
