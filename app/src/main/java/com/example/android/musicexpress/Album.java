package com.example.android.musicexpress;

public class Album {
    private int mAlbumId;
    private String mAlbumName;

    public Album(String albumName,int albumId){
        mAlbumId = albumId;
        mAlbumName = albumName;
    }

    public String getAlbumName(){
        return mAlbumName;
    }

    public int getAlbumId() {
        return mAlbumId;
    }
}
