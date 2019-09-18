package com.example.Rythemica.com.rythemica.event;

import java.util.ArrayList;
import java.util.Iterator;

public class EventDispatcher implements iEventDispatcher {
    ArrayList<Listener> arrayList ;

    @Override
    public void addEventListener(iEventHandler handler , String type) {
        Listener listener = new Listener(handler , type);
        removeEventListener(type);
        arrayList.add(0,listener);
    }

    @Override
    public void removeEventListener(String type) {
        for (Iterator<Listener> iterator = arrayList.iterator() ;iterator.hasNext(); ) {
            Listener listener  =  iterator.next() ;
            if (type == listener.getType()){
                arrayList.remove(listener) ;
            }

        }
    }

    @Override
    public void dispatchEvent(Event event) {
    for (Iterator<Listener> iterator = arrayList.iterator();iterator.hasNext();)
    {
        Listener listener = (Listener) iterator.next();
        if (listener.getType() == event.getType()){
            iEventHandler eventHandler =  listener.getHandler() ;
            eventHandler.callBack(event);
        }

    }    }

    @Override
    public Boolean hasEventListener(String type) {
        return false;
    }

    @Override
    public void removeAllListeners() {

    }
}
