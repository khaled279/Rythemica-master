package com.example.Rythemica;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.lang.reflect.Type;

import static android.content.ContentValues.TAG;

public class StorageUtil {
    private final  String Storage = "com.example.Rythemica.STORAGE";
    private SharedPreferences sharedPreferences  ;
    private Context context ;
    public StorageUtil(Context context){
        this.context = context;
    }
    public void storeSong(ArrayList<Song> arrayList){
        sharedPreferences = context.getSharedPreferences(Storage, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("SongArrayList",json);
        editor.apply();
        }
     public ArrayList<Song> loadSong(){
        sharedPreferences = context.getSharedPreferences(Storage , Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("SongArrayList" , null);
        Type type = new TypeToken<ArrayList<Song>>(){}.getType() ;
        return gson.fromJson(json , type) ;
     }
     public void  storeSongIndex(int index){
        sharedPreferences = context.getSharedPreferences(Storage,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("index" , index) ;
        editor.apply();
     }
     public  int loadSongIndex(){
        sharedPreferences = context.getSharedPreferences(Storage , Context.MODE_PRIVATE);
        int index = sharedPreferences.getInt("index" , -1) ;

     return  index;}
     public  void  clearCachedSongList(){
        sharedPreferences = context.getSharedPreferences(Storage , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit() ;
        editor.clear();
        editor.commit();

     }
}

