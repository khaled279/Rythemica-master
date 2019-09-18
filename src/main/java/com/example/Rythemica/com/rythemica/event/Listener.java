package com.example.Rythemica.com.rythemica.event;

public class Listener {
     public  String type ;
   public iEventHandler handler ;
   public Listener(iEventHandler handler ,String type){
        this.handler = handler ;
        this.type = type ;
    }

    public iEventHandler getHandler() {
        return handler;
    }

    public String getType() {
        return type;
    }
}
