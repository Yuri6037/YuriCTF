package fr.yurictf.extentions.event;

import fr.yurictf.classes.CTFClass;
import fr.yurictf.server.CTFPlayer;

public class EventClassChange implements Event {

    private boolean canceled = false;

    public final CTFPlayer playerWhoChangeClass;
    public final CTFClass newPlayerClass;

    public EventClassChange(CTFPlayer p, CTFClass c){
        playerWhoChangeClass = p;
        newPlayerClass = c;
        canceled = false;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "ClassChange";
    }

    public void setCanceled(boolean b){
        canceled = b;
    }

    public Object[] getEventValues() {
        return new Object[]{canceled};
    }
}
