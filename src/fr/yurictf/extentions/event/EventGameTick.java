package fr.yurictf.extentions.event;

import fr.yurictf.YuriCTF;

public class EventGameTick implements Event {

    public YuriCTF thePlugin;

    public EventGameTick(YuriCTF plugin){
        thePlugin = plugin;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "GameTick";
    }

    public Object[] getEventValues() {
        return new Object[]{};
    }
}
