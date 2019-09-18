package com.example.Rythemica.com.rythemica.event;

public interface iEventDispatcher {
 void addEventListener(iEventHandler handler , String type) ;
 void removeEventListener(String type) ;
 void dispatchEvent(Event event) ;
 Boolean hasEventListener(String type) ;
 void removeAllListeners() ;
}
