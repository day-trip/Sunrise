package com.daytrip.sunrise.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    private static final Map<Class<? extends Event>, List<Map.Entry<Object, Method>>> invokes = new HashMap<>();
    private static final Map<Object, Method> ignoresMethods = new HashMap<>();


    public static void initBus() {
        for(Events events : Events.values()) {
            invokes.putIfAbsent(events.getEventsClass(), new ArrayList<>());
        }
    }

    public static void registerListener(EventListener listener) {
        listeners.add(listener);
    }
    
    public static void registerListenerr(Object object) {
        //System.out.println(1);
        int i = 0;
        for(Method method : object.getClass().getMethods()) {
            System.out.println(i);
            // All code in here
            //System.out.println(2);
            if(method.getParameters().length == 1) {
                //System.out.println(3);
                if(method.isAnnotationPresent(EventHandler.class)) {
                    //System.out.println(4);
                    for(Class<? extends Event> eventClass : invokes.keySet()) {
                        //System.out.println(5);
                        if(contains(method.getParameterTypes(), eventClass)) {
                            //System.out.println(6);
                            invokes.get(eventClass).add(new AbstractMap.SimpleImmutableEntry<>(object, method));
                        }
                    }
                }
                if(method.isAnnotationPresent(EventIgnores.class) && contains(method.getParameterTypes(), Event.class) && method.getReturnType() == Boolean.class) {
                    //System.out.println(7);
                    ignoresMethods.put(object, method);
                }
            }
            i++;
        }
        System.out.println("!!!!!");
    }


    private static  <T> boolean contains(T[] arr, T toCheckValue) {
        for (T element : arr) {
            if (element == toCheckValue) {
                return true;
            }
        }
        return false;
    }
     

    public static void post(Event event) throws Exception {
        for(EventListener listener : listeners) {
            if(listener.ignore(event)) {
                continue;
            }
            listener.onEvent(event);
            if(event.isCancelled()) {
                break;
            }
        }
    }

    public static void postt(Event event) throws Exception {
        System.out.println(event.getClass().getSimpleName());
        for(Map.Entry<Object, Method> methodEntry : invokes.get(event.getClass())) {
            if(ignoresMethods.containsKey(methodEntry.getKey())) {
                boolean b = (boolean) ignoresMethods.get(methodEntry.getKey()).invoke(methodEntry.getKey(), event);
                if(b) continue;
            }
            methodEntry.getValue().invoke(methodEntry.getKey(), event);
            if(event.isCancelled()) {
                break;
            }
        }
    }
}
