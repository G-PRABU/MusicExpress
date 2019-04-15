package com.example.android.musicexpress;

public class Song {

    private String mSongName;
    private String mArtistName;
    private int mSongId;
    private String mSongPath;
    private String mSongDuration;

    public Song(String songName,String artistName,int songId,String songPath,String songDuration){
        mSongName = songName;
        mArtistName = artistName;
        mSongId = songId;
        mSongPath = songPath;
        mSongDuration = songDuration;
    }

    public String getSongName(){
        return mSongName;
    }

    public String getArtistName(){
        return mArtistName;
    }

    public String getSongPath() {
        return mSongPath;
    }

    public String getSongDuration() {
        return mSongDuration;
    }

    public int getSongId() {
        return mSongId;
    }
}
