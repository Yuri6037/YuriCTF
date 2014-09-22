package fr.yurictf.votifier;

import com.vexsoftware.votifier.model.VotifierEvent;
import fr.yurictf.extentions.EventSystem;
import fr.yurictf.extentions.event.player.EventVotePlayer;
import fr.yurictf.server.CTFPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public class VotifierHandler implements Listener {

    @EventHandler
    public void onVote(VotifierEvent event){
        CTFPlayer player = VotifierManager.getInstance().thePlugin.playerList.get(event.getVote().getUsername());
        if (player != null){
            player.playerVoteSystem.votePoints++;
            EventSystem.callEvent(new EventVotePlayer(player));
        }
    }
}
