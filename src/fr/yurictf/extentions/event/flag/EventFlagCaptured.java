package fr.yurictf.extentions.event.flag;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventFlagCaptured implements Event {

    public String message = null;

    public final CTFPlayer playerWhoCaptured;

    public EventFlagCaptured(CTFPlayer player){
        playerWhoCaptured = player;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "FlagCaptured";
    }

    public Object[] getEventValues() {
        return new Object[]{message};
    }
}
