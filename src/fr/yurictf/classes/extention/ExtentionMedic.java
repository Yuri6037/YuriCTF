package fr.yurictf.classes.extention;

import fr.yurictf.classes.CTFClass;
import fr.yurictf.server.CTFPlayer;
import fr.yurictf.YuriCTF;

public class ExtentionMedic extends Extention {

    public int healthTicks;

    public ExtentionMedic(YuriCTF plugin, CTFPlayer player, CTFClass ctfClass) {
        super(plugin, player, ctfClass);
    }

    public void resetExtention(){
        healthTicks = 0;
    }
}
