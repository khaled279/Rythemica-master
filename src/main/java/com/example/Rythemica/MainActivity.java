package com.example.Rythemica;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.Rythemica.com.rythemica.event.Listener;
import com.example.myapplication.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{
    ArrayList<Song> arrList;
    public  static  final String Broadcast_PLAY_NEW_AUDIO = "com.Rythemica.audioplayer.PlayNewAudio" ;
    ListView list;
    ImageButton btn1;
    ImageButton btn2;
    ImageButton btn3;
    SongAdapter songAdapter;
    MediaPlayerServices mediaPlayerServices;
    Bound bound = Bound.NOTBOUND ;
    long backTime ;
    Toast toast  ;
    PlaybackStatus playbackStatus = PlaybackStatus.PAUSED ;
    private final static int MY_PERMISSION_REQUAEST = 1;
    private final String changeToPlay = "com.Rythemica.mainActivity_changeToPly" ;
    private final String changeToPause = "com.Rythemica.mainActivity_changeToPause" ;
    public  static final String ACTION_PAUSE= "com.Rythemica.ACTION_PAUSE_SONGADAPTER";
    public static final  String ACTION_RESUME = "com.Rythemica.ACTION_RESUME_SONGADAPTER";
    public  static  final  String ACTION_NEXT = "com.Rythemica.ACTION_NEXT_MAINACTIVITY" ;
    public  static  final  String ACTION_PREVIOUS = "com.Rythemic.ACTION_PREVIOUS_MAINACTIVITY" ;
    public static final  String  ACTION_BOUND = "com.Rythemica.ACTION_BOUND_MAINACTIVITY" ;
    public static final  String  ACTION_NotBOUND = "com.Rythemica.ACTION_NotBOUND_MAINACTIVITY" ;

    public BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //hide the title bar
        register_pausing();
        register_playing();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                {Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUAEST);
                //************************************************************************************************************************
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                {Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUAEST);

            }


        } else {
            new doTheStuff().execute();
        }
        View.OnClickListener onPlayClicked = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              if (playbackStatus == PlaybackStatus.PAUSED) {
                  sendBroadcast(new Intent(SongAdapter.ACTION_RESUME));
                  playbackStatus = PlaybackStatus.PLAYING ;
                  changeBtnIconToPause();
              }else if (playbackStatus == PlaybackStatus.PLAYING) {
                  sendBroadcast(new Intent(SongAdapter.ACTION_PAUSE));
                  playbackStatus = PlaybackStatus.PAUSED ;
                  changeBtnIconToply();
              }else if(playbackStatus == PlaybackStatus.PAUSED && bound == Bound.NOTBOUND){
                  playAudio(new StorageUtil(getApplicationContext()).loadSongIndex());
              }
            }
        };

        View.OnClickListener onNextClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(ACTION_NEXT));
                changeBtnIconToPause();
            }
        };
        View.OnClickListener onPreviousClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(ACTION_PREVIOUS));
                changeBtnIconToPause();
            }
        };
        btn1 = findViewById(R.id.play);
        btn2= findViewById(R.id.next);
        btn3 = findViewById(R.id.nexton);
        btn1.setOnClickListener(onPlayClicked);
        btn2.setOnClickListener(onNextClicked);
        btn3.setOnClickListener(onPreviousClicked);
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
       if (backTime+2000 > System.currentTimeMillis()){
           Intent i = new Intent();
           i.setAction(Intent.ACTION_MAIN);
           i.addCategory(Intent.CATEGORY_HOME);
           this.startActivity(i);
       }else {
                toast = Toast.makeText(getApplicationContext(),"press back again to push app to background" , Toast.LENGTH_SHORT) ;
                toast.show();

       }
       backTime = System.currentTimeMillis();
    }

    private BroadcastReceiver playbackStatus_isPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playbackStatus = PlaybackStatus.PLAYING ;
            changeBtnIconToPause();
        }
    };
    private BroadcastReceiver playbackStatus_isPaused  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                playbackStatus = PlaybackStatus.PAUSED  ;
                changeBtnIconToply();
        }
    };
    private void register_playing(){
        IntentFilter intentFilter = new IntentFilter(changeToPlay);
        registerReceiver(playbackStatus_isPlaying,intentFilter);
    }
    private void register_pausing()
    {
        IntentFilter intentFilter = new IntentFilter(changeToPause) ;
        registerReceiver(playbackStatus_isPaused , intentFilter);
    }
    public   void playAudio(int index){
        if (bound == Bound.NOTBOUND){
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            storageUtil.storeSong(arrList);
            storageUtil.storeSongIndex(index);

            Intent intent = new Intent(MainActivity.this , MediaPlayerServices.class);

            startService(intent) ;
            bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        }else {
            StorageUtil storageUtil = new StorageUtil(getApplicationContext());
            storageUtil.storeSongIndex(index);
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }

    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerServices.LocalBinder binder = (MediaPlayerServices.LocalBinder) service;
            mediaPlayerServices = binder.getService();
          bound =   Bound.BOUND ;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                bound = Bound.NOTBOUND ;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(serviceConnection);
        Log.d("tmam", "onDestroy: ama etnadht ");
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playbackStatus_isPaused);
        unregisterReceiver(playbackStatus_isPlaying);
    }

    public void doTheThing(final ArrayList<Song> arr) {

        list = findViewById(R.id.listView);

        final Context context = getApplicationContext();
        songAdapter = new SongAdapter(arr, context);
        list.setAdapter(songAdapter);
        list.setOnItemClickListener(songAdapter.clickListener);
       // songAdapter.ms.v = findViewById(R.id.play);
    }

    public ArrayList<Song> getMusicInfo() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri imgUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor mCursor = contentResolver.query(songUri, null, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            int songTitle = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistTitle = mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int duration = mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumCover = mCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            arrList = new ArrayList<Song>();
            do {

                String songName = mCursor.getString(songTitle);
                String artistName = mCursor.getString(artistTitle);
                String songDuration = mCursor.getString(duration);
                int intpath = mCursor.getColumnIndex(MediaStore.Audio.Media
                        .DATA);
                String path = mCursor.getString(intpath);
                Uri uri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(uri, mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                if (uri != null) {
                    Song song = new Song(songName, artistName, path, albumArt);
                    arrList.add(song);
                    song.setAlmub_id(mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                } else {
                    Song song = new Song(songName, artistName, path, null);
                    arrList.add(song);
                }


            }
            while (mCursor.moveToNext());
        }


        return arrList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUAEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        new doTheStuff().execute();
                    }

                } else {
                    Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }

    }

    class doTheStuff extends AsyncTask<String, String[], ArrayList<Song>> {

        @Override
        protected ArrayList<Song> doInBackground(String... strings) {
            return getMusicInfo();
        }

        @Override
        protected void onPostExecute(ArrayList<Song> strings) {
            if (strings != null) {
                doTheThing(strings);
                if (strings.size() > 190) {
                    Log.d("MyActivity", "yes it's");
                }

            } else Toast.makeText(MainActivity.this, "Strings is empty", Toast.LENGTH_LONG).show();
        }
    }

    private void changeBtnIconToPause() {
        btn1 = (ImageButton) findViewById(R.id.play);
        btn1.setImageResource(R.drawable.circle_pause);
    }

    private void changeBtnIconToply() {
        btn1 = (ImageButton) findViewById(R.id.play);
        btn1.setImageResource(R.drawable.circle_play);
    }



}
