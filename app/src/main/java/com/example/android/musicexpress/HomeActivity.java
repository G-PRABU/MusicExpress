package com.example.android.musicexpress;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int currentPosition;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if(mediaPlayer != null){
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if(isAvailable()){
                currentPosition++;
            } else {
                currentPosition = 0;
            }
            playMusic();
        }
    };
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){
                mediaPlayer.pause();
            } else if(focusChange == AudioManager.AUDIOFOCUS_GAIN){
                mediaPlayer.start();
            } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS){
                releaseMediaPlayer();
            }
        }
    };
    private BottomSheetBehavior songBottomControls;
    private ContentResolver contentResolver;
    private Uri musicUri;
    private ConstraintLayout songControlLayouts;
    private static final int RUNTIME_PERMISSION_CODE = 7;
    boolean isMusicStarted = false;
    boolean hasPermissionToAccessStorage = false;
    ArrayList<Song> songs;
    ListView songList;
    MusicAdapter musicAdapter;
    Button accessPermissionButton;
    LinearLayout accessPermissionLayout;
    TextView emptyTextView;
    TextView currentSongName;
    TextView currentSongArtist;
    ImageButton topPlayButton;
    ImageButton bottomPlayButton;
    ImageButton nextButton;
    ImageButton previousButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        emptyTextView = (TextView)findViewById(R.id.empty_list_tv);
        accessPermissionLayout = (LinearLayout)findViewById(R.id.access_permission_layout);
        accessPermissionButton = (Button)findViewById(R.id.access_permission_button);
        currentSongName = (TextView)findViewById(R.id.current_song_name);
        currentSongArtist = (TextView)findViewById(R.id.current_song_artist);
        topPlayButton = (ImageButton)findViewById(R.id.play_pause_button);
        bottomPlayButton = (ImageButton)findViewById(R.id.play_button);
        nextButton = (ImageButton)findViewById(R.id.next_button);
        previousButton = (ImageButton)findViewById(R.id.previous_button);
        musicUri =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        songList = (ListView)findViewById(R.id.song_list);
        songs = new ArrayList<Song>();
        songControlLayouts = (ConstraintLayout) findViewById(R.id.song_controls);
        songBottomControls = BottomSheetBehavior.from(songControlLayouts);
        songBottomControls.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState){
                    case BottomSheetBehavior.STATE_EXPANDED:
                        topPlayButton.setVisibility(View.INVISIBLE);
                        songList.setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        topPlayButton.setVisibility(View.VISIBLE);
                        songList.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        songList.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        runTimePermissionToAccessStorage();
        if(hasPermissionToAccessStorage){
            setSongs();
        } else {
            accessPermissionRequest();
        }
        accessPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTimePermissionToAccessStorage();
                if(hasPermissionToAccessStorage){
                    setSongs();
                } else {
                    accessPermissionRequest();
                }
            }
        });
        topPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        bottomPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    releaseMediaPlayer();
                }
                if(isAvailable()){
                    currentPosition++;
                } else {
                    currentPosition = 0;
                }
                playMusic();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mediaPlayer != null) {
                   mediaPlayer.stop();
                   releaseMediaPlayer();
               }
                if(currentPosition > 0){
                    currentPosition--;
                } else {
                   currentPosition = songs.size()-1;
                }
                playMusic();
            }
        });
        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                currentPosition = position;
                playMusic();

            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void setSongs() {
        getAllSongs();
        if(currentPosition >= 0) {
            songControlLayouts.setVisibility(View.VISIBLE);
            currentSongName.setText(songs.get(currentPosition).getSongName());
            currentSongArtist.setText(songs.get(currentPosition).getArtistName());
            songList.setVisibility(View.VISIBLE);
            accessPermissionLayout.setVisibility(View.INVISIBLE);
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
            songControlLayouts.setVisibility(View.INVISIBLE);
        }
    }

    public void accessPermissionRequest(){
        songList.setVisibility(View.INVISIBLE);
        accessPermissionLayout.setVisibility(View.VISIBLE);
        songControlLayouts.setVisibility(View.INVISIBLE);
    }

    public void pausePlay(){
        if(!isMusicStarted && mediaPlayer == null){
            playMusic();
            isMusicStarted = true;
        } else {
            if(mediaPlayer.isPlaying()){
                topPlayButton.setImageResource(R.drawable.ic_play);
                bottomPlayButton.setImageResource(R.drawable.ic_play_circle);
                mediaPlayer.pause();
            } else {
                topPlayButton.setImageResource(R.drawable.ic_pause);
                bottomPlayButton.setImageResource(R.drawable.ic_pause_circle);
                mediaPlayer.start();
            }
        }
    }

    public boolean isAvailable(){
        return songs.size()-1>currentPosition;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_albums) {
            if(hasPermissionToAccessStorage) {
                if(currentPosition >= 0) {
                    Intent albumActivity = new Intent(HomeActivity.this, AlbumsActivity.class);
                    releaseMediaPlayer();
                    topPlayButton.setImageResource(R.drawable.ic_play);
                    startActivity(albumActivity);
                } else {
                    Toast.makeText(this,getString(R.string.empty_list_home),Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this,getString(R.string.permission_toast),Toast.LENGTH_LONG).show();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void runTimePermissionToAccessStorage(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(HomeActivity.this);
                    alert_builder.setMessage(getString(R.string.permission_message));
                    alert_builder.setTitle(getString(R.string.permission_title));
                    alert_builder.setPositiveButton(getString(R.string.permission_positive), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    HomeActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RUNTIME_PERMISSION_CODE
                            );
                        }
                    });
                    alert_builder.setNeutralButton(getString(R.string.permission_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hasPermissionToAccessStorage = false;
                        }
                    });
                    AlertDialog dialog = alert_builder.create();
                    dialog.show();
                } else {
                    ActivityCompat.requestPermissions(
                            HomeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            RUNTIME_PERMISSION_CODE
                    );
                }
            } else {
                hasPermissionToAccessStorage = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case RUNTIME_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    setSongs();
                    hasPermissionToAccessStorage = true;
                }
                else {
                    accessPermissionRequest();
                    hasPermissionToAccessStorage = false;
                }
            }
        }
    }

    public String changeTimeFormat(int durationMilli){
        String minutes = String.valueOf((durationMilli/1000)/60);
        String seconds;
        if((durationMilli/1000)%60 < 10){
            seconds = "0" + String.valueOf((durationMilli/1000)%60);
        } else {
            seconds = String.valueOf((durationMilli/1000)%60);
        }
        return minutes +":"+seconds;
    }

    public void getAllSongs(){
        contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(musicUri,null,null,null,null);
        if(cursor != null && cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistNameColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songDurationColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songPathColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songIdColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            do {
                String name = cursor.getString(nameColumnIndex);
                String artistName = cursor.getString(artistNameColumnIndex);
                int durationMilli = cursor.getInt(songDurationColumnIndex);
                String duration = changeTimeFormat(durationMilli);
                String songPath = cursor.getString(songPathColumnIndex);
                int songId = cursor.getInt(songIdColumnIndex);
                songs.add(new Song(name,artistName,songId,songPath,duration));
            } while(cursor.moveToNext());
            musicAdapter = new MusicAdapter(this,songs);
            songList.setAdapter(musicAdapter);
        } else {
            currentPosition = -1;
        }
    }

    public void playMusic(){
        mediaPlayer = new MediaPlayer();
        try {
            int result = audioManager.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.setDataSource(songs.get(currentPosition).getSongPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(onCompletionListener);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        topPlayButton.setImageResource(R.drawable.ic_pause);
        bottomPlayButton.setImageResource(R.drawable.ic_pause_circle);
        currentSongName.setText(songs.get(currentPosition).getSongName());
        currentSongArtist.setText(songs.get(currentPosition).getArtistName());
    }

    public void releaseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }
}
