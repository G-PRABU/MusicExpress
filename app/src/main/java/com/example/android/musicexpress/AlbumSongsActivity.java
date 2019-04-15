package com.example.android.musicexpress;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;

public class AlbumSongsActivity extends AppCompatActivity {

    private int currentPosition;
    private MediaPlayer mediaPlayer;
    private AudioManager albumAudioManager;
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
    BottomSheetBehavior albumSongControlBottomSheet;
    ConstraintLayout albumSongControlLayout;
    TextView albumSongTitle;
    TextView albumSongArtist;
    ImageButton albumNextButton;
    ImageButton albumPreviousButton;
    ImageButton albumTopPlayButton;
    ImageButton albumBottomPlayButton;
    ListView albumSongList;
    private ArrayList<Song> albumSongs;
    private MusicAdapter musicAdapter;
    boolean isMusicStarted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.album_songs_title);
        final int albumId = Integer.parseInt(getIntent().getStringExtra(getString(R.string.album_id_key)));
        setContentView(R.layout.activity_album_songs);
        albumAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        albumSongList = (ListView) findViewById(R.id.album_song_list);
        albumSongControlLayout = (ConstraintLayout) findViewById(R.id.album_song_controls);
        albumSongControlBottomSheet = BottomSheetBehavior.from(albumSongControlLayout);
        albumNextButton = (ImageButton) findViewById(R.id.album_next_button);
        albumPreviousButton = (ImageButton) findViewById(R.id.album_previous_button);
        albumTopPlayButton = (ImageButton) findViewById(R.id.album_play_pause_button);
        albumBottomPlayButton = (ImageButton) findViewById(R.id.album_play_button);
        albumSongTitle = (TextView)findViewById(R.id.album_current_song_name);
        albumSongArtist = (TextView)findViewById(R.id.album_current_song_artist);
        albumSongs = new ArrayList<Song>();
        getAllSongs(albumId);
        albumSongControlBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch(newState){
                    case BottomSheetBehavior.STATE_EXPANDED:
                        albumTopPlayButton.setVisibility(View.INVISIBLE);
                        albumSongList.setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        albumTopPlayButton.setVisibility(View.VISIBLE);
                        albumSongList.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        albumSongList.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        albumSongList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                currentPosition = position;
                playMusic();
            }
        });
        albumTopPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        albumBottomPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        albumNextButton.setOnClickListener(new View.OnClickListener() {
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
        albumPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    releaseMediaPlayer();
                }
                if(currentPosition > 0){
                    currentPosition--;
                } else {
                    currentPosition = albumSongs.size()-1;
                }
                playMusic();
            }
        });
        albumSongTitle.setText(albumSongs.get(currentPosition).getSongName());
        albumSongArtist.setText(albumSongs.get(currentPosition).getArtistName());
    }

    public boolean isAvailable(){
        return albumSongs.size()-1>currentPosition;
    }

    public void pausePlay(){
        if(!isMusicStarted && mediaPlayer == null){
            playMusic();
            isMusicStarted = true;
        } else {
            if(mediaPlayer.isPlaying()){
                albumTopPlayButton.setImageResource(R.drawable.ic_play);
                albumBottomPlayButton.setImageResource(R.drawable.ic_play_circle);
                mediaPlayer.pause();
            } else {
                albumTopPlayButton.setImageResource(R.drawable.ic_pause);
                albumBottomPlayButton.setImageResource(R.drawable.ic_pause_circle);
                mediaPlayer.start();
            }
        }
    }

    public String changeTimeFormat(int durationMilli) {
        String minutes = String.valueOf((durationMilli / 1000) / 60);
        String seconds;
        if ((durationMilli / 1000) % 60 < 10) {
            seconds = "0" + String.valueOf((durationMilli / 1000) % 60);
        } else {
            seconds = String.valueOf((durationMilli / 1000) % 60);
        }
        return minutes + ":" + seconds;
    }

    public void getAllSongs(int albumId) {
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String albumSelection = MediaStore.Audio.Media.ALBUM_ID+"=?";
        String[] id = {String.valueOf(albumId)};
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(musicUri, null, albumSelection, id, null);
        if (cursor != null && cursor.moveToFirst()) {
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
                albumSongs.add(new Song(name, artistName, songId, songPath, duration));
            } while (cursor.moveToNext());
            musicAdapter = new MusicAdapter(this,albumSongs);
            albumSongList.setAdapter(musicAdapter);
        }
    }

    public void playMusic(){
        mediaPlayer = new MediaPlayer();
        try {
            int result = albumAudioManager.requestAudioFocus(audioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.setDataSource(albumSongs.get(currentPosition).getSongPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(onCompletionListener);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        albumTopPlayButton.setImageResource(R.drawable.ic_pause);
        albumBottomPlayButton.setImageResource(R.drawable.ic_pause_circle);
        albumSongTitle.setText(albumSongs.get(currentPosition).getSongName());
        albumSongArtist.setText(albumSongs.get(currentPosition).getArtistName());
    }

    public void releaseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
            albumAudioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    @Override
    public void onBackPressed() {
        releaseMediaPlayer();
        super.onBackPressed();
    }
}
