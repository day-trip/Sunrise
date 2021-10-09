package com.daytrip.shared.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    //private static final Map<Class<? extends Event>, List<EventListenerInstance>> invokes = new HashMap<>();


    /*
    public static void initBus() {
        Reflections reflections = new Reflections("com.daytrip.shared.event.impl");
        for(Class<? extends Event> event : reflections.getSubTypesOf(Event.class)) {
            invokes.put(event, new ArrayList<>());
            Minecraft.logger.info(event);
        }
    }

     */

    public static void registerListener(EventListener listener) {
        listeners.add(listener);
    }

    /*
    public static void registerListener(Object listener) {
        for(Method method : listener.getClass().getDeclaredMethods()) {
            for(Class<? extends Event> event : invokes.keySet()) {
                if(method.getParameterTypes().length == 1 && contains(method.getParameterTypes(), event)) {
                    EventListenerInstance instance = new EventListenerInstance();
                    instance.object = listener;
                    instance.method = method;
                    invokes.get(event).add(instance);
                }
            }
        }
    }

     */

    /*
    private static  <T> boolean contains(T[] arr, T toCheckValue) {
        for (T element : arr) {
            if (element == toCheckValue) {
                return true;
            }
        }
        return false;
    }

     */

    public static void post(Event event) throws Exception {
        for(EventListener listener : listeners) {
            if(listener.ignore(event) || event.isExcluded(listener.getListenerName())) {
                continue;
            }
            listener.onEvent(event);
            if(event.isCancelled()) {
                break;
            }
        }
    }

    /*
    public static void post(Event event) throws Exception {
        for(EventListenerInstance instance : invokes.get(event.getClass())) {
            instance.method.invoke(instance.object, event);
        }
    }

     */
}
