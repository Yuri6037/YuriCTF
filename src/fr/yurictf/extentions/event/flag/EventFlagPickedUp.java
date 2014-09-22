package fr.yurictf.extentions.event.flag;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventFlagPickedUp implements Event {
    public String message = null;

    public final CTFPlayer playerWhoPickedUp;

    public EventFlagPickedUp(CTFPlayer player){
        playerWhoPickedUp = player;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "FlagPickedUp";
    }

    public Object[] getEventValues() {
        return new Object[]{message};
    }
}
