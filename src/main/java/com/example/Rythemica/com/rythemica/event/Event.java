package com.example.Rythemica.com.rythemica.event;

public class Event {
        String type ;
        public  final String COMPLETE = "complete" ;
        public Event(String type){
            setType(type) ;
        }
    public void setType(String type) {
            this.type = type ;
    }
    public String getType(){
            return this.type ;
    }

}

