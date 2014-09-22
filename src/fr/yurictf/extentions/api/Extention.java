package fr.yurictf.extentions.api;

import fr.yurictf.extentions.ExtentionEntry;

import java.util.logging.Logger;

public abstract class Extention implements IExtention {
    public abstract void intialize(Logger log, ExtentionEntry entry);
    public abstract void onExtentionLoad();
    public abstract void onExtentionUnLoad();
}
