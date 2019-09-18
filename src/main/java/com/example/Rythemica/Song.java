package com.example.Rythemica;

import android.net.Uri;
import java.io.Serializable;
import java.net.URI;

public class Song implements Serializable {
    String songname ;
    String songArtist ;
    String duration ;
    Long album_id;
   transient Uri imgPath ;
    Song(String song , String artist , String path, Uri imgPath ){
        this.songname = song ;
        this.songArtist = artist ;
        this.duration = path;
        this.imgPath = imgPath;
    }

        String getName(){return songname;}
        String getSongArtist(){return songArtist;}
        String getPath(){return duration;}
        Uri getSongImg(){return imgPath;}
        public void setAlmub_id(Long albumId){
            album_id = albumId ;
        }

    public Long getAlbum_id() {
        return album_id;
    }
}
