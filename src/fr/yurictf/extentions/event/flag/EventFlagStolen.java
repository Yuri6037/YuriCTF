package fr.yurictf.extentions.event.flag;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventFlagStolen implements Event {
    public String message = null;

    public final CTFPlayer playerWhoStole;

    public EventFlagStolen(CTFPlayer player){
        playerWhoStole = player;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "FlagStolen";
    }

    public Object[] getEventValues() {
        return new Object[]{message};
    }
}
