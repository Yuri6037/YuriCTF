package fr.yurictf.extentions.event;

public interface Event {

    public void onEventStart(String addonName);
    public String getEventName();
    public Object[] getEventValues();

}
