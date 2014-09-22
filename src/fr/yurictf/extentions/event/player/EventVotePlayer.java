package fr.yurictf.extentions.event.player;

import fr.yurictf.extentions.event.Event;
import fr.yurictf.server.CTFPlayer;

public class EventVotePlayer implements Event {

    public final CTFPlayer playerWhoVote;

    public EventVotePlayer(CTFPlayer p){
        playerWhoVote = p;
    }

    public void onEventStart(String addonName) {
    }

    public String getEventName() {
        return "VotePlayer";
    }

    public Object[] getEventValues() {
        return new Object[]{};
    }
}
