package fr.yurictf.extentions;

import fr.yurictf.extentions.api.Extention;

import java.util.logging.Logger;

public class ExtentionEntry {
    public final String extVersion;
    public final String extID;
    public final String extAuthor;
    public final Extention theExtentionClass;
    public final String extDescription;

    protected ExtentionEntry(String a, String b, String c, Extention d, String e){
        extID = a;
        extAuthor = b;
        extVersion = c;
        theExtentionClass = d;
        extDescription = e;
    }

    public void load(Logger log){
        theExtentionClass.intialize(log, this);
    }
}
