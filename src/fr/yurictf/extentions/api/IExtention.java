package fr.yurictf.extentions.api;

import fr.yurictf.extentions.ExtentionEntry;

import java.util.logging.Logger;

public interface IExtention {

    public void intialize(Logger log, ExtentionEntry entry);
    public void onExtentionLoad();
    public void onExtentionUnLoad();

}
