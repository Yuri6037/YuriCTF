package fr.yurictf.classes.etc;

import fr.yurictf.classes.*;
import fr.yurictf.server.CTFPlayer;

import java.util.ArrayList;
import java.util.List;

public class EnumClasses {
    public static CTFClass[] pluginClasses;

    public static CTFClass getDefaultClass(boolean editing){
        for (CTFClass ctfClass : pluginClasses){
            if (ctfClass != null && ctfClass.isDefaultClass(editing)){
                return ctfClass;
            }
        }
        return null;
    }

    public static CTFClass getCorrespondingClass(String name){
        for (CTFClass ctfClass : pluginClasses){
            if (ctfClass != null && ctfClass.getClassName().equalsIgnoreCase(name)){
                return ctfClass;
            }
        }
        return null;
    }

    public static List<String> getAllClassesHelp(CTFPlayer player){
        List<String> s = new ArrayList<String>();
        for (CTFClass ctfClass : pluginClasses){
            if (ctfClass != null && ctfClass.canAccessClass(player.thePlugin, player)){
                if (ctfClass.getClassDescription(player) != null){
                    s.add(ctfClass.getClassName() + " -> " + ctfClass.getClassDescription(player));
                } else {
                    s.add(ctfClass.getClassName() + " -> " + "ERROR_NO_CLASS_DESCRIPTION");
                }
            }
        }
        return s;
    }

    static {
        pluginClasses = new CTFClass[16];
        pluginClasses[0] = new CTFClassHeavy();
        pluginClasses[1] = new CTFClassMedic();
        pluginClasses[2] = new CTFClassSoldier();
        pluginClasses[3] = new CTFClassBuilder();
    }
}
