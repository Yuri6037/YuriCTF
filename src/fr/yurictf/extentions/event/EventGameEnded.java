package fr.yurictf.extentions.event;

import fr.yurictf.YuriCTF;

public class EventGameEnded implements Event {

    public final YuriCTF thePlugin;

    public EventGameEnded(YuriCTF plugin){
        thePlugin = plugin;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "GameEnded";
    }

    public Object[] getEventValues() {
        return new Object[]{};
    }
}
