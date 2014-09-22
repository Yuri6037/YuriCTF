package fr.yurictf.extentions.api;

import fr.yurictf.extentions.event.CTF_EVENT_REGISTERER;
import fr.yurictf.extentions.event.Event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class EventListener {

    private Map<Class<? extends Event>, Method> registeredEvents = new HashMap<Class<? extends Event>, Method>();

    public void registerAllEvents(EventListener listener, Logger log){
        Method[] m = listener.getClass().getDeclaredMethods();
        List<Method> eventMethods = new ArrayList<Method>();
        for (Method method : m){
            Annotation[] var = method.getDeclaredAnnotations();
            for (Annotation a : var){
                if (a.annotationType().equals(CTF_EVENT_REGISTERER.class)){
                    eventMethods.add(method);
                }
            }
        }
        for (Method var1 : eventMethods){
            Class<?>[] methodParams =  var1.getParameterTypes();
            for (Class<?> c : methodParams){
                Class<? super Event> var2 = (Class<? super Event>) c;
                if (var2 != null){
                    registeredEvents.put((Class<? extends Event>) c, var1);
                } else {
                    log.severe("A StoneLineEventSystem listener has tried to annotate an invalid event method");
                    log.severe("Unable to continue registry stuff for this bukkit extension : aborting");
                    return;
                }
            }
        }
    }

    public void onListenerUnload(){
    }

    public Method getEventMethod(Event event){
        return registeredEvents.get(event.getClass());
    }

    public boolean hasEvent(Event event){
        for (Map.Entry ent : registeredEvents.entrySet()){
            Class<? extends Event> c = (Class<? extends Event>) ent.getKey();
            if (c.equals(event.getClass())){
                return true;
            }
        }
        return false;
    }
}
