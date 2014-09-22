package fr.yurictf.extentions.event.flag;

import fr.yurictf.YuriCTF;
import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventFlagReset implements Event {
    public String message = null;

    public final CTFPlayer playerWhoEngagedReset;
    public final YuriCTF thePlugin;

    public EventFlagReset(CTFPlayer player, YuriCTF plugin){
        playerWhoEngagedReset = player;
        thePlugin = plugin;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "FlagReset";
    }

    public Object[] getEventValues() {
        return new Object[]{message};
    }
}
