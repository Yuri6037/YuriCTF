package fr.yurictf.votifier;

import fr.yurictf.YuriCTF;

import java.util.HashMap;
import java.util.Map;

public class VotifierManager {

    public boolean isVotifierInstalled;

    protected YuriCTF thePlugin;

    private static VotifierManager instance;

    public VotifierManager(YuriCTF plugin){
        thePlugin = plugin;
        instance = this;
    }

    public static VotifierManager getInstance(){
        return instance;
    }
}
