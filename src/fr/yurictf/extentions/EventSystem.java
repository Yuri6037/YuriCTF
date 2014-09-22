package fr.yurictf.extentions;

import fr.yurictf.YuriCTF;
import fr.yurictf.extentions.api.EventListener;
import fr.yurictf.extentions.api.Extention;
import fr.yurictf.extentions.event.Event;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class EventSystem {

    private static YuriCTF thePlugin;

    private static Map<String, EventListener> listeners;

    public static void tryToSetUpAndStartEventSystem(YuriCTF plugin){
        listeners = new HashMap<String, EventListener>();
        thePlugin = plugin;
    }

    public static Map<String, Object[]> callEvent(Event eventName){
        Map<String, Object[]> var = new HashMap<String, Object[]>();
        for (Map.Entry e : listeners.entrySet()){
            EventListener listener = (EventListener) e.getValue();
            if (listener.hasEvent(eventName)){
                eventName.onEventStart((String) e.getKey());
                Method m = listener.getEventMethod(eventName);
                try {
                    m.invoke(listener, eventName);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                var.put((String) e.getKey(), eventName.getEventValues());
            }
        }
        return var;
    }

    public static void unregisterEventListener(ExtentionEntry ext){
        EventListener listener = listeners.get(ext.extID);
        if (listener != null){
            listener.onListenerUnload();
            listeners.remove(ext.extID);
        }
    }

    public static void registerEventListener(ExtentionEntry ext, EventListener listener){
        listener.registerAllEvents(listener, thePlugin.getLogger());
        listeners.put(ext.extID, listener);
    }
}
