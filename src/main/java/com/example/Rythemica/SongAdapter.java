package com.example.Rythemica;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
   // MediaPlayer player = new MediaPlayer();
   public  static  final String Broadcast_PLAY_NEW_AUDIO = "com.Rythemica.audioplayer.PlayNewAudio" ;
    TextView songTxt;
    TextView artistTxt;
    ImageView img ;
    ArrayList<Song> arrayList;
    Context context ;
    String hold ;
    MediaPlayerServices ms = new MediaPlayerServices() ;
    PlaybackStatus playbackStatus = PlaybackStatus.PAUSED ;
    ListView ls ;
    boolean isPlaying = false ;
    Song current ;
    String serialed ;
    String Serialed2 ;
    Boolean isBound = false ;
    Bound bound = Bound.NOTBOUND;
    int keepPosition = -1 ;
    private final String changeToPlay = "com.Rythemica.mainActivity_changeToPly" ;
    private final String changeToPause = "com.Rythemica.mainActivity_changeToPause" ;
    public  static final String ACTION_PAUSE= "com.Rythemica.ACTION_PAUSE_SONGADAPTER";
    public static final  String ACTION_RESUME = "com.Rythemica.ACTION_RESUME_SONGADAPTER";
    public SongAdapter(ArrayList<Song> arr, Context context) {
        this.arrayList = arr;
        hold = arrayList.get(0).getPath() ;
        this.context = context;
    }
 /*   @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Song getItem(int c) {
        return arrayList.get(c);
    }*/

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item , viewGroup , false)
        ) ;
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder viewHolder,  int i) {
        Song currentSong = arrayList.get(i);
            viewHolder.songTxt.setText(currentSong.getName());
            viewHolder.artistTxt.setText(currentSong.getSongArtist());
             final long position = getItemId(i) ;
        Glide.with(context).load(currentSong.getSongImg()).apply(RequestOptions.circleCropTransform()).into(viewHolder.img) ;
            viewHolder.viewParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    keepPosition = new StorageUtil(context).loadSongIndex() ;
                    if (keepPosition == position&& playbackStatus == PlaybackStatus.PLAYING){
                        Intent pauseIntent = new Intent(ACTION_PAUSE) ;
                        context.sendBroadcast(pauseIntent);
                        playbackStatus =   PlaybackStatus.PAUSED;
                        context.sendBroadcast(new Intent(changeToPause));
                    }
                    else if(keepPosition != position){
                        playAudio((int) position);
                        keepPosition =(int) position ;
                        playbackStatus = PlaybackStatus.PLAYING ;
                        Intent intent = new Intent(context , InPlay.class);
                        context.startActivity(intent);
                        context.sendBroadcast(new Intent(changeToPlay));
                    }else if(keepPosition == position && playbackStatus == PlaybackStatus.PAUSED) {
                        Intent resumeAudioIntent = new Intent(ACTION_RESUME);
                        context.sendBroadcast(resumeAudioIntent);
                        playbackStatus = PlaybackStatus.PLAYING;
                        context.sendBroadcast(new Intent(changeToPlay));
                        Intent intent  = new Intent(context , InPlay.class);
                        context.startActivity(intent);
                } }


    });}

    @Override
    public long getItemId(int c) {
        return c;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

  /* @Override
        public View getView(int c, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        Song currentSong = getItem(c);
        songTxt = view.findViewById(R.id.songName);
        ls = view.findViewById(R.id.listView) ;
//        ls.setOnItemClickListener(clickListener);
        artistTxt = view.findViewById(R.id.artistName);
        img = view.findViewById(R.id.art) ;
        songTxt.setText(currentSong.getName());
        artistTxt.setText(currentSong.getSongArtist());

      //  Picasso.with(context).load(currentSong.getSongImg()).transform(new CropCircleTransformation()).fit().into(img);
        Glide.with(context).load(currentSong.getSongImg()).apply(RequestOptions.circleCropTransform()).into(img) ;
        Log.d("tmam", "getView: "+currentSong.getSongImg());

        return view;
    }


               Song getSong(int num){
               return arrayList.get(num);
    }*/

        private ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerServices.LocalBinder binder = (MediaPlayerServices.LocalBinder) service ;
                ms = binder.getService();
                isBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound= false;

            }
        };
    public   void playAudio(int index){
        if (bound == Bound.NOTBOUND){
            StorageUtil storageUtil = new StorageUtil(context);
            storageUtil.storeSong(arrayList);
            storageUtil.storeSongIndex(index);
            Intent intent = new Intent(context, MediaPlayerServices.class);
            context.startService(intent) ;
            context.sendBroadcast(new Intent(MainActivity.ACTION_BOUND));
            bound = Bound.BOUND;

        }else {
            Intent intent = new Intent(context , MediaPlayerServices.class);
            context.bindService(intent , serviceConnection , Context.BIND_AUTO_CREATE) ;
            StorageUtil storageUtil = new StorageUtil(context);
            storageUtil.storeSongIndex(index);
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            context.sendBroadcast(broadcastIntent);
            context.sendBroadcast(broadcastIntent);
        }
    }
        BroadcastReceiver boundreceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    bound =Bound.BOUND ;
            }
        };
            private void registerBoundreceiver() {
                IntentFilter intentFilter = new IntentFilter(MainActivity.ACTION_BOUND);
                context.registerReceiver(boundreceiver , intentFilter);
            }
 /*
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            keepPosition = new StorageUtil(context).loadSongIndex() ;
            if (keepPosition == position&& playbackStatus == PlaybackStatus.PLAYING){
                Intent pauseIntent = new Intent(ACTION_PAUSE) ;
                context.sendBroadcast(pauseIntent);
              playbackStatus =   PlaybackStatus.PAUSED;
              context.sendBroadcast(new Intent(changeToPause));
            }
            else if(keepPosition != position){
                playAudio(position);
                keepPosition = position ;
                playbackStatus = PlaybackStatus.PLAYING ;
                Intent intent = new Intent(context , InPlay.class);
                context.startActivity(intent);
                context.sendBroadcast(new Intent(changeToPlay));
            }else if(keepPosition == position && playbackStatus == PlaybackStatus.PAUSED) {
                Intent resumeAudioIntent = new Intent(ACTION_RESUME);
                context.sendBroadcast(resumeAudioIntent);
                playbackStatus = PlaybackStatus.PLAYING;
                context.sendBroadcast(new Intent(changeToPlay));
                Intent intent  = new Intent(context , InPlay.class);
            }
        }
     */

       public  static class ViewHolder extends RecyclerView.ViewHolder{
           private View viewParent ;
           private TextView songTxt ;
           private  TextView artistTxt ;
           private ImageView img ;
            public ViewHolder(@NonNull View itemView) {
             super(itemView);
                this.viewParent = itemView ;
               songTxt =  itemView.findViewById(R.id.songName);
               artistTxt = itemView.findViewById(R.id.artistName) ;
               img = itemView.findViewById(R.id.art)
                      ;



            }
        }
}





